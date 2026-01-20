package com.portket.app.repository;

import com.portket.app.domain.Instrument;
import com.portket.app.domain.InstrumentTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentTagRepository extends JpaRepository<InstrumentTag, Long> {
    Optional<InstrumentTag> findByInstrument(Instrument instrument);
}
