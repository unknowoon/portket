package com.portket.app.repository;

import com.portket.app.domain.*;
import com.portket.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("HoldingRepository 테스트")
class HoldingRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    private User testUser;
    private Portfolio testPortfolio;
    private Instrument appleStock;
    private Instrument googleStock;
    private Holding holding1;
    private Holding holding2;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        testUser = TestDataBuilder.createUser();
        entityManager.persist(testUser);
        
        testPortfolio = TestDataBuilder.createPortfolio(testUser);
        entityManager.persist(testPortfolio);
        
        appleStock = TestDataBuilder.createInstrument("AAPL", "Apple Inc.", new BigDecimal("150.00"));
        entityManager.persist(appleStock);
        
        googleStock = TestDataBuilder.createInstrument("GOOGL", "Google Inc.", new BigDecimal("2000.00"));
        entityManager.persist(googleStock);
        
        holding1 = TestDataBuilder.createHolding(testPortfolio, appleStock, new BigDecimal("10"), new BigDecimal("140.00"));
        entityManager.persist(holding1);
        
        holding2 = TestDataBuilder.createHolding(testPortfolio, googleStock, new BigDecimal("5"), new BigDecimal("1900.00"));
        entityManager.persist(holding2);
        
        entityManager.flush();
    }
    
    @Test
    @DisplayName("포트폴리오로 보유 자산 조회 성공")
    void findByPortfolio_Success() {
        // when
        List<Holding> holdings = holdingRepository.findByPortfolio(testPortfolio);
        
        // then
        assertThat(holdings).hasSize(2);
        assertThat(holdings).extracting("instrument.ticker")
                .containsExactlyInAnyOrder("AAPL", "GOOGL");
    }
    
    @Test
    @DisplayName("포트폴리오와 상품으로 보유 자산 조회 성공")
    void findByPortfolioAndInstrument_Success() {
        // when
        Optional<Holding> found = holdingRepository.findByPortfolioAndInstrument(testPortfolio, appleStock);
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualByComparingTo("10");
        assertThat(found.get().getAveragePrice()).isEqualByComparingTo("140.00");
    }
    
    @Test
    @DisplayName("포트폴리오와 상품으로 보유 자산 조회 실패 - 존재하지 않는 조합")
    void findByPortfolioAndInstrument_NotFound() {
        // given
        Instrument otherStock = TestDataBuilder.createInstrument("MSFT", "Microsoft", new BigDecimal("300.00"));
        entityManager.persist(otherStock);
        entityManager.flush();
        
        // when
        Optional<Holding> found = holdingRepository.findByPortfolioAndInstrument(testPortfolio, otherStock);
        
        // then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("여러 포트폴리오와 상품으로 보유 자산 조회 성공")
    void findByPortfolioInAndInstrument_Success() {
        // given
        Portfolio anotherPortfolio = Portfolio.builder()
                .name("Another Portfolio")
                .user(testUser)
                .build();
        entityManager.persist(anotherPortfolio);
        
        Holding holding3 = TestDataBuilder.createHolding(anotherPortfolio, appleStock, new BigDecimal("20"), new BigDecimal("145.00"));
        entityManager.persist(holding3);
        entityManager.flush();
        
        // when
        List<Holding> holdings = holdingRepository.findByPortfolioInAndInstrument(
                Arrays.asList(testPortfolio, anotherPortfolio), appleStock
        );
        
        // then
        assertThat(holdings).hasSize(2);
        assertThat(holdings).extracting("portfolio.name")
                .containsExactlyInAnyOrder("Test Portfolio", "Another Portfolio");
    }
    
    @Test
    @DisplayName("보유 자산 저장 성공")
    void save_Success() {
        // given
        Instrument newStock = TestDataBuilder.createInstrument("TSLA", "Tesla Inc.", new BigDecimal("700.00"));
        entityManager.persist(newStock);
        
        Holding newHolding = TestDataBuilder.createHolding(testPortfolio, newStock, new BigDecimal("3"), new BigDecimal("650.00"));
        
        // when
        Holding saved = holdingRepository.save(newHolding);
        entityManager.flush();
        
        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualByComparingTo("3");
        assertThat(saved.getAveragePrice()).isEqualByComparingTo("650.00");
    }
    
    @Test
    @DisplayName("보유 자산 수정 성공")
    void update_Success() {
        // given
        holding1.setQuantity(new BigDecimal("15"));
        holding1.setAveragePrice(new BigDecimal("145.00"));
        
        // when
        Holding saved = holdingRepository.save(holding1);
        entityManager.flush();
        
        // then
        assertThat(saved.getQuantity()).isEqualByComparingTo("15");
        assertThat(saved.getAveragePrice()).isEqualByComparingTo("145.00");
    }
    
    @Test
    @DisplayName("보유 자산 삭제 성공")
    void delete_Success() {
        // when
        holdingRepository.delete(holding1);
        entityManager.flush();
        
        // then
        List<Holding> holdings = holdingRepository.findByPortfolio(testPortfolio);
        assertThat(holdings).hasSize(1);
        assertThat(holdings.get(0).getInstrument().getTicker()).isEqualTo("GOOGL");
    }
}