# Portfolio CRUD 상태 분석

> 분석일: 2026-02-11

## 1. 프로젝트 구조

**백엔드 단독 프로젝트** (Spring Boot + Gradle, 프론트엔드 없음)

```
portket/
├── src/main/java/com/portket/
│   ├── app/
│   │   ├── controller/    # REST 엔드포인트 (9개)
│   │   ├── service/       # 비즈니스 로직 (8개)
│   │   ├── domain/        # JPA 엔티티 (10개)
│   │   ├── dto/           # 요청/응답 DTO (~25개)
│   │   ├── repository/    # JPA + JOOQ 쿼리
│   │   ├── finder/        # 조회 전용 서비스 (6개)
│   │   └── consumer/      # Kafka 컨슈머
│   ├── config/            # Security, Kafka, Swagger
│   ├── exception/         # 글로벌 예외 처리
│   ├── security/          # OAuth2 + JWT
│   └── util/              # 유틸리티
├── Dockerfile, Jenkinsfile, build.gradle
```

**기술 스택**: Spring Boot, JPA + JOOQ, PostgreSQL, OAuth2/JWT, Kafka, Docker

## 2. 포트폴리오 관련 파일 맵

| 레이어 | 파일 | 역할 |
|--------|------|------|
| Controller | `PortfolioController` | CRUD 엔드포인트 5개 |
| Service | `PortfolioService` | 생성/수정/삭제/목록/상세 |
| Finder | `PortfolioFinder` | 조회 로직 분리 (JPA+JOOQ) |
| Domain | `Portfolio`, `PortfolioTag`, `PortfolioInstrument` | 엔티티 |
| Repository | `PortfolioRepository` | JPA 기본 CRUD |
| JOOQ | `PortfolioJooqQuery` | 복합 목록 조회 |
| DTO | `PortfolioInput`, `PortfolioOutput`, `PortfolioComponentRequest`, `Component` | 입출력 |

## 3. CRUD 플로우 현황

### Create (`POST /api/portfolios`)
- 총 비중 100% 검증 → 사용자 인증 → 이름 중복 검증 → 컴포넌트(TAG/INSTRUMENT) 매핑 → 저장
- **정상 동작 구조**

### Read (`GET /api/portfolios`, `GET /api/portfolios/{name}`)
- 목록: JOOQ로 포트폴리오 + 태그 + 자산 3테이블 조인 조회
- 상세: JPA로 단건 조회 후 컴포넌트 매핑
- **정상 동작 구조**

### Update (`PUT /api/portfolios`)
- 비중 수정만 지원 (이름 변경 불가)
- ⚠️ **기존 컴포넌트를 clear하지 않고 addAll만 수행** → 중복 누적 버그 가능성
- `portfolio.getPortfolioInstruments().addAll(instruments)` — 기존 항목 제거 없이 추가만 함

### Delete (`DELETE /api/portfolios/{name}`)
- 소유자 검증 후 삭제, `CascadeType.ALL + orphanRemoval` 로 연관 엔티티 자동 삭제
- **정상 동작 구조**

## 4. 에러 처리 현황

### 글로벌 예외 핸들러 (`GlobalExceptionHandler`)
- `MethodArgumentNotValidException` → 400 (필드별 에러 메시지)
- `BizException` → ErrorCode 기반 커스텀 응답
- `Exception` (catch-all) → 500

### ErrorCode 정의
- 일반(10000번대): NOT_FOUND, INVALID_INPUT, INVALID_REQUEST, NOT_NULL
- 사용자(20000번대): USER_NOT_FOUND, INVALID_PASSWORD, USERNAME_ALREADY_EXISTS
- 서버(50000번대): INTERNAL_SERVER_ERROR
- ⚠️ **포트폴리오 전용 에러 코드 없음** (PORTFOLIO_NOT_FOUND, DUPLICATE_NAME 등 미정의)

### 문제점 요약

| 항목 | 상태 | 설명 |
|------|------|------|
| Update 중복 누적 | ⚠️ 버그 | 기존 컴포넌트 미삭제 후 추가만 수행 |
| IllegalArgumentException 미처리 | ⚠️ 누락 | GlobalExceptionHandler에 핸들러 없음 → 500 반환 |
| DTO 유효성 검증 | ⚠️ 미흡 | `@Valid` 미사용, Bean Validation 어노테이션 없음 |
| 이름 중복 검증 범위 | ⚠️ | `findByName`은 전체 사용자 대상 → 다른 유저의 포트폴리오 이름과도 충돌 |
| 포트폴리오 전용 ErrorCode | ❌ 없음 | BizException 대신 IllegalArgumentException 직접 throw |
| TODO 주석 | 📝 | "ErrorController로 바꿔야 합니다" 미완료 |
