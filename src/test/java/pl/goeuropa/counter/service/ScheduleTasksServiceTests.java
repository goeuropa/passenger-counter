package pl.goeuropa.counter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.goeuropa.counter.repository.PeopleCountRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleTasksServiceTests {

    @Autowired
    private ScheduleTasksService service;

    private final PeopleCountRepository repo = PeopleCountRepository.getInstance();

    @BeforeEach
    void clearRepo() {
        repo.getVehicleIds().clear();
        repo.setVehiIdnosParam(null);
    }

    @Test
    void shouldAssignCorrectSuffixesByVehiclePrefix() {
        service.extractIdsAndPersist(List.of(
                Map.of("vehicleName", "ME-1"),
                Map.of("vehicleName", "BUS-5")
        ));

        assertTrue(repo.getVehicleIds().contains("ME-1-APC2"),
                "ME-prefix vehicle should get -APC2 suffix");
        assertTrue(repo.getVehicleIds().contains("BUS-5-APC"),
                "Non-ME vehicle should get -APC suffix");
    }

    @Test
    void shouldRemoveExcludedVehicleIds() {
        service.extractIdsAndPersist(List.of(
                Map.of("vehicleName", "ME-8"),
                Map.of("vehicleName", "ME-1")
        ));

        // ME-8 → ME-8-APC2, which is in api.excluded-vehicle-ids
        assertFalse(repo.getVehicleIds().contains("ME-8-APC2"),
                "ME-8-APC2 should be excluded per configuration");
        assertTrue(repo.getVehicleIds().contains("ME-1-APC2"),
                "ME-1-APC2 should remain");
    }

    @Test
    void shouldStripSuffixesFromVehicleNamesViaConfiguredRegex() {
        assertNotNull(PeopleCountRepository.SUFFIX_STRIP_REGEX,
                "SUFFIX_STRIP_REGEX must be initialized by AppConfig");

        String regex = PeopleCountRepository.SUFFIX_STRIP_REGEX;
        assertEquals("ME-1", "ME-1-APC2".replaceAll(regex, ""));
        assertEquals("BUS-5", "BUS-5-APC".replaceAll(regex, ""));
        assertEquals("TR-3", "TR-3".replaceAll(regex, ""),
                "Vehicle without suffix should be unchanged");
    }
}