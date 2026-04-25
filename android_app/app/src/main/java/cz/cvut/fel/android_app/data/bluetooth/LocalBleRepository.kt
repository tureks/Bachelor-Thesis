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
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class LocalBleRepository(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BleRepository {

    @Volatile private var gatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    override val connectionState = _connectionState.asStateFlow()

    private val _velocityReadings = MutableSharedFlow<Double>(extraBufferCapacity = 64)
    override val velocityReadings: Flow<Double> = _velocityReadings.asSharedFlow()

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
            val characteristic = gatt
                .getService(SERVICE_UUID)
                ?.getCharacteristic(VELOCITY_CHAR_UUID)
                ?: run {
                    _connectionState.value = BleConnectionState.Error(-1)
                    return
                }

            enableNotifications(gatt, characteristic)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (descriptor.uuid != CCCD_UUID) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Error(status)
                return
            }
            gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            _connectionState.value = BleConnectionState.Connected(gatt.device.address)
        }

        @Suppress("DEPRECATION", "OverridingDeprecatedMember")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            parseAndEmit(characteristic.value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseAndEmit(value)
        }
    }

    override fun connect(address: String) {
        if (gatt != null) return
        _connectionState.value = BleConnectionState.Connecting
        gatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(context, false, gattCallback)
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

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
        // Connected state is set in onDescriptorWrite after the peripheral confirms
    }

    private fun parseAndEmit(bytes: ByteArray) {
        val value = bytes.toString(Charsets.UTF_8).trim().toDoubleOrNull() ?: return
        _velocityReadings.tryEmit(value)
    }

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("a177eaf2-c661-4f76-b07d-36826eca67bd")
        private val VELOCITY_CHAR_UUID: UUID = UUID.fromString("0f6866f4-8a14-43a9-b7e4-93075f456d5c")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}