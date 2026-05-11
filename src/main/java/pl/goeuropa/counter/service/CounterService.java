package pl.goeuropa.counter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.goeuropa.counter.dto.BusLoadDto;
import pl.goeuropa.counter.dto.ArduinoDeviceSnapshotDto;
import pl.goeuropa.counter.dto.LogEntryDto;
import pl.goeuropa.counter.repository.PeopleCountRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CounterService {

    private final PeopleCountRepository peopleCountRepository = PeopleCountRepository.getInstance();

    @Value("${api.name-mapping:}")
    private String nameMapping;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public void asyncParseJsonFile(List<String> reader) {
        Thread.currentThread().setName("async-parser");
        log.debug("Started parsing json file by thread: [{}-{}]",
                Thread.currentThread().getName(),
                Thread.currentThread().getId());

        LogEntryDto newestCount = null;
        try {
            for (String line : reader) {
                if (line.isBlank()) continue;
                LogEntryDto dto = objectMapper.readValue(line, LogEntryDto.class);
                if (dto.getTimestamp() != null) {
                    if (newestCount == null || dto.getTimestamp() > newestCount.getTimestamp()) {
                        newestCount = dto;
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (newestCount != null) {
            BusLoadDto busLoadDto = new BusLoadDto(newestCount);
            if (nameMapping != null && !nameMapping.isBlank()){
                String[] vehicleNames = nameMapping.split(":");
                if (busLoadDto.getVehicleName().equals(vehicleNames[0]))
                    busLoadDto.setVehicleName(vehicleNames[1]);
            }
            log.info("Parsed new object {}", busLoadDto);
            peopleCountRepository.getUpdatesAboutLoads()
                    .put(busLoadDto.getVehicleName(), busLoadDto);
            log.debug("{} dto objects is persisted.", peopleCountRepository.getUpdatesAboutLoads().size());
        }
    }

    public Map<String, BusLoadDto> getCountDetailsWithTimeCheck() {
        BusLoadDto timeCheck = new BusLoadDto();
        timeCheck.setVehicleName(LocalDateTime.now().toString());
        timeCheck.setTimestamp(System.currentTimeMillis());
        var busLoadsWithTimeCheck = peopleCountRepository.getUpdatesAboutLoads();
        busLoadsWithTimeCheck.put("time", timeCheck);
        removeOldObjects(peopleCountRepository.getUpdatesAboutLoads());
        return peopleCountRepository.getUpdatesAboutLoads();
    }

    public void saveDeviceSnapshot(ArduinoDeviceSnapshotDto snapshot) {
        peopleCountRepository.getDeviceSnapshots().put(snapshot.getDeviceId(), snapshot);
        peopleCountRepository.getUpdatesAboutLoads().put(snapshot.getDeviceId(), new BusLoadDto(snapshot));
        log.debug("Device snapshot saved for [{}], total snapshots: {}",
                snapshot.getDeviceId(),
                peopleCountRepository.getDeviceSnapshots().size());
    }

    public Map<String, ArduinoDeviceSnapshotDto> getDeviceSnapshots() {
        return peopleCountRepository.getDeviceSnapshots();
    }

    private void removeOldObjects(Map<String, BusLoadDto> objectMap) {
        long currentTime = System.currentTimeMillis();
        long minutesInMillis = 30 * 60 * 1000;

        Iterator<Map.Entry<String, BusLoadDto>> iterator = objectMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BusLoadDto> entry = iterator.next();
            if ((currentTime - entry.getValue().getTimestamp()) > minutesInMillis ||
                    entry.getValue().getCurrentFullness() > 150) {
                log.debug("Removing old object: " + entry.getValue());
                iterator.remove();
            }
        }
    }
}
