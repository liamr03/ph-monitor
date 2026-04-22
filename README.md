# Arduino pH Monitoring System

Description to be updated...

## Features
Features to be updated...

## Hardware Setup

Hardware setup to be updated...

<div align="center">
  <img width="423" height="922" alt="Mobile App UI Screenshot" src="https://github.com/user-attachments/assets/9916e792-ad97-42c8-8442-94d623a1e403" />
  <p><em>Example of the mobile interface paired with this hardware.</em></p>
</div>

---

## Arduino Code

```cpp
void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available() > 0) {
    char command = Serial.read();
    
    // Send 'r' via Serial Monitor to get a reading
    if (command == 'r') {
      float ph = getAveragePH();  // Get filtered average
      Serial.println(ph, 2);      // Print pH value with 2 decimal places
    }
  }
}

/**
 * Collects samples, removes outliers, and returns the average pH.
 */
float getAveragePH() {
  const int sampleSize = 20;
  float phValues[sampleSize];

  // 1. Collect pH readings
  for (int i = 0; i < sampleSize; i++) {
    int raw = analogRead(A0);
    // Linear Calibration Formula: y = mx + b
    phValues[i] = -0.025 * raw + 26.32; 
    delay(50); 
  }

  // 2. Sort the values (Simple Bubble Sort)
  for (int i = 0; i < sampleSize - 1; i++) {
    for (int j = i + 1; j < sampleSize; j++) {
      if (phValues[i] > phValues[j]) {
        float temp = phValues[i];
        phValues[i] = phValues[j];
        phValues[j] = temp;
      }
    }
  }

  // 3. Average middle values (remove top 5 and bottom 5)
  float total = 0;
  for (int i = 5; i < sampleSize - 5; i++) {
    total += phValues[i];
  }

  return total / (sampleSize - 10);
}
