package pl.goeuropa.counter.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class CapacitiesConfig {

    private final Map<Integer, List<String>> vehicleConfigs;

    public CapacitiesConfig(@Value("${api.capacities-file}") String filePath, ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource(filePath);
        this.vehicleConfigs = new ObjectMapper()
                .readValue(resource.getInputStream(), VehicleConfigLoader.class)
                .getVehicles();
        log.debug("Loaded from file {} vehicles", vehicleConfigs.size());
    }

    public HashMap<String, Integer> getVehiclesAndDivisors() {
        HashMap<String, Integer> vehicleMap = new HashMap<>();
        vehicleConfigs.forEach((divisor, value) -> value
                .forEach(vehicle -> vehicleMap.put(vehicle, divisor)));
        log.info("Mapped {} vehicles with config capacity", vehicleMap.size());
        return vehicleMap;
    }

    @Getter
    @Setter
    private static class VehicleConfigLoader {
        private Map<Integer, List<String>> vehicles;
    }
}
