package pl.goeuropa.counter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScannedDeviceDto {

    @JsonProperty("mac")
    private String mac;

    @JsonProperty("rssi")
    private int rssi;

    @JsonProperty("distance")
    private double distance;

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;

    @JsonProperty("address_type")
    private String addressType;

    @JsonProperty("seen_count")
    private int seenCount;

    @JsonProperty("first_seen")
    private long firstSeen;

    @JsonProperty("last_seen")
    private long lastSeen;
}
