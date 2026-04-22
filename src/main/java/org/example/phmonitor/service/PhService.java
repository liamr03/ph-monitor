package org.example.phmonitor.service;

import com.fazecast.jSerialComm.SerialPort;
import org.example.phmonitor.model.PhModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PhService {

    private static final Logger logger = LoggerFactory.getLogger(PhService.class);

    @Value("${arduino.port:/dev/ttyUSB0}")
    private String portName;

    @Value("${arduino.poll-interval-ms:1000}")
    private int pollIntervalMs;

    private final AtomicReference<PhModel> latestReading = new AtomicReference<>(new PhModel(0.0));
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private BufferedReader reader;
    private OutputStream outputStream;

    @PostConstruct
    public void init() {
        scheduler.submit(() -> {
            try {
                SerialPort port = SerialPort.getCommPort(portName);
                port.setBaudRate(9600);
                port.setNumDataBits(8);
                port.setNumStopBits(SerialPort.ONE_STOP_BIT);
                port.setParity(SerialPort.NO_PARITY);
                port.setDTR();

                if (!port.openPort()) {
                    logger.error("Failed to open serial port: {} ", portName);
                    return;
                }

                Thread.sleep(2000); // Wait for Arduino to reset after DTR
                port.flushIOBuffers();

                reader = new BufferedReader(new InputStreamReader(port.getInputStream(), StandardCharsets.UTF_8));
                outputStream = port.getOutputStream();

                scheduler.scheduleAtFixedRate(this::poll, 0, pollIntervalMs, TimeUnit.MILLISECONDS);
                logger.info("Arduino ready on {}", portName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Serial setup interrupted", e);
            } catch (Exception e) {
                logger.error("Serial setup failed: {}", e.getMessage());
            }
        });
    }

    private void poll() {
        if (reader == null || outputStream == null) return;
        try {
            outputStream.write("r".getBytes());
            outputStream.flush();
            Thread.sleep(200);

            if (!reader.ready()) return;

            String line = reader.readLine();
            if (line == null || line.isBlank()) return;

            double value = Double.parseDouble(line.trim().replace(',', '.'));
            if (value >= 0 && value <= 14) latestReading.set(new PhModel(value));
            else logger.error("Out-of-range pH ignored: {}", value);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Poll interrupted", e);
        } catch (Exception e) {
            logger.error("Poll error: {}", e.getMessage());
        }
    }

    public PhModel getLatestReading() {
        return latestReading.get();
    }

    public void saveReading(PhModel reading) {
        if (reading.getValue() < 0 || reading.getValue() > 14)
            throw new IllegalArgumentException("pH value must be between 0 and 14");
        latestReading.set(reading);
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdownNow();
    }
}
