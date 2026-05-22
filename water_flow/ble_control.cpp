#include "ble_control.h"
#include <bluefruit.h>

#define BLE_TIMEOUT_MS 60000

BLEBas battery_service;
BLEService flow_service(UUID_FLOW_SERVICE);
BLECharacteristic velocity_char(UUID_VELOCITY_CHAR, BLENotify, 20);
BLECharacteristic status_char(UUID_STATUS_CHAR, BLERead | BLENotify, 1);

static bool ble_enabled = false;
static bool ble_advertising = false;
static uint32_t ble_disconnect_time = 0;

uint8_t (*battery_callback)() = nullptr;
bool (*status_callback)() = nullptr;

void disconnect_callback(uint16_t conn_handle, uint8_t reason) {
    ble_advertising = false;
    ble_disconnect_time = millis();
    Serial.print("Disconnected, reason: 0x");
    Serial.println(reason, HEX);

    if (reason != 0x13) {
        Bluefruit.Advertising.start(30);
        ble_advertising = true;
        Serial.println("Unexpected disconnect, advertising 30s...");
    }
}

void connect_callback(uint16_t conn_handle) {
    ble_advertising = false;
    Serial.println("Connected!");
    delay(200);
    if (battery_callback != nullptr) {
        uint8_t pct = battery_callback();
        battery_service.write(pct);
        battery_service.notify(pct);
    }
    if (status_callback != nullptr) {
        uint8_t val = status_callback();
        status_char.write(&val, 1);
        status_char.notify(&val, 1);
    }
}

void adv_stop_callback() {
    ble_advertising = false;
    Serial.println("Advertising stopped");
}

void pair_complete_callback(uint16_t conn_handle, uint8_t auth_status) {
    if (auth_status == 0) {
        Serial.println("Bonded successfully!");
    } else {
        Serial.print("Bonding failed, status: 0x");
        Serial.println(auth_status, HEX);
    }
}

void ble_setup() {
    Bluefruit.begin(1, 0);
    Bluefruit.setName("FlowMeter");

    // bonding setup
    Bluefruit.Security.setIOCaps(false, false, false);
    Bluefruit.Security.setPairCompleteCallback(pair_complete_callback);
    Bluefruit.Security.begin();

    // callbacks
    Bluefruit.Periph.setConnectCallback(connect_callback);
    Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

    // services
    battery_service.begin();
    flow_service.begin();
    velocity_char.begin();
    err_t status_err = status_char.begin();
    Serial.print("status_char.begin() = "); Serial.println(status_err);

    // advertising config
    Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
    Bluefruit.Advertising.setType(BLE_GAP_ADV_TYPE_CONNECTABLE_SCANNABLE_UNDIRECTED);
    Bluefruit.Advertising.addTxPower();
    Bluefruit.Advertising.addService(flow_service);
    Bluefruit.Advertising.addName();
    Bluefruit.Advertising.restartOnDisconnect(false);
    Bluefruit.Advertising.setStopCallback(adv_stop_callback);
    Bluefruit.Advertising.setInterval(32, 244);
    Bluefruit.Advertising.setFastTimeout(30);

    ble_enabled = true;
    ble_disconnect_time = millis();
    Serial.println("BLE ready");
}

void ble_loop() {
    if (ble_enabled &&
        !Bluefruit.connected() &&
        !ble_advertising &&
        millis() - ble_disconnect_time > BLE_TIMEOUT_MS) {

        ble_enabled = false;
        Serial.println("BLE turned off (timeout)");
    }
}

void ble_shutdown() {
    if (!ble_enabled) return; 
    if (Bluefruit.connected()) {
        Bluefruit.disconnect(Bluefruit.connHandle());
        delay(100);
    }
    ble_stop_advertising();
    ble_enabled = false;
    ble_advertising = false;
    Serial.println("BLE shut down");
}

void ble_start_advertising() {
    if (!ble_enabled) {
        ble_enabled = true;
        ble_disconnect_time = millis();
        Serial.println("BLE enabled");
    }

    if (!ble_advertising && !Bluefruit.connected()) {
        Bluefruit.Advertising.start(30);
        ble_advertising = true;
        ble_disconnect_time = millis();
        Serial.println("Advertising 30s...");
    }
}

void ble_stop_advertising() {
    if (ble_advertising) {
        Bluefruit.Advertising.stop();
        ble_advertising = false;
        Serial.println("Advertising stopped by user");
    }
}

void ble_send_velocity(float velocity) {
    if (Bluefruit.connected()) {
        char buf[10];
        sprintf(buf, "%.2f", velocity);
        velocity_char.write((uint8_t*)buf, strlen(buf));
        velocity_char.notify((uint8_t*)buf, strlen(buf));
    }
}

void ble_send_battery(uint8_t percent) {
    if (Bluefruit.connected()) {
        battery_service.write(percent);
        battery_service.notify(percent);
    }
}

void ble_send_status(bool device_connected) {
    if (Bluefruit.connected()) {
        uint8_t val = device_connected ? 1 : 0;
        status_char.write(&val, 1);
        status_char.notify(&val, 1);
    }
}

bool ble_is_connected() { return Bluefruit.connected(); }
bool ble_is_advertising() { return ble_advertising; }
bool ble_is_enabled() { return ble_enabled; }