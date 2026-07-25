package in.guvi.event.management.system.dto;

import in.guvi.event.management.system.enums.EventCategory;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EventSearchDto {

    private String keyword;
    private EventCategory category;
    private String location;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
}
