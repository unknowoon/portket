package com.portket.app.finder;

import com.portket.app.domain.Instrument;
import com.portket.app.dto.InstrumentListInqInput;
import com.portket.app.dto.InstrumentListInqOutput;
import com.portket.app.dto.InstrumentOutput;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.repository.InstrumentRepository;
import com.portket.app.repository.jooq.InstrumentJooqQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentFinder {
    private final InstrumentRepository instrumentRepository;
    private final InstrumentJooqQuery instrumentJooqQuery;

    /**
     * 해당하는 이름이 있는지 검증하는 로직이 있음
     * @param name
     * @return Instrument
     */
    public Instrument validName(String name) {
        return instrumentRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + name));
    }

    /**
     * 해당하는 티커가 있는지 검증하는 로직이 있음
     * @param ticker
     * @return Instrument
     */
    public Instrument validTicker(String ticker) {
        return instrumentRepository.findByTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + ticker));
    }

    /**
     * 입력값 동적 페이지네이션 조회하는 로직이 있음
     * @param input
     * @return PaginatedResponse<InstrumentOutput>
     */
    public PaginatedResponse<InstrumentListInqOutput> list(InstrumentListInqInput input) {
        return instrumentJooqQuery.list(input);
    }

    /**
     * ticker를 엔티티 정보로 변환하는 함수
     * @param ticker
     * @return InstrumentOutput
     */
    public InstrumentOutput get(String ticker) {
        Instrument instrument = this.validTicker(ticker);
        return InstrumentOutput.builder()
                .id(instrument.getId())
                .name(instrument.getName())
                .ticker(instrument.getTicker())
                .country(instrument.getCountry())
                .market(instrument.getMarket())
                .currency(instrument.getCurrency().getCode())
                .currentPrice(instrument.getCurrentPrice())
                .build();
    }


    /**
     * name를 엔티티 정보로 변환하는 함수
     * @param name
     * @return InstrumentOutput
     */
    public List<InstrumentOutput> getLike(String name) {

        List<Instrument> instruments = instrumentRepository.findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(name, name);

        return instruments.stream()
                .map(instrument -> InstrumentOutput.builder()
                        .id(instrument.getId())
                        .name(instrument.getName())
                        .ticker(instrument.getTicker())
                        .country(instrument.getCountry())
                        .market(instrument.getMarket())
                        .currency(instrument.getCurrency().getCode())
                        .currentPrice(instrument.getCurrentPrice())
                        .build())
                .toList();
    }
}
