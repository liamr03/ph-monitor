package org.example.phmonitor.controller;

import org.example.phmonitor.model.PhModel;
import org.example.phmonitor.service.PhService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ph")
@CrossOrigin(origins = "*") // Needed if frontend is on a different port
public class PhController {
    private static final Logger logger = LoggerFactory.getLogger(PhController.class);
    private final PhService phService;

    public PhController(PhService phService) {
        this.phService = phService;
    }

    @PostMapping
    public ResponseEntity<String> receivePhData(@RequestBody PhModel reading) {
        logger.info("Received pH: {}", reading.getValue());
        phService.saveReading(reading);
        return ResponseEntity.ok("Received");
    }

    @GetMapping
    public PhModel latestReading() {
        return phService.getLatestReading();
    }
}
