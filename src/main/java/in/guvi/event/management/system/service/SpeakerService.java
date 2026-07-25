package in.guvi.event.management.system.service;

import in.guvi.event.management.system.dto.SpeakerDto;
import in.guvi.event.management.system.entity.Speaker;

import java.util.List;

public interface SpeakerService {

    Speaker createSpeaker(SpeakerDto dto);

    Speaker updateSpeaker(Long id, SpeakerDto dto);

    void deleteSpeaker(Long id);

    Speaker findById(Long id);

    List<Speaker> findAll();

    SpeakerDto toDto(Speaker speaker);
}
