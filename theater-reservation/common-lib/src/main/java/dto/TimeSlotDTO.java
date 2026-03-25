package dto;

import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeSlotDTO {
    @NonNull
    private LocalDate date;
    @NonNull
    private Instant startTime;
    private Duration duration;
    @NonNull
    private Instant endTime;


    @Override
    public String toString() {
        return "Date: " + getDate().toString() + " Start time: " + getStartTime().toString() + " End time: " + getEndTime().toString();
    }
}
