package org.example.phmonitor.service;

import com.fazecast.jSerialComm.SerialPort;
import org.example.phmonitor.model.PhModel;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class PhService {

    private SerialPort serialPort;
    private BufferedReader reader;
    private PhModel latestReading = new PhModel(0.0);

    public PhService() {
        serialPort = SerialPort.getCommPort("/dev/ttyUSB0"); // Adjust as needed
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);

        serialPort.setParity(SerialPort.NO_PARITY);

        // Enable DTR to reset Arduino on open (like screen does)
        serialPort.setDTR();

        // Open port and check success
        if (serialPort.openPort()) {
            System.out.println("Serial port opened successfully.");
            try {
                Thread.sleep(2000);  // Wait 2 seconds for Arduino to reset and be ready
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to open serial port.");
            return;  // or handle failure as needed
        }


        // Flush any leftover data
        serialPort.flushIOBuffers();

        // Wait for Arduino to reset and start sending data
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), StandardCharsets.UTF_8));
    }

    public PhModel getLatestReading() {
        try {
            OutputStream out = serialPort.getOutputStream();
            out.write("r".getBytes());
            out.flush();
            Thread.sleep(100); // delay for Arduino to respond

            String line = null;
            long start = System.currentTimeMillis();
            while ((line == null || line.isEmpty()) && System.currentTimeMillis() - start < 2000) {
                if (reader.ready()) {
                    line = reader.readLine();
                } else {
                    Thread.sleep(50);
                }
            }

            if (line != null && !line.isEmpty()) {
                System.out.println("Raw line received: " + line);

                // Print ASCII codes of each character in the line to debug spaces/newlines
                System.out.print("Raw bytes: ");
                for (char c : line.toCharArray()) {
                    System.out.print((int)c + " ");
                }
                System.out.println();

                // Replace comma with dot to ensure correct parsing
                String normalized = line.trim().replace(',', '.');

                double value = Double.parseDouble(normalized);
                System.out.println("Parsed value = " + value);

                latestReading = new PhModel(value);
                return latestReading;
            } else {
                throw new RuntimeException("No response from Arduino");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return latestReading; // fallback to last known
        }
    }

    public void saveReading(PhModel reading) {
        if (reading.getValue() < 0 || reading.getValue() > 14) {
            throw new IllegalArgumentException("pH value must be between 0 and 14");
        }
        this.latestReading = reading;
    }

    @PreDestroy
    public void cleanup() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }
}
