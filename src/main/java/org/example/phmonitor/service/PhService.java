package org.example.phmonitor.service;

import com.fazecast.jSerialComm.SerialPort;
import org.example.phmonitor.model.PhModel;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.Scanner;

@Service
public class PhService {

    private SerialPort serialPort;
    private Scanner scanner;
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

        scanner = new Scanner(serialPort.getInputStream());
    }

    public PhModel getLatestReading() {
        try {
            serialPort.getOutputStream().write('r');
            serialPort.getOutputStream().flush();

            long start = System.currentTimeMillis();
            while (!scanner.hasNextLine() && System.currentTimeMillis() - start < 2000) {
                Thread.sleep(50);
            }

            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                double value = Double.parseDouble(line.trim());
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
