package pl.goeuropa.counter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static pl.goeuropa.counter.repository.PeopleCountRepository.CAPACITY_CONFIGS;
import static pl.goeuropa.counter.repository.PeopleCountRepository.SUFFIX_STRIP_REGEX;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusLoadDto implements Serializable {

    private String vehicleName;
    private int currentCount;
    private float currentFullness;
    private long timestamp;

    public BusLoadDto(IncomeInfoDto dto) {
        this.vehicleName = dto.getVehiIdno().replaceAll(SUFFIX_STRIP_REGEX, "");
        this.currentCount = dto.getIncrPeople();
        this.currentFullness = getFullness();
        this.timestamp = getDateTime(dto.getTimeStr()).getTime();
    }

    private Date getDateTime(String timeStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(timeStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + timeStr, e);
        }
    }

    private float getFullness() {
        int divisor = CAPACITY_CONFIGS.get(this.vehicleName);
        return  ((float) this.currentCount / divisor) * 100;
    }
}
