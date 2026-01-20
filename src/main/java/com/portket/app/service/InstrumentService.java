package com.portket.app.service;

import com.portket.app.dto.*;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.finder.InstrumentFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentFinder instrumentFinder;

    @Transactional(readOnly = true)
    public PaginatedResponse<InstrumentListInqOutput> list(InstrumentListInqInput input) {
        return instrumentFinder.list(input);
    }

    public InstrumentOutput get(String ticker) {
        return instrumentFinder.get(ticker);
    }

    public ListResponse<InstrumentOutput> getLike(String name) {
        List<InstrumentOutput> output = instrumentFinder.getLike(name);

        return ListResponse.<InstrumentOutput>builder().totalElements(output.size()).data(output).build();
    }
}
