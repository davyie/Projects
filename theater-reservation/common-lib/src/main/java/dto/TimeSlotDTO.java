package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDTO {
    private LocalDate date;
    private Instant startTime;
    private Duration duration;
    private Instant endTime;


    @Override
    public String toString() {
        return "Date: " + getDate().toString() + " Start time: " + getStartTime().toString() + " End time: " + getEndTime().toString();
    }
}
