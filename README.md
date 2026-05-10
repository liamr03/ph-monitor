# Pool pH Monitor

A lightweight, real-time pool pH monitoring system designed specifically to run efficiently with a Raspberry Pi 

This project features a **Python Flask** backend for hardware communication and a **Progressive Web App (PWA)** frontend with a sleek, dark-mode dashboard.

##  Features
* **Real-time Monitoring:** pH levels updated every 5–10 seconds.
* **Hardware Optimized:** Uses Python's `adafruit-ads1x15` for stable, single-shot I2C readings.
* **High Precision:** Utilizes the 16-bit ADS1115 Analog-to-Digital Converter.

## 🛠 Hardware Requirements
* **Raspberry Pi** (Tested on Pi 1 Model B)
* **ADS1115** 16-bit ADC
* **Analog pH Probe & Driver Board**
* **I2C enabled** via `raspi-config`
