import board
import busio
import adafruit_ads1x15.ads1115 as ADS
from adafruit_ads1x15.analog_in import AnalogIn
from flask import Flask, jsonify, render_template, send_from_directory
import collections

app = Flask(__name__, static_url_path='/static')
CORS(app)

# --- Hardware Setup ---
hardware_ok = False
chan = None
probe_connected = True
consecutive_errors = 0

try:
    i2c = busio.I2C(board.SCL, board.SDA)
    ads = ADS.ADS1115(i2c)
    ads.gain = 1
    chan = AnalogIn(ads, 0)
    hardware_ok = True
except Exception as e:
    print(f"HARDWARE ERROR: ADC init failed — {e}")

# --- Calculation Constants ---
VOLTAGE_AT_686 = 2.522
VOLTAGE_AT_401 = 2.990
SLOPE = (4.01 - 6.86) / (VOLTAGE_AT_401 - VOLTAGE_AT_686)

# Median filter: keep last N samples, return median
SAMPLE_COUNT = 5
reading_buffer = collections.deque(maxlen=SAMPLE_COUNT)

@app.route('/')
@app.route('/index.html')
def index():
    # Serves index.html from the /templates folder
    return render_template('index.html')

@app.route('/ph', methods=['GET'])
def get_ph():
    global probe_connected, consecutive_errors

    if not hardware_ok or chan is None:
        return jsonify({"error": "ADC not available — check I2C connection"}), 500

    try:
        voltage = chan.voltage
        ph_value = 6.86 + (voltage - VOLTAGE_AT_686) * SLOPE

        # Reject out-of-range readings (pH 0-14 maps to ~0.5-4.5V for this setup)
        if voltage < 0.1 or voltage > 5.0:
            consecutive_errors += 1
            if consecutive_errors > 3:
                probe_connected = False
            return jsonify({"error": f"Out-of-range voltage: {round(voltage, 3)}V — probe may be disconnected"}), 500

        if ph_value < 0 or ph_value > 14:
            consecutive_errors += 1
            if consecutive_errors > 3:
                probe_connected = False
            return jsonify({"error": f"Out-of-range pH: {round(ph_value, 2)} — probe may be disconnected"}), 500

        consecutive_errors = 0
        probe_connected = True
        reading_buffer.append(ph_value)

        # Return median of recent samples
        median_ph = sorted(reading_buffer)[len(reading_buffer) // 2]
        print(f"Reading: {round(median_ph, 2)} (Voltage: {round(voltage, 3)}V)")
        return jsonify({"value": round(median_ph, 2)})
    except Exception as e:
        consecutive_errors += 1
        if consecutive_errors > 3:
            probe_connected = False
        print(f"READ ERROR: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/service-worker.js')
def sw():
    return send_from_directory('static', 'service-worker.js')

if __name__ == '__main__':
    # Starts instantly on port 8080
    app.run(host='0.0.0.0', port=8080)
