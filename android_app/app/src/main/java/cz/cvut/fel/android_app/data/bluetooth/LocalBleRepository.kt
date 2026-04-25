package cz.cvut.fel.android_app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.domain.repository.BleRepository
import cz.cvut.fel.android_app.domain.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("MissingPermission")
class LocalBleRepository(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceRepository: DeviceRepository
) : BleRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    @Volatile private var gatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    override val connectionState = _connectionState.asStateFlow()

    private val _velocityReadings = MutableSharedFlow<Double>(extraBufferCapacity = 128)
    override val velocityReadings: Flow<Double> = _velocityReadings.asSharedFlow()

    private val _batteryLevel = MutableStateFlow<Int>(0)
    override val batteryLevel: Flow<Int> = _batteryLevel.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<Device>>(emptyList())
    override val scannedDevices: Flow<List<Device>> = _scannedDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown Device"
            val address = device.address

            _scannedDevices.update { devices ->
                if (devices.any { it.macAddress == address }) {
                    devices
                } else {
                    devices + Device(name = name, macAddress = address, lastConnected = null)
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Error(status)
                gatt.close()
                this@LocalBleRepository.gatt = null
                return
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (gatt.device.bondState == BluetoothDevice.BOND_NONE) {
                        gatt.device.createBond()
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = BleConnectionState.Disconnected
                    gatt.close()
                    this@LocalBleRepository.gatt = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Error(status)
                return
            }

            val velocityChar = gatt.getService(SERVICE_UUID)?.getCharacteristic(VELOCITY_CHAR_UUID)
            if (velocityChar != null) {
                enableNotifications(gatt, velocityChar)
            } else {
                _connectionState.value = BleConnectionState.Error(-1)
                return
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (descriptor.uuid != CCCD_UUID) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Error(status)
                return
            }

            if (descriptor.characteristic.uuid == VELOCITY_CHAR_UUID) {
                val batteryChar = gatt.getService(BATTERY_SERVICE_UUID)?.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)
                if (batteryChar != null) {
                    enableNotifications(gatt, batteryChar)
                } else {
                    finishConnection(gatt)
                }
            } else if (descriptor.characteristic.uuid == BATTERY_LEVEL_CHAR_UUID) {
                finishConnection(gatt)
            }
        }

        private fun finishConnection(gatt: BluetoothGatt) {
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            _connectionState.value = BleConnectionState.Connected(gatt.device.address)
            
            // Update last connected timestamp in DB
            scope.launch {
                val address = gatt.device.address
                val knownDevices = deviceRepository.getAll().firstOrNull() ?: emptyList()
                knownDevices.find { it.macAddress == address }?.let { device ->
                    deviceRepository.updateLastConnected(device.id, System.currentTimeMillis())
                }
            }
        }

        @Suppress("DEPRECATION", "OverridingDeprecatedMember")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            handleCharacteristicChanged(characteristic.uuid, characteristic.value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleCharacteristicChanged(characteristic.uuid, value)
        }
    }

    private fun handleCharacteristicChanged(uuid: UUID, value: ByteArray) {
        when (uuid) {
            VELOCITY_CHAR_UUID -> parseAndEmitVelocity(value)
            BATTERY_LEVEL_CHAR_UUID -> parseAndEmitBattery(value)
        }
    }

    override fun connect(address: String) {
        if (gatt != null) return
        stopScanning()
        _connectionState.value = BleConnectionState.Connecting
        gatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(context, false, gattCallback)
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun startScanning() {
        _scannedDevices.value = emptyList()
        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
    }

    override fun stopScanning() {
        bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
    }

    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: run {
            _connectionState.value = BleConnectionState.Error(-2)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    private fun parseAndEmitVelocity(bytes: ByteArray) {
        val value = bytes.toString(Charsets.UTF_8).trim().toDoubleOrNull() ?: return
        scope.launch {
            _velocityReadings.emit(value)
        }
    }

    private fun parseAndEmitBattery(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val level = bytes[0].toInt() and 0xFF
        _batteryLevel.value = level
    }

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("a177eaf2-c661-4f76-b07d-36826eca67bd")
        private val VELOCITY_CHAR_UUID: UUID = UUID.fromString("0f6866f4-8a14-43a9-b7e4-93075f456d5c")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        private val BATTERY_LEVEL_CHAR_UUID: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    }
}
