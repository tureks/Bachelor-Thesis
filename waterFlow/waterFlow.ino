#include "ble_control.h"
#include <Adafruit_TinyUSB.h>
#include <nrf_wdt.h>

#define DEVICE_PIN D7
#define SIGNAL_PIN D8
#define BUTTON_PIN D9
#define LONG_PRESS_MS 1000
#define NO_PULSE_TIMEOUT_MS 90
#define K 0.01941f

volatile uint32_t pulse_count = 0;
volatile uint32_t rotation_time = 0;
volatile uint32_t first_pulse_time = 0;
volatile bool new_rotation = false;

static float last_velocity = 0;
static uint32_t last_status_time = 0;
static uint32_t last_send_time = 0;
static uint32_t last_battery_time = 0;
static uint32_t last_pulse_time = 0;

void go_to_deep_sleep() {
    Serial.println("Going to deep sleep. Press button to wake up.");
    Serial.flush();
    delay(100);

    detachInterrupt(digitalPinToInterrupt(SIGNAL_PIN));

    ble_shutdown();

    pinMode(VBAT_ENABLE, OUTPUT);
    digitalWrite(VBAT_ENABLE, LOW);

    pinMode(BUTTON_PIN, INPUT_PULLUP_SENSE);

    NRF_POWER->SYSTEMOFF = 1;
    while (1);  
}

bool is_device_connected() {
    return digitalRead(DEVICE_PIN) == LOW; 
}

void check_button() {
    if (digitalRead(BUTTON_PIN) == LOW) {
        uint32_t press_start = millis();
        while (digitalRead(BUTTON_PIN) == LOW);
        uint32_t duration = millis() - press_start;

        if (duration >= LONG_PRESS_MS) {
            go_to_deep_sleep();

        } else {
            if (!ble_is_advertising() && !ble_is_connected()) {
                ble_start_advertising();
            } else if (ble_is_advertising()) {
                ble_stop_advertising();
            }
        }
    }
}

float read_voltage() {
    pinMode(VBAT_ENABLE, OUTPUT);
    digitalWrite(VBAT_ENABLE, LOW);
    delay(50);
    analogReadResolution(12);
    
    float sum = 0;
    for (int i = 0; i < 16; i++) {
        sum += analogRead(PIN_VBAT);
        delay(3);
    }
    float raw = sum / 16.0f;
    digitalWrite(VBAT_ENABLE, HIGH);

    float voltage = raw * 3.6f / 4096.0f * 3.0f;
    Serial.print("ADC raw: ");
    Serial.print(raw);
    Serial.print(" | Voltage: ");
    Serial.print(voltage, 2);
    Serial.print("V | Battery: ");
    return voltage;
}

uint8_t voltage_to_percent(float voltage) {
    // Li-Ion discharge curve (3.0V cutoff, 4.2V max)
    static const float v[] = {4.20, 4.15, 4.10, 4.05, 4.00, 3.95,
                               3.90, 3.85, 3.80, 3.75, 3.70, 3.65,
                               3.60, 3.55, 3.50, 3.45, 3.40, 3.30,
                               3.20, 3.10, 3.00};
    static const uint8_t p[] = {100, 98, 95, 91, 86, 81,
                                  76, 70, 64, 58, 52, 46,
                                  40, 34, 28, 22, 16, 10,
                                   5,  2,  0};
    static const int n = 21;

    if (voltage >= v[0])   return 100;
    if (voltage <= v[n-1]) return 0;

    for (int i = 0; i < n - 1; i++) {
        if (voltage >= v[i+1]) {
            float t = (voltage - v[i+1]) / (v[i] - v[i+1]);
            return (uint8_t)(p[i+1] + t * (p[i] - p[i+1]));
        }
    }
    return 0;
}

uint8_t read_battery_percent() {
    float voltage = read_voltage();

    // Low battery check
    if (voltage < 3.0f) {
        Serial.println("LOW BATTERY!");
        pinMode(LED_BUILTIN, OUTPUT);
        for (int i = 0; i < 3; i++) {
            digitalWrite(LED_BUILTIN, LOW);
            delay(200);
            digitalWrite(LED_BUILTIN, HIGH);
            delay(200);
            NRF_WDT->RR[0] = WDT_RR_RR_Reload;
        }
        go_to_deep_sleep();
    }

    uint8_t percent = voltage_to_percent(voltage);
    Serial.print(percent);
    Serial.println("%");
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
    Serial.begin(115200);
    delay(500);

    pinMode(SIGNAL_PIN, INPUT);
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    pinMode(DEVICE_PIN, INPUT_PULLUP);

    battery_callback = read_battery_percent;
    status_callback = is_device_connected;
    read_battery_percent();

    attachInterrupt(digitalPinToInterrupt(SIGNAL_PIN), on_falling_edge, FALLING);

    ble_setup();
    Serial.println("Ready!");
    ble_start_advertising();

    NRF_WDT->CONFIG = 0x01;
    NRF_WDT->CRV = 32768 * 10;
    NRF_WDT->RREN = 0x01;
    NRF_WDT->TASKS_START = 1;
}

void loop() {
    check_button();
    ble_loop();

    if (!ble_is_enabled()) {
        go_to_deep_sleep();
    }

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

    if (millis() - last_status_time >= 1000) {
        last_status_time = millis();
        bool probe = is_device_connected();
        ble_send_status(probe);
    }

    delay(1);
    NRF_WDT->RR[0] = WDT_RR_RR_Reload;
}