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
    if (command == 'r') {
      Serial.println(getPH(), 2);
    }
  }
}

float getPH() {
  const int sampleSize = 5;
  float total = 0;

  for (int i = 0; i < sampleSize; i++) {
    int raw = analogRead(A0);
    total += -0.025 * raw + 26.32;
    delay(50);
  }

  return total / sampleSize;
}
