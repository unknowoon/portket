package com.portket.app.repository.jooq;

import com.portket.app.dto.InstrumentListInqInput;
import com.portket.app.dto.InstrumentListInqOutput;
import com.portket.app.dto.http.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.portket.jooq.generated.Tables.INSTRUMENTS;

@Repository
@RequiredArgsConstructor
public class InstrumentJooqQuery {

    private final DSLContext dsl;

    public PaginatedResponse<InstrumentListInqOutput> list(InstrumentListInqInput input) {

        int page = input.getPage();
        int size = input.getSize();
        int offset = (page - 1) * size;

        Condition condition = DSL.trueCondition();

        if (StringUtils.isNotBlank(input.getCountry())) {
            condition = condition.and(INSTRUMENTS.COUNTRY.eq(input.getCountry()));
        }
        if (StringUtils.isNotBlank(input.getMarket())) {
            condition = condition.and(INSTRUMENTS.MARKET.eq(input.getMarket()));
        }

        int totalCount = dsl.selectCount().from(INSTRUMENTS).where(condition).fetchOneInto(Integer.class);

        Result<Record> records = dsl.select()
                .from(INSTRUMENTS)
                .where(condition)
                .limit(size)
                .offset(offset)
                .fetch();

        List<InstrumentListInqOutput> data = records.stream()
                .map(record -> InstrumentListInqOutput.builder()
                        .id(record.get(INSTRUMENTS.ID))
                        .name(record.get(INSTRUMENTS.NAME))
                        .ticker(record.get(INSTRUMENTS.TICKER))
                        .country(record.get(INSTRUMENTS.COUNTRY))
                        .market(record.get(INSTRUMENTS.MARKET))
                        .currency(record.get(INSTRUMENTS.CURRENCY))
                        .currentPrice(record.get(INSTRUMENTS.CURRENT_PRICE))
                        .build()
                )
                .toList();

        int totalPage = (int) Math.ceil((double) totalCount / size);

        return PaginatedResponse.<InstrumentListInqOutput>builder().page(page).totalPage(totalPage).data(data).build();
    }
}
