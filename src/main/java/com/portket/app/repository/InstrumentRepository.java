package com.portket.app.repository;

import com.portket.app.domain.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    Optional<Instrument> findByName(String name);
    Optional<Instrument> findByTicker(String ticker);
    List<Instrument> findByNameContainingIgnoreCaseOrTickerContainingIgnoreCase(String name, String ticker);
}
