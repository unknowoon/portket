package com.portket.app.finder;

import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.dto.TransactionListInquiryInput;
import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.app.repository.jooq.TransactionJooqQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionFinder {

    private final TransactionJooqQuery jooqQuery;

    public PaginatedResponse<TransactionListInquiryOutput> list(TransactionListInquiryInput input, Long userId) {
        return jooqQuery.list(input, userId);
    }
}
