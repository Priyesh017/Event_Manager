package in.guvi.event.management.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpeakerDto {

    private Long id;

    @NotBlank(message = "Speaker name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;

    private String photoUrl;
}
