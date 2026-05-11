package pl.goeuropa.counter.configs;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class CapacitiesConfig {
    private final Map<Integer, List<String>> vehicleConfigs;

    public CapacitiesConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.vehicleConfigs = objectMapper.readValue(Paths.get("/app/resources/vehicleCapacities.json").toFile(),
                            VehicleConfigLoader.class)
                    .getVehicles();
            log.info("Loaded from file {} vehicles", vehicleConfigs.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load vehicle config file", e);
        }
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
