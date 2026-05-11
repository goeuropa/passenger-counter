package pl.goeuropa.counter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceSummaryDto {

    @JsonProperty("total_devices")
    private int totalDevices;

    @JsonProperty("phones")
    private int phones;

    @JsonProperty("computers")
    private int computers;

    @JsonProperty("wearables")
    private int wearables;

    @JsonProperty("beacons")
    private int beacons;

    @JsonProperty("other")
    private int other;
}
