package pl.goeuropa.counter.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.goeuropa.counter.dto.BusLoadDto;
import pl.goeuropa.counter.dto.ArduinoDeviceSnapshotDto;
import pl.goeuropa.counter.service.CounterService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CounterController {

    private CounterService service;

    @Autowired
    public CounterController(CounterService service) {
        this.service = service;
    }

    @GetMapping("/v1/busloads")
    public Map<String, BusLoadDto> getAll() {
        var mapOfCounts = service.getCountDetailsWithTimeCheck();

        log.info("Get {} objects of occupancy", mapOfCounts.size());
        return mapOfCounts;
    }

    @GetMapping("/v2/devices")
    public Map<String, ArduinoDeviceSnapshotDto> getDeviceAndruino() {
        var snapshots = service.getDeviceSnapshots();
        log.info("Get {} device snapshots", snapshots.size());
        return snapshots;
    }

    @PostMapping(value = "/v2/devices", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String uploadDeviceAndruino(@RequestBody ArduinoDeviceSnapshotDto snapshot) {
        service.saveDeviceSnapshot(snapshot);
        log.info("Received snapshot from device [{}]", snapshot.getDeviceId());
        return "Snapshot from " + snapshot.getDeviceId() + " saved.";
    }

    @PostMapping(value = "/v1/upload-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String uploadJson(HttpServletRequest request) {
        try (
                BufferedReader inputFile = new BufferedReader(
                        new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> lines = inputFile.lines().toList();

            service.asyncParseJsonFile(lines);

            log.debug("Uploaded json file content [{}] - has been processed.", lines.size());
            return "Uploaded json file content " + lines.size() + " lines - has been processing.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
