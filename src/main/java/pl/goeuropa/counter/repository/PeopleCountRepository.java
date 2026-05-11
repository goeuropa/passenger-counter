package pl.goeuropa.counter.repository;

import lombok.Data;
import pl.goeuropa.counter.configs.CapacitiesConfig;
import pl.goeuropa.counter.dto.BusLoadDto;
import pl.goeuropa.counter.dto.ArduinoDeviceSnapshotDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PeopleCountRepository {

    public static final Map<String, Integer> CAPACITY_CONFIGS = new CapacitiesConfig()
            .getVehiclesAndDivisors();

    private static final PeopleCountRepository singleton = new PeopleCountRepository();

    private final Map<String, BusLoadDto> updatesAboutLoads = new ConcurrentHashMap<>();

    private final Map<String, ArduinoDeviceSnapshotDto> deviceSnapshots = new ConcurrentHashMap<>();

    private PeopleCountRepository() {
    }

    public static PeopleCountRepository getInstance() {
        return singleton;
    }

}
