package pl.goeuropa.counter.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.goeuropa.counter.repository.PeopleCountRepository;

import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final ApiProperties properties;
    private final CapacitiesConfig capacitiesConfig;

    @PostConstruct
    public void initRepository() {
        PeopleCountRepository.CAPACITY_CONFIGS = capacitiesConfig.getVehiclesAndDivisors();
        PeopleCountRepository.SUFFIX_STRIP_REGEX =
                "(" + Pattern.quote(properties.getApc2Suffix()) +
                "|" + Pattern.quote(properties.getApcSuffix()) + ")$";
    }
}