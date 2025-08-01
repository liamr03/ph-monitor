package org.example.phmonitor.controller;

import org.example.phmonitor.model.PhModel;
import org.example.phmonitor.service.PhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ph")
@CrossOrigin(origins = "*") // Needed if frontend is on a different port
public class PhController {

    private final PhService phService;

    @Autowired
    public PhController(PhService phService) {
        this.phService = phService;
    }

    @PostMapping
    public ResponseEntity<String> receivePhData(@RequestBody PhModel reading) {
        System.out.println("Received pH: " + reading.getValue());
        phService.saveReading(reading);
        return ResponseEntity.ok("Received");
    }

    @GetMapping
    public PhModel latestReading() {
        return phService.getLatestReading();
    }
}
