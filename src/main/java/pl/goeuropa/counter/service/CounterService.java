package pl.goeuropa.counter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.goeuropa.counter.configs.ApiProperties;
import pl.goeuropa.counter.dto.BusLoadDto;
import pl.goeuropa.counter.repository.PeopleCountRepository;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounterService {

    private final ApiProperties properties;
    private final PeopleCountRepository peopleCountRepository = PeopleCountRepository.getInstance();

    public Map<String, BusLoadDto> getCountDetailsWithTimeCheck() {
        BusLoadDto timeCheck = new BusLoadDto();
        timeCheck.setVehicleName(LocalDateTime.now().toString());
        timeCheck.setTimestamp(System.currentTimeMillis());
        var busLoadsWithTimeCheck = peopleCountRepository.getUpdatesAboutLoads();
        busLoadsWithTimeCheck.put("time", timeCheck);
        removeOldObjects(peopleCountRepository.getUpdatesAboutLoads());
        return peopleCountRepository.getUpdatesAboutLoads();
    }

    private void removeOldObjects(Map<String, BusLoadDto> objectMap) {
        long currentTime = System.currentTimeMillis();
        long ttlInMillis = (long) properties.getStaleTtlMinutes() * 60 * 1000;

        Iterator<Map.Entry<String, BusLoadDto>> iterator = objectMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BusLoadDto> entry = iterator.next();
            if ((currentTime - entry.getValue().getTimestamp()) > ttlInMillis ||
                    entry.getValue().getCurrentFullness() > properties.getMaxFullnessPercent()) {
                log.debug("Removing old object: " + entry.getValue());
                iterator.remove();
            }
        }
    }
}