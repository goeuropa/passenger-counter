package pl.goeuropa.counter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArduinoDeviceSnapshotDto {

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("devices")
    private List<ScannedDeviceDto> devices;

    @JsonProperty("summary")
    private DeviceSummaryDto summary;
}
