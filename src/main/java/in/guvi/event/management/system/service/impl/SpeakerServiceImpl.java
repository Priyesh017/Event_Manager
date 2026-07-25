package in.guvi.event.management.system.service.impl;

import in.guvi.event.management.system.dto.SpeakerDto;
import in.guvi.event.management.system.entity.Speaker;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.SpeakerRepository;
import in.guvi.event.management.system.service.SpeakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakerServiceImpl implements SpeakerService {

    private final SpeakerRepository speakerRepository;

    @Override
    public Speaker createSpeaker(SpeakerDto dto) {
        Speaker speaker = Speaker.builder()
            .name(dto.getName())
            .bio(dto.getBio())
            .photoUrl(dto.getPhotoUrl())
            .build();
        Speaker saved = speakerRepository.save(speaker);
        log.info("Speaker created: '{}'", saved.getName());
        return saved;
    }

    @Override
    public Speaker updateSpeaker(Long id, SpeakerDto dto) {
        Speaker speaker = findById(id);
        speaker.setName(dto.getName());
        speaker.setBio(dto.getBio());
        speaker.setPhotoUrl(dto.getPhotoUrl());
        return speakerRepository.save(speaker);
    }

    @Override
    public void deleteSpeaker(Long id) {
        if (!speakerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Speaker", id);
        }
        speakerRepository.deleteById(id);
        log.info("Speaker deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Speaker findById(Long id) {
        return speakerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Speaker", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Speaker> findAll() {
        return speakerRepository.findAllByOrderByNameAsc();
    }

    @Override
    public SpeakerDto toDto(Speaker speaker) {
        SpeakerDto dto = new SpeakerDto();
        dto.setId(speaker.getId());
        dto.setName(speaker.getName());
        dto.setBio(speaker.getBio());
        dto.setPhotoUrl(speaker.getPhotoUrl());
        return dto;
    }
}
