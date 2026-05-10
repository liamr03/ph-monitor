import board
import busio
import adafruit_ads1x15.ads1115 as ADS
from adafruit_ads1x15.analog_in import AnalogIn
from flask import Flask, jsonify, render_template, send_from_directory
from flask_cors import CORS

app = Flask(__name__, static_url_path='/static')
CORS(app)

# --- Hardware Setup ---
# The Adafruit library handles the single-shot trigger and byte-swapping automatically!
i2c = busio.I2C(board.SCL, board.SDA)
ads = ADS.ADS1115(i2c)
ads.gain = 1  # This matches your ±4.096V range
chan = AnalogIn(ads, 0)

# --- Calculation Constants ---
VOLTAGE_AT_686 = 2.522
VOLTAGE_AT_401 = 2.990
SLOPE = (4.01 - 6.86) / (VOLTAGE_AT_401 - VOLTAGE_AT_686)

@app.route('/')
@app.route('/index.html')
def index():
    # Serves index.html from the /templates folder
    return render_template('index.html')

@app.route('/ph', methods=['GET'])
def get_ph():
    try:
        # Every time the UI calls this, the library triggers a fresh conversion
        voltage = chan.voltage
        ph_value = 6.86 + (voltage - VOLTAGE_AT_686) * SLOPE
        print(f"Reading: {round(ph_value, 2)} (Voltage: {round(voltage, 3)}V)")
        return jsonify({"value": round(ph_value, 2)})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Helper route to make sure your PWA Service Worker is found
@app.route('/service-worker.js')
def sw():
    return send_from_directory('static', 'service-worker.js')

if __name__ == '__main__':
    # Starts instantly on port 8080
    app.run(host='0.0.0.0', port=8080)
