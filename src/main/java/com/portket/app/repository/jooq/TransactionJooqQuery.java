package com.portket.app.repository.jooq;

import com.portket.app.constant.TransactionType;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.dto.TransactionListInquiryInput;
import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.util.finance.FinancialCalculator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.portket.jooq.generated.Tables.INSTRUMENTS;
import static com.portket.jooq.generated.Tables.TRANSACTIONS;

@Repository
@RequiredArgsConstructor
public class TransactionJooqQuery {

    private final DSLContext dsl;

    public PaginatedResponse<TransactionListInquiryOutput> list(TransactionListInquiryInput input, Long userId) {

        int page = input.getPage();
        int size = input.getSize();
        int offset = (page - 1) * size;

        // 기본 조건: 사용자 ID
        Condition condition = TRANSACTIONS.USER_ID.eq(userId);

        // 거래일 범위 (startDate, endDate가 모두 있을 때만 적용)
        if (input.getStartDate() != null && input.getEndDate() != null) {
            condition = condition.and(TRANSACTIONS.TRANSACTION_DATE.between(input.getStartDate(), input.getEndDate()));
        } else if (input.getStartDate() != null) {
            condition = condition.and(TRANSACTIONS.TRANSACTION_DATE.greaterOrEqual(input.getStartDate()));
        } else if (input.getEndDate() != null) {
            condition = condition.and(TRANSACTIONS.TRANSACTION_DATE.lessOrEqual(input.getEndDate()));
        }

        // 거래 유형(type)이 입력된 경우 추가 조건 적용
        if (StringUtils.isNotBlank(input.getType())) {
            condition = condition.and(TRANSACTIONS.TRANSACTION_TYPE.eq(input.getType()));
        }

        // 종목(ticker)이 입력된 경우 추가 조건 적용
        if (StringUtils.isNotBlank(input.getTicker())) {
            condition = condition.and(INSTRUMENTS.TICKER.eq(input.getTicker()));
        }

        // 전체 건수 조회 (페이징 전)
        int totalCount = dsl.fetchCount(
                dsl.select()
                        .from(TRANSACTIONS)
                        .join(INSTRUMENTS)
                        .on(TRANSACTIONS.INSTRUMENT_ID.eq(INSTRUMENTS.ID))
                        .where(condition)
        );

        Result<Record> records = dsl.select()
                .from(TRANSACTIONS)
                .join(INSTRUMENTS)
                    .on(TRANSACTIONS.INSTRUMENT_ID.eq(INSTRUMENTS.ID))
                .where(condition)
                .orderBy(TRANSACTIONS.TRANSACTION_DATE.desc(), TRANSACTIONS.ID.desc())
                .limit(size)
                .offset(offset)
                .fetch();

        // 조회 결과 매핑
        List<TransactionListInquiryOutput> data = records.stream()
                .map(record -> {
                    Long id = record.get(TRANSACTIONS.ID, Long.class);
                    String instrumentName = record.get(INSTRUMENTS.NAME, String.class);
                    String ticker = record.get(INSTRUMENTS.TICKER, String.class);
                    String country = record.get(INSTRUMENTS.COUNTRY, String.class);
                    TransactionType type = record.get(TRANSACTIONS.TRANSACTION_TYPE, TransactionType.class);
                    BigDecimal quantity = record.get(TRANSACTIONS.QUANTITY, BigDecimal.class);
                    BigDecimal amount = record.get(TRANSACTIONS.AMOUNT, BigDecimal.class);
                    String currency = record.get(INSTRUMENTS.CURRENCY, String.class);
                    if (currency == null) currency = "USD";
                    BigDecimal averagePrice = FinancialCalculator.calculateAveragePrice(amount, quantity);

                    return TransactionListInquiryOutput.builder()
                            .id(id)
                            .instrumentName(instrumentName)
                            .ticker(ticker)
                            .country(country)
                            .type(type)
                            .quantity(quantity)
                            .amount(amount)
                            .averagePrice(averagePrice)
                            .currency(currency)
                            .transactionDate(record.get(TRANSACTIONS.TRANSACTION_DATE, LocalDate.class))
                            .build();
                })
                .collect(Collectors.toList());

        // 만약 조회된 데이터가 없으면 빈 리스트 처리
        if (data.isEmpty()) {
            data = Collections.emptyList();
        }

        int totalPage = (int) Math.ceil((double) totalCount / size);

        return PaginatedResponse.<TransactionListInquiryOutput>builder()
                .page(page)
                .size(size)
                .totalPage(totalPage)
                .totalElements(totalCount)
                .data(data)
                .build();
    }

    /**
     * 최근 거래 내역 조회 (대시보드용)
     */
    public List<TransactionListInquiryOutput> recent(Long userId, int limit) {
        Result<Record> records = dsl.select()
                .from(TRANSACTIONS)
                .join(INSTRUMENTS)
                    .on(TRANSACTIONS.INSTRUMENT_ID.eq(INSTRUMENTS.ID))
                .where(TRANSACTIONS.USER_ID.eq(userId))
                .orderBy(TRANSACTIONS.TRANSACTION_DATE.desc(), TRANSACTIONS.ID.desc())
                .limit(limit)
                .fetch();

        return records.stream()
                .map(record -> {
                    BigDecimal quantity = record.get(TRANSACTIONS.QUANTITY, BigDecimal.class);
                    BigDecimal amount = record.get(TRANSACTIONS.AMOUNT, BigDecimal.class);

                    return TransactionListInquiryOutput.builder()
                            .id(record.get(TRANSACTIONS.ID, Long.class))
                            .instrumentName(record.get(INSTRUMENTS.NAME, String.class))
                            .ticker(record.get(INSTRUMENTS.TICKER, String.class))
                            .country(record.get(INSTRUMENTS.COUNTRY, String.class))
                            .type(record.get(TRANSACTIONS.TRANSACTION_TYPE, TransactionType.class))
                            .quantity(quantity)
                            .amount(amount)
                            .averagePrice(FinancialCalculator.calculateAveragePrice(amount, quantity))
                            .currency(record.get(INSTRUMENTS.CURRENCY, String.class) != null ? record.get(INSTRUMENTS.CURRENCY, String.class) : "USD")
                            .transactionDate(record.get(TRANSACTIONS.TRANSACTION_DATE, LocalDate.class))
                            .build();
                })
                .collect(Collectors.toList());
    }
}
