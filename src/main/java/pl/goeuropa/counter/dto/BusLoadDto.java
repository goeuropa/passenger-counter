package pl.goeuropa.counter.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.goeuropa.counter.repository.PeopleCountRepository.CAPACITY_CONFIGS;

@Data
@NoArgsConstructor
public class BusLoadDto implements Serializable {

    private String vehicleName;
    private int currentCount;
    private float currentFullness;
    private long timestamp;

    public BusLoadDto(LogEntryDto logEntry) {
        parseMessage(logEntry.getMessage());
        this.currentFullness = getFullness();
        this.timestamp = logEntry.getTimestamp() / 1000;
    }

    public BusLoadDto(ArduinoDeviceSnapshotDto snapshot) {
        this.vehicleName = snapshot.getDeviceId();
        this.currentCount = snapshot.getSummary().getPhones();
        this.currentFullness = getFullness();
        this.timestamp = snapshot.getTimestamp() * 1000;
    }

    private void parseMessage(String message) {
        Pattern pattern = Pattern.compile("^\\s*(\\S+)\\s+([0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            this.vehicleName = matcher.group(1);
            this.currentCount = (int) Float.parseFloat(matcher.group(2));
        } else {
            this.vehicleName = "Unknown";
            this.currentCount = -1;
        }
    }

    private float getFullness() {
        int divisor = CAPACITY_CONFIGS.getOrDefault(this.vehicleName, -1);
        if (divisor != -1) return ((float) this.currentCount / divisor) * 100;
        else return Float.NaN;
    }
}
