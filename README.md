Code for Arduino:

void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available() > 0) {
    char command = Serial.read();
    if (command == 'r') {
      float ph = getAveragePH();  // Get filtered average
      Serial.println(ph, 2);      // Print pH value with 2 decimal places
    }
  }
}

// Improved averaging with outlier removal
float getAveragePH() {
  const int sampleSize = 20;
  float phValues[sampleSize];

  // Collect pH readings
  for (int i = 0; i < sampleSize; i++) {
    int raw = analogRead(A0);
    phValues[i] = -0.025 * raw + 26.32; // Use your latest calibration formula
    delay(50);  // Shorter delay between reads
  }

  // Sort the values
  for (int i = 0; i < sampleSize - 1; i++) {
    for (int j = i + 1; j < sampleSize; j++) {
      if (phValues[i] > phValues[j]) {
        float temp = phValues[i];
        phValues[i] = phValues[j];
        phValues[j] = temp;
      }
    }
  }

  // Average middle values (remove top & bottom 5)
  float total = 0;
  for (int i = 5; i < sampleSize - 5; i++) {
    total += phValues[i];
  }

  return total / (sampleSize - 10);
}

