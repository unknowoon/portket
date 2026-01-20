package com.portket.app.repository.jooq;

import com.portket.app.dto.Component;
import com.portket.app.dto.PortfolioOutput;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.portket.jooq.generated.Tables.*;
import static com.portket.app.constant.ComponentType.INSTRUMENT;
import static com.portket.app.constant.ComponentType.TAG;

@Repository
@RequiredArgsConstructor
public class PortfolioJooqQuery {

    private final DSLContext dsl;

    public List<PortfolioOutput> listPortfoliosWithName(String name, Long userId) {

        // 조회조건 1. 사용자 소유
        var mine = PORTFOLIOS.USER_ID.eq(userId);
        // 조회조건 2. 특정 포트폴리오
        var condition = StringUtils.isEmpty(name) ?
                mine :
                mine.and(PORTFOLIOS.NAME.eq(name));

        // 포폴 레코드
        Result<Record> portfolioRecords = dsl.select()
                .from(PORTFOLIOS)
                .where(condition)
                .fetch();

        if (portfolioRecords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> portfolioIds = portfolioRecords.getValues(PORTFOLIOS.ID);

        // 포폴태그 레코드
        Result<Record> tagRecords = dsl.select()
                .from(PORTFOLIO_TAGS)
                .join(TAGS).on(PORTFOLIO_TAGS.TAG_ID.eq(TAGS.ID))
                .where(PORTFOLIO_TAGS.PORTFOLIO_ID.in(portfolioIds))
                .fetch();

        Map<Long, List<Component>> tagMap = tagRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(PORTFOLIO_TAGS.PORTFOLIO_ID),
                        Collectors.mapping(
                                record -> Component.builder()
                                        .componentType(TAG)
                                        .name(record.get(TAGS.NAME))
                                        .weight(record.get(PORTFOLIO_TAGS.WEIGHT))
                                        .build(),
                                Collectors.toList()
                        )
                ));

        // 포폴자산 레코드
        Result<Record> instrumentRecords = dsl.select()
                .from(PORTFOLIO_INSTRUMENTS)
                .join(INSTRUMENTS).on(PORTFOLIO_INSTRUMENTS.INSTRUMENT_ID.eq(INSTRUMENTS.ID))
                .where(PORTFOLIO_INSTRUMENTS.PORTFOLIO_ID.in(portfolioIds))
                .fetch();

        Map<Long, List<Component>> instrumentMap = instrumentRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(PORTFOLIO_INSTRUMENTS.PORTFOLIO_ID),
                        Collectors.mapping(
                                record -> Component.builder()
                                        .componentType(INSTRUMENT)
                                        .name(record.get(INSTRUMENTS.NAME))
                                        .weight(record.get(PORTFOLIO_INSTRUMENTS.WEIGHT))
                                        .build(),
                                Collectors.toList()
                        )
                ));

        // 합치기
        Map<Long, List<Component>> componentMap = new HashMap<>();

        for (Long portfolioId : portfolioIds) {
            List<Component> componentList = new ArrayList<>();
            componentList.addAll(tagMap.getOrDefault(portfolioId, Collections.emptyList()));
            componentList.addAll(instrumentMap.getOrDefault(portfolioId, Collections.emptyList()));
            componentMap.put(portfolioId, componentList);
        }

        // 반환
        return portfolioRecords.stream()
                .map(record -> {

                    Long id = record.get(PORTFOLIOS.ID);
                    String pname = record.get(PORTFOLIOS.NAME);
                    List<Component> components = componentMap.get(id);
                    return PortfolioOutput.builder()
                            .id(id)
                            .name(pname)
                            .components(components)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
