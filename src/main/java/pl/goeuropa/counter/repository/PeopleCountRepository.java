package pl.goeuropa.counter.repository;

import lombok.Data;
import pl.goeuropa.counter.dto.BusLoadDto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PeopleCountRepository {

    public static Map<String, Integer> CAPACITY_CONFIGS;
    public static String SUFFIX_STRIP_REGEX;

    private static final PeopleCountRepository singleton = new PeopleCountRepository();

    private final Map<String, BusLoadDto> updatesAboutLoads = new ConcurrentHashMap<>();

    private String jSessionId = null;
    private String vehiIdnosParam = null;
    private Set<String> vehicleIds = new HashSet<>();

    private PeopleCountRepository() {
    }

    public static PeopleCountRepository getInstance() {
        return singleton;
    }
}