package pl.goeuropa.counter.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String videoKey;
    private String videoAccount;
    private String keyAgency;
    private String capacitiesFile;
    private String apcSuffix;
    private String apc2Suffix;
    private String apc2VehiclePrefix;
    private List<String> excludedVehicleIds;
    private int staleTtlMinutes;
    private int maxFullnessPercent;
    private int pollRateMs;
    private String loginPath;
    private String peopleDetailPath;
    private int pageRecords;
}