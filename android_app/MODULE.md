# Module Stream Measurement App

Android application for measuring stream flow rates using a BLE measuring device.
The measuring device is a microcontroller that connects to the velocity probe (sensor) by wire and communicates with the app over Bluetooth.

## Domain Layer
Core business logic — independent of Android and databases.
- **`domain`** — use cases
- **`domain.model`** — domain entities with physical units (m, m/s, m³/s)
- **`domain.repository`** — repository contracts

## Data Layer
Repository implementations and data sources.
- **`data.bluetooth`** — BLE GATT client for the flow-meter measuring device
- **`data.location`** — GPS via FusedLocationProviderClient
- **`data.measurement`** — Room-backed measurement repository

## ViewModel Layer
UI state management per screen.
- **`viewmodel`** — ViewModels for each screen

# Package cz.cvut.fel.android_app.domain
Use cases implementing stream measurement business logic.

# Package cz.cvut.fel.android_app.domain.model
Domain entities. All physical values use SI units unless noted otherwise.

# Package cz.cvut.fel.android_app.domain.repository
Repository interfaces forming the contract between domain and data layers.

# Package cz.cvut.fel.android_app.data.bluetooth
BLE GATT client for the custom flow-meter measuring device.

# Package cz.cvut.fel.android_app.data.location
GPS location provider backed by FusedLocationProviderClient.

# Package cz.cvut.fel.android_app.data.measurement
Room-backed implementation of StreamMeasurementRepository.

# Package cz.cvut.fel.android_app.viewmodel
ViewModels managing UI state for each screen.