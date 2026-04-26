#include "ble_control.h"
#include <Adafruit_TinyUSB.h>
#include <nrf_wdt.h>

#define SIGNAL_PIN D2
#define BUTTON_PIN D1
#define LONG_PRESS_MS 1000
#define NO_PULSE_TIMEOUT_MS 90
#define K 0.02617f

volatile uint32_t pulse_count = 0;
volatile uint32_t rotation_time = 0;
volatile uint32_t first_pulse_time = 0;
volatile bool new_rotation = false;

static float last_velocity = 0;
static uint32_t last_send_time = 0;
static uint32_t last_battery_time = 0;
static uint32_t last_pulse_time = 0;

void check_button() {
    if (digitalRead(BUTTON_PIN) == LOW) {
        uint32_t press_start = millis();
        while (digitalRead(BUTTON_PIN) == LOW);
        uint32_t duration = millis() - press_start;

        if (duration >= LONG_PRESS_MS) {
            ble_start_advertising();
        } else if (!ble_is_enabled()) {
            ble_enable();
        } else {
            ble_stop_advertising();
        }
    }
}

uint8_t read_battery_percent() {
    float voltage = analogRead(PIN_VBAT) * 3.3f / 1024.0f * 2.0f;
    uint8_t percent = constrain((voltage - 3.0f) / 1.2f * 100.0f, 0, 100);
    return percent;
}

void on_falling_edge() {
    pulse_count++;
    if (pulse_count == 1) {
        first_pulse_time = micros();
    }
    if (pulse_count >= 4) {
        rotation_time = micros() - first_pulse_time;
        pulse_count = 0;
        new_rotation = true;
    }
}

void setup() {
    NRF_WDT->CONFIG = 0x01;
    NRF_WDT->CRV = 32768 * 10;
    NRF_WDT->RREN = 0x01;
    NRF_WDT->TASKS_START = 1;

    Serial.begin(115200);
    delay(500);

    pinMode(SIGNAL_PIN, INPUT);
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(SIGNAL_PIN), on_falling_edge, FALLING);

    ble_setup();
    Serial.println("Ready!");
    ble_start_advertising();
}

void loop() {
    check_button();
    ble_loop();

    if (new_rotation) {
        new_rotation = false;
        noInterrupts();
        uint32_t t = rotation_time;
        interrupts();

        float rps = 1000000.0f / t;
        last_velocity = K * rps;
        last_pulse_time = millis();

        Serial.print("velocity: ");
        Serial.print(last_velocity, 2);
        Serial.println(" m/s");
    }

    if (millis() - last_send_time >= 100) {
        last_send_time = millis();
        if (millis() - last_pulse_time > NO_PULSE_TIMEOUT_MS) {
            ble_send_velocity(0);
        } else {
            ble_send_velocity(last_velocity);
        }
    }

    if (millis() - last_battery_time >= 30000) {
        last_battery_time = millis();
        ble_send_battery(read_battery_percent());
    }

    delay(1);
    NRF_WDT->RR[0] = WDT_RR_RR_Reload;
}