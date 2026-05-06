#pragma once
#include <Arduino.h>

// uuids
#define UUID_FLOW_SERVICE     "a177eaf2-c661-4f76-b07d-36826eca67bd"
#define UUID_VELOCITY_CHAR    "0f6866f4-8a14-43a9-b7e4-93075f456d5c"

void pair_complete_callback(uint16_t conn_handle, uint8_t auth_status);
void ble_setup();
void ble_loop();
void ble_start_advertising();
void ble_stop_advertising();
void ble_send_velocity(float velocity);
void ble_send_battery(uint8_t percent);
void ble_shutdown();
bool ble_is_connected();
bool ble_is_advertising();
bool ble_is_enabled();
extern uint8_t (*battery_callback)();