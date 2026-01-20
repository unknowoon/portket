package com.portket.app.service;

import com.portket.app.domain.Instrument;
import com.portket.app.domain.InstrumentTag;
import com.portket.app.domain.Tag;
import com.portket.app.domain.User;
import com.portket.app.dto.InstrumentOutput;
import com.portket.app.dto.TagInput;
import com.portket.app.dto.TagOutput;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.repository.InstrumentRepository;
import com.portket.app.repository.InstrumentTagRepository;
import com.portket.app.repository.TagRepository;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentTagRepository instrumentTagRepository;
    private final InstrumentFinder instrumentFinder;

    @Transactional
    public void createTag(TagInput input) {

        tagRepository.findByName(input.getName()).ifPresent(tag -> {
            throw new IllegalArgumentException("이미 등록된 태그이름입니다");
        });

        List<Instrument> instruments = input.getInstruments().stream()
                .map(ticker -> instrumentRepository.findByTicker(ticker)
                        .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker))
                )
                .toList();

        User user = SecurityUtils.getCurrentUserOrThrow();

        Tag tag = Tag.builder()
                .name(input.getName())
                .user(user)
                .build();

        List<InstrumentTag> instrumentTags = instruments.stream()
                .map(instrument ->
                             InstrumentTag.builder()
                                    .tag(tag)
                                    .instrument(instrument)
                                    .build()

                ).collect(Collectors.toUnmodifiableList());


        tag.addInstrumentTags(instrumentTags);
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTagAll(TagInput input) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Tag tag = validOwner(input.getName(), user);
        tagRepository.delete(tag);
    }

    private Tag validOwner(String name, User user) {
        return tagRepository.findByUserAndName(user, name)
                .orElseThrow(() -> new IllegalArgumentException("Not exist tag"));
    }

    @Transactional
    public void deleteTag(String ticker, String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        validOwner(name, user);
        Instrument instrument = instrumentRepository.findByTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker));
        InstrumentTag it = instrumentTagRepository.findByInstrument(instrument)
                .orElseThrow(() -> new IllegalArgumentException("Not exist in Tag"));

        instrumentTagRepository.delete(it);
    }

    @Transactional(readOnly = true)
    public ListResponse<TagOutput> list() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        List<TagOutput> output = tagRepository.findAllByUser(user).stream()
                .map(tag -> TagOutput.builder()
                        .name(tag.getName())
                        .build()
                ).toList();

        return ListResponse.<TagOutput>builder().data(output).totalElements(output.size()).build();
    }

    @Transactional(readOnly = true)
    public ListResponse<InstrumentOutput> get(String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Tag tag = validOwner(name, user);
        List<InstrumentOutput> output = tag.getInstrumentTags().stream()
                .map(instrumentTag -> InstrumentOutput.builder()
                        .id(instrumentTag.getInstrument().getId())
                        .name(instrumentTag.getInstrument().getName())
                        .ticker(instrumentTag.getInstrument().getTicker())
                        .country(instrumentTag.getInstrument().getCountry())
                        .market(instrumentTag.getInstrument().getMarket())
                        .currency(instrumentTag.getInstrument().getCurrency().getCode())
                        .currentPrice(instrumentTag.getInstrument().getCurrentPrice())
                        .build()
                )
                .toList();

        return ListResponse.<InstrumentOutput>builder().data(output).totalElements(output.size()).build();
    }

    public void addTag(String ticker, String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Tag tag = validOwner(name, user);
        Instrument instrument = instrumentFinder.validTicker(ticker);

        InstrumentTag it = InstrumentTag.builder().tag(tag).instrument(instrument).build();
        instrumentTagRepository.save(it);
    }
}
