# 물리 FK(Foreign Key) 사용 분석 및 개선 TODO

## 개요

본 문서는 JANDI IDE Backend 코드베이스의 **물리 FK 제약조건 사용 현황**과, 그로 인해 발생 중인 실제 결함 및 개선 과제를 기술한다.

**분석일:** 2026-08-08
**분석 범위:** `src/main/java/com/webproject/jandi_ide_backend/**` JPA 엔티티 12개 전체, git 히스토리 전체
**분석 방법:** 두 차례 독립 조사를 교차 검증하여 통합. 상충하던 git 커밋 참조는 재검증 후 확정(→ [조사 방법 및 교차 검증 기록](#부록-조사-방법-및-교차-검증-기록))

---

## 결론

- 코드 기준으로 물리 FK 생성 대상이 **15개** 존재한다 (`@ManyToOne` 11개 + `@ElementCollection` 소유자 관계 4개).
- 코드베이스 전체에 `ConstraintMode.NO_CONSTRAINT` 사용처가 **단 한 곳도 없다.** 따라서 "물리 FK를 사용하지 않는다"는 정책에는 부합하지 않는다.
- 과거 `ddl-auto=update` 로 약 3개월간 운영된 구간이 있고, **모든 FK 대상 연관관계가 그 구간 안에서 추가되었다.** Hibernate가 물리 FK를 실제로 생성했을 가능성이 매우 높다.
- **삭제 로직 결함 2건**(FK-001, FK-002)이 코드상 확인된다. 두 경우 모두 캐스케이드가 누락되어 있으며, 실물 FK가 **있으면 삭제 실패**로, **없으면 고아 데이터 발생**으로 나타난다. 어느 쪽이든 결함이므로 FK 존재 여부와 무관하게 수정 대상이다.
- 다만 이 프로젝트의 실질적 문제는 물리 FK의 존재 자체보다 **물리 FK와 논리 FK가 일관성 없이 혼재**하는 구조(FK-003)와 **스키마가 코드 밖에서 관리되지 않는 상태**(FK-004)다.
- 물리 FK 사용은 보편적 안티패턴이 아니다. 제거 여부는 데이터 무결성 전략, 배포 방식, 샤딩·서비스 분리 계획을 기준으로 결정해야 한다.

---

## 과제 요약

| ID | 항목 | 심각도 | 상태 |
|----|------|--------|------|
| FK-001 | 문제(Problem) 삭제 영구 불가 | 높음 | 미해결 |
| FK-002 | 기업(Company) 삭제 대부분 실패 | 높음 | 미해결 |
| FK-003 | 물리 FK / 논리 FK 정책 불일치 | 높음 | 미해결 |
| FK-004 | 스키마 마이그레이션 도구 부재 | 높음 | 미해결 |
| FK-005 | FK 위반 예외가 `catch (Exception)` 에 뭉개짐 | 중간 | 미해결 |
| FK-006 | FK 제거 시 인덱스 처리 (MySQL) | 중간 | 미해결 |
| FK-007 | 운영 환경 실제 설정 미확인 | 중간 | 미해결 |
| FK-008 | `problem_sets.company` 컬럼 네이밍 불일치 | 낮음 | 미해결 |

---

## 배경: 물리 FK가 DB에 생성된 경로

프로젝트에 **스키마 마이그레이션 도구가 없다.**

- `build.gradle` 에 Flyway / Liquibase 의존성 없음
- git 히스토리 전체에 `.sql` 파일 0건 (`git log --all --diff-filter=A --name-only` 로 확인)

즉 스키마는 전적으로 Hibernate가 생성했다.

### 확정된 타임라인

| 시점 | 커밋 | 내용 |
|------|------|------|
| 2025-04-10 | `d0b662d` | `ddl-auto=update` 최초 도입 |
| 2025-04-11 ~ 04-21 | `ed9254a` … `d28e5f3` | **FK 대상 연관관계가 전부 이 기간에 추가됨** |
| 2025-04-19 | `24d1624` | 이 시점까지 여전히 `update` |
| **2025-07-09** | **`a1801fb`** | **`update` → `validate` 전환** |
| 2026-01-06 | `c618c68`, `001b9d3` | `application.properties` 를 저장소에서 제거하고 `.example` 만 유지 (런타임 설정 외부화) |

**FK 생성 가능 구간: 2025-04-10 ~ 2025-07-09 (약 3개월).** 모든 연관관계(~04-21)가 이 구간에 포함되므로, 해당 기간에 애플리케이션이 대상 MySQL에 연결된 채 기동됐다면 FK는 생성되었다. 이후 `validate` 로 바꿔도 **이미 생성된 FK는 자동으로 제거되지 않는다.**

### 왜 코드만 봐서는 안 보이는가

Hibernate의 `validate` 는 테이블과 컬럼의 존재·타입만 검증하며 **FK 제약조건은 검증하지 않는다.** 애플리케이션이 정상 기동한다는 사실만으로 물리 FK의 존재 여부를 판단해서는 안 된다.

---

## 물리 FK 전체 목록 (15개)

### `@ManyToOne` 기반 (11개)

| 소유 엔티티.필드 | 참조 대상 | 컬럼 | 위치 |
|------------------|-----------|------|------|
| `UserTechStack.user` | `User` | `userId` | `UserTechStack.java:26` |
| `UserTechStack.techStack` | `TechStack` | `techStackId` | `UserTechStack.java:31` |
| `UserFavoriteCompany.user` | `User` | `userId` | `UserFavoriteCompany.java:27` |
| `UserFavoriteCompany.company` | `Company` | `companyId` | `UserFavoriteCompany.java:32` |
| `Project.owner` | `User` | `userId` | `Project.java:41` |
| `Solution.user` | `User` | `user_id` | `Solution.java:30` |
| `ProblemSet.company` | `Company` | `company` | `ProblemSet.java:37` |
| `ProblemSet.user` | `User` | `user_id` | `ProblemSet.java:60` |
| `TestCase.problem` | `Problem` | `problem_id` | `TestCase.java:24` |
| `JobPosting.company` | `Company` | `company_id` | `JobPosting.java:27` |
| `JobPostingSchedule.jobPosting` | `JobPosting` | `job_posting_id` | `JobPostingSchedule.java:25` |

`User`, `Company`, `JobPosting` 등에 선언된 `@OneToMany(mappedBy = ...)` 는 위 연관관계의 반대편이므로 **별도의 FK를 추가 생성하지 않는다.**

### `@ElementCollection` 소유자 관계 (4개)

Hibernate는 `@CollectionTable` 의 조인 컬럼에도 FK를 생성한다. 놓치기 쉬운 부분이다.

| 컬렉션 | 컬렉션 테이블 | 소유자 컬럼 | 위치 |
|--------|---------------|-------------|------|
| `Company.levels` | `company_problems` | `company_id` | `Company.java:31` |
| `Company.programmingLanguages` | `company_programming_languages` | `company_id` | `Company.java:41` |
| `Problem.tags` | `problem_tags` | `problem_id` | `Problem.java:40` |
| `ProblemSet.problems` | `problem_set_problems` | `problem_set_id` | `ProblemSet.java:42` |

### FK처럼 보이지만 물리 FK가 아닌 필드

혼동을 막기 위해 명시한다. 아래는 전부 **논리 FK**(애플리케이션이 무결성을 책임지는 참조)다.

| 위치 | 필드 | 실제 정체 |
|------|------|-----------|
| `Solution.java:35` | `problemId` | `Problem` 연관관계가 아닌 단순 `Integer` 컬럼 |
| `Solution.java:39` | `problemSetId` | `ProblemSet` 연관관계가 아닌 단순 `Long` 컬럼 |
| `ProblemSet.java:44` | `problem_set_problems.problem_id` | `List<Integer>` 의 **값 컬럼**. 같은 테이블의 `problem_set_id` 는 FK지만 `problem_id` 는 `Problem` 을 참조하는 FK가 아니다 |
| `ChatMessage.java` | `roomId` | MongoDB 문서의 문자열 필드. 관계형 DB의 FK가 아니다 |

→ 현재 모델은 **일부 관계만 DB가 참조 무결성을 보장하고, 나머지는 애플리케이션이 책임지는 혼합 구조**다. (FK-003)

### 실제 DB 검증 쿼리

위 목록은 코드와 git 히스토리 기반 추론이다. `validate` 가 FK를 검사하지 않으므로 **실물 확인이 반드시 필요하다.**

```sql
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME, ORDINAL_POSITION;
```

- [ ] 개발 / 스테이징 / 운영 DB **각각**에서 위 쿼리 실행
- [ ] 결과를 파일로 보관하고 위 15개 후보와 대조
- [ ] 환경 간 FK 구성이 다른지 확인 (환경마다 `ddl-auto` 이력이 달랐을 수 있음)

---

## FK-001: 문제(Problem) 삭제 영구 불가

### 심각도
**높음** — 관리자 기능이 현재 동작하지 않음

### 발생 위치
- `algorithm/problem/service/ProblemService.java:132` (`deleteProblem`)
- `algorithm/testCase/entity/TestCase.java:24`

### 상세 분석

`Problem` 엔티티에는 `TestCase` 로 향하는 `@OneToMany` 매핑이 **존재하지 않는다.** 따라서 JPA 캐스케이드가 동작하지 않는다.

반면 `test_cases.problem_id` 는 `nullable = false` 인 물리 FK다.

```java
// TestCase.java:23-25
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "problem_id", nullable = false)
private Problem problem;
```

결과적으로 테스트케이스가 1개라도 등록된 문제는 삭제 시 FK 제약조건 위반이 발생하여 **영구히 삭제할 수 없다.**

**실물 FK가 없는 경우에도 결함이다.** 이때는 삭제가 성공하는 대신 `test_cases` 에 고아 행이 남고, 아무도 이를 감지하지 못한다. 즉 FK 존재 여부와 무관하게 수정이 필요하다.

참고: `problem_tags` 는 `@ElementCollection` 이므로 Hibernate가 부모 삭제 전에 자동으로 제거한다. 실제 차단 원인은 `test_cases` 단독이다.

### 해결 방안 (택 1)

- [ ] (A) `Problem` 에 `@OneToMany(mappedBy = "problem", cascade = ALL, orphanRemoval = true)` 추가 — 물리 FK 유지 시
- [ ] (B) 삭제 전 `testCaseRepository.deleteByProblemId(id)` 명시적 호출 — 애플리케이션이 삭제 순서를 소유
- [ ] (C) FK 제거 후 논리 FK로 전환 (FK-003 과 함께 처리)

---

## FK-002: 기업(Company) 삭제 대부분 실패

### 심각도
**높음** — 관리자 기능이 현재 동작하지 않음

### 발생 위치
- `company/service/CompanyService.java:108` (`deleteCompany`)
- `company/entity/Company.java:46`

### 상세 분석

`Company` 는 `jobPostings` 에만 캐스케이드가 설정되어 있다.

```java
// Company.java:46-47
@OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
private List<JobPosting> jobPostings = new ArrayList<>();
```

그러나 `companies` 를 물리 FK로 참조하는 테이블은 총 5개다:

| 참조 테이블 | 캐스케이드 | 삭제 차단 |
|-------------|-----------|-----------|
| `job_postings` | 있음 | 차단 안 됨 |
| `company_problems` (`@ElementCollection`) | Hibernate 자동 처리 | 차단 안 됨 |
| `company_programming_languages` (`@ElementCollection`) | Hibernate 자동 처리 | 차단 안 됨 |
| `UserFavoriteCompany` | **없음** | **차단됨** |
| `problem_sets` | **없음** | **차단됨** |

즉 **어떤 사용자든 해당 기업을 관심기업으로 등록해 두었다면 그 기업은 삭제할 수 없다.** 문제집이 해당 기업을 참조하는 경우도 동일하다.

FK-001과 마찬가지로, 실물 FK가 없다면 삭제는 성공하되 `UserFavoriteCompany` 와 `problem_sets` 에 존재하지 않는 기업을 가리키는 고아 행이 남는다.

### 해결 방안

- [ ] 삭제 전 `UserFavoriteCompany` 정리 로직 추가
- [ ] `problem_sets.company` 의 삭제 정책 결정 (SET NULL / 삭제 차단을 의도된 동작으로 명시 / 논리 FK 전환)

---

## FK-003: 물리 FK / 논리 FK 정책 불일치 (핵심 설계 문제)

### 심각도
**높음** — 물리 FK 존재 자체보다 실질적으로 더 위험

### 상세 분석

이 프로젝트는 **이미 논리 FK를 사용하고 있으나, 일관성이 없다.** (위 "FK처럼 보이지만 물리 FK가 아닌 필드" 표 참조)

가장 극명한 사례는 `solutions` 테이블이다. **하나의 테이블 안에서 두 정책이 공존한다:**

| 컬럼 | 정책 |
|------|------|
| `solutions.user_id` | 물리 FK (DB가 무결성 보장) |
| `solutions.problem_id` | 논리 FK (제약 없음) |
| `solutions.problem_set_id` | 논리 FK (제약 없음) |

### 그 결과: `problems` 는 최악의 조합

- `test_cases` 쪽 물리 FK 때문에 **삭제가 차단된다** (FK-001)
- 그럼에도 삭제에 성공하는 경로가 생기면, `solutions.problem_id` 와 `problem_set_problems.problem_id` 는 **아무도 감지하지 못하는 고아 데이터**가 된다

→ 제약조건의 이득(정합성 보장)도, 논리 FK의 이득(유연성)도 얻지 못하는 상태다.

### 판단 시 유의점

"물리 FK를 쓰면 안 된다"는 절대 규칙이 아니라 규모·운영 조건에서 파생된 관례다. 주된 근거는 무중단 스키마 변경, 대량 마이그레이션, 샤딩 대응, 서비스 분리, 삭제 정책을 애플리케이션이 소유하는 설계다. 팀에 따라 물리 FK를 의도적으로 유지하기도 한다.

**이 프로젝트에서 실제로 아픈 지점은 FK의 존재 자체가 아니라 정책 불일치와 캐스케이드 누락이다.** FK-001, FK-002 는 물리 FK를 전부 걷어내도 사라지지 않고 고아 데이터 문제로 형태만 바뀐다.

### 해결 방안

- [ ] 팀 또는 회사의 물리 FK 정책과 **그 근거**를 확인한다
- [ ] 정책을 하나로 결정한다:
  - **(A) 물리 FK 유지** + 캐스케이드 / `ON DELETE` 정비
  - **(B) 물리 FK 제거** + 논리 FK 일원화 + 애플리케이션 레벨 정합성 보장
- [ ] 데이터 무결성의 **책임 주체**를 문서에 명시한다
- [ ] (B) 선택 시: 삭제 시 참조 정리 책임을 서비스 레이어에 명시하고, 고아 데이터 탐지 배치 또는 검증 쿼리를 마련한다
- [ ] (B) 선택 시: JPA 매핑에 `@ForeignKey(ConstraintMode.NO_CONSTRAINT)` 를 명시해 향후 DDL 생성 시 FK가 다시 만들어지지 않도록 한다
- [ ] (B) 선택 시: **애노테이션 변경만으로 기존 FK는 삭제되지 않는다.** 기존 제약조건은 버전 관리된 마이그레이션으로 별도 제거한다
- [ ] (B) 선택 시: **FK가 막아주던 고아 데이터가 이미 존재하는지 제거 전에 먼저 검사한다**
- [ ] 결정된 정책을 문서화하여 이후 엔티티 추가 시 흔들리지 않도록 고정한다

---

## FK-004: 스키마 마이그레이션 도구 부재

### 심각도
**높음** — FK-001~003 의 근본 원인이자 모든 개선 작업의 선행 과제

### 상세 분석

- 마이그레이션 도구 없음 (Flyway / Liquibase 미도입)
- 히스토리 전체에 `.sql` 파일 없음
- 운영 스키마는 과거 `ddl-auto=update` 가 만든 결과물이며, **코드 어디에도 기록되어 있지 않다**
- 현재 `validate` 는 FK를 검증하지 않으므로, 코드와 실제 DB 제약조건이 불일치해도 애플리케이션은 정상 기동한다

FK를 제거하든 유지하든, 스키마를 코드 밖에서 버전 관리하지 않으면 안전하게 변경할 수 없다.

### 해결 방안

- [ ] Flyway 도입 (또는 Liquibase)
- [ ] 운영 DB 현재 스키마를 baseline 마이그레이션으로 추출 (`mysqldump --no-data`)
- [ ] 이후 스키마 변경은 전부 마이그레이션 스크립트로 관리
- [ ] 스키마 변경 이력을 코드와 함께 리뷰 대상에 포함

---

## FK-005: FK 위반 예외가 `catch (Exception)` 에 뭉개짐

### 심각도
**중간** — FK-001, FK-002 가 지금까지 발견되지 않은 이유

### 발생 위치
- `algorithm/problem/service/ProblemService.java:135-139`
- `company/service/CompanyService.java:112-116`
- 그 외 대부분의 `delete*` 메서드에서 동일 패턴 반복

### 상세 분석

```java
try{
    problemRepository.delete(problem);
} catch (Exception e) {
    throw new CustomException(CustomErrorCodes.DB_OPERATION_FAILED);
}
```

FK 제약조건 위반(`SQLIntegrityConstraintViolationException`)이 발생해도 원인이 응답에도, 로그에도 남지 않는다. `DB_OPERATION_FAILED` 라는 동일한 메시지로만 관측되어 원인 파악이 불가능하다.

### 해결 방안

- [ ] 최소한 `log.error("...", e)` 로 원인 예외를 기록
- [ ] 제약조건 위반은 별도 에러 코드로 구분하여 "참조 중인 데이터가 있어 삭제할 수 없음"을 호출자에게 전달

---

## FK-006: FK 제거 시 인덱스 처리 (MySQL)

### 심각도
**중간** — 제거를 선택하는 경우에만 해당

### 상세 분석

MySQL(InnoDB)은 FK 제약조건 생성 시 해당 컬럼에 인덱스가 없으면 **자동으로 인덱스를 만든다.** 따라서 현재 15개 FK 컬럼에는 (명시적으로 만들지 않았더라도) 인덱스가 존재할 가능성이 높다.

**중요: FK를 제거해도 이 인덱스는 자동으로 사라지지 않는다.** 인덱스는 남고, 지우려면 별도로 `DROP INDEX` 해야 한다. MySQL 8.0 레퍼런스 기준:

```sql
-- FK와 인덱스를 함께 지우려면 둘 다 명시해야 한다
ALTER TABLE tbl DROP FOREIGN KEY fk_name, DROP INDEX idx_name;
```

> "Dropping an index required by a foreign key constraint is not permitted, even with `foreign_key_checks=0`. The foreign key constraint must be removed before dropping the index."
> — MySQL 8.0 Reference Manual, *Server System Variables* / *InnoDB Online DDL Operations*

즉 **FK 제거만으로 성능이 곧바로 나빠지지는 않는다.** 실제 위험은 두 가지다:

1. FK를 제거한 뒤 남은 인덱스를 "이제 안 쓰는 것"으로 오인하고 함께 지우는 경우 → `solutions.user_id`, `test_cases.problem_id`, `job_postings.company_id` 처럼 조인 경로에 쓰이는 컬럼이면 성능이 급격히 나빠진다
2. 반대로 방치하면 의도가 불분명한 자동 생성 이름(`FKxxxxxxxx`)의 인덱스가 남아 이후 스키마 관리가 어려워진다

### 해결 방안

- [ ] FK 제거 전, 각 FK 컬럼의 **현재 인덱스 존재 여부와 이름을 확인**한다 (`SHOW INDEX FROM tbl`)
- [ ] 조회·조인에 필요한 인덱스는 **의도가 드러나는 이름으로 명시적으로 재정의**하고, FK와 무관하게 독립적으로 유지한다
- [ ] 남은 자동 생성 인덱스를 지울지 유지할지 컬럼별로 판단한다 (**조인에 쓰이면 반드시 유지**)
- [ ] 제거 전후 주요 쿼리의 실행 계획(`EXPLAIN`)을 비교한다

---

## FK-007: 운영 환경 실제 설정 미확인

### 심각도
**중간** — 분석의 전제를 확정하기 위해 필요

### 상세 분석

- `2026-01-06` (`001b9d3`) 이후 `application.properties` 는 저장소에서 제외되었다 (`.gitignore`)
- 실제 실행 설정은 저장소 밖의 `application.properties` 또는 컨테이너 외부 마운트 경로(`/app/config/application.properties`)를 사용한다
- 따라서 **저장소만으로는 운영 환경의 최종 `ddl-auto` 값을 확인할 수 없다**
- 저장소에 남은 것은 `application.properties.example:19` 의 `validate` 뿐이며, 이는 예시 파일이다

### 해결 방안

- [ ] 운영 / 스테이징 / 개발 각 환경의 실제 `ddl-auto` 값을 확인한다
- [ ] 운영에 `update` 나 `create` 가 남아 있지 않은지 반드시 점검한다 (남아 있다면 그 자체로 별도의 심각한 위험)

---

## FK-008: `problem_sets.company` 컬럼 네이밍 불일치

### 심각도
**낮음**

### 발생 위치
- `algorithm/problemSet/entity/ProblemSet.java:37`

### 상세 분석

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "company")   // 다른 곳은 모두 company_id
private Company company;
```

FK 컬럼임에도 `_id` 접미사가 없어 `job_postings.company_id`, `company_problems.company_id` 등 다른 참조와 규칙이 어긋난다.

참고로 프로젝트 전반에 테이블/컬럼 네이밍 컨벤션이 혼재한다 — snake_case(`job_postings`, `problem_sets`, `test_cases`)와 PascalCase(`UserTechStack`, `UserFavoriteCompany`, `Project`, `TechStack`)가 섞여 있고, 컬럼도 `user_id` 와 `userId` 가 공존한다.

### 해결 방안

- [ ] FK-004(마이그레이션 도구 도입) 이후 마이그레이션으로 일괄 정리
- [ ] 컬럼명 변경은 운영 DB 변경을 수반하므로 **단독 진행 금지**

---

## 삭제 경로별 점검 목록

물리 FK 유지 여부와 **무관하게** 각 삭제 경로를 테스트해야 한다.

> JPA의 `CascadeType.ALL` 과 `orphanRemoval` 은 ORM이 엔티티 작업을 전파하는 설정이며, DB의 `ON DELETE CASCADE` 와 **동일한 기능이 아니다.** ORM을 우회하는 벌크 삭제나 직접 SQL에는 적용되지 않는다.

- [ ] **`Company` 삭제** → `UserFavoriteCompany`, `ProblemSet`, `JobPosting`, `company_problems`, `company_programming_languages` 처리 정책 (FK-002)
- [ ] **`Problem` 삭제** → `TestCase`, `Solution.problemId`, `ProblemSet.problems`, `problem_tags` 에 남는 참조 값 처리 정책 (FK-001)
- [ ] **`User` 삭제** → `UserTechStack`, `UserFavoriteCompany`, `Project`, `Solution`, `ProblemSet` 의 처리 **순서** (`UserService.java:382`)
- [ ] **`JobPosting` 삭제** → `JobPostingSchedule` 처리 방식
- [ ] **`ProblemSet` 삭제** → `problem_set_problems`, `Solution.problemSetId` 처리 방식 (`ProblemSetService.java:271`)
- [ ] 동시성 상황에서 고아 데이터가 생기지 않는지 통합 테스트 추가

---

## 권장 진행 순서

1. **FK-007** — 각 환경의 실제 `ddl-auto` 설정 확인 (가장 저렴하고, 운영에 `update` 가 남아 있으면 최우선 대응 필요)
2. **실물 FK 검증** — `information_schema` 쿼리를 환경별로 실행하여 추론이 아닌 실제 FK 목록 확정
3. **FK-004** — Flyway 도입 및 현재 스키마 baseline 확보 (이후 모든 스키마 변경의 전제)
4. **FK-003** — 정책 결정 (물리 FK 유지 vs 논리 FK 일원화). **이후 작업의 방향이 여기서 갈린다**
5. **FK-001, FK-002** — 결정된 정책에 따라 삭제 로직 수정 + 삭제 경로별 테스트
6. **FK-006** — 제거를 선택한 경우, FK 제거 후 남는 인덱스를 컬럼별로 유지/삭제 판단
7. **FK-005** — 예외 처리 개선 (다른 항목과 독립적으로 언제든 진행 가능)
8. **FK-008** — 네이밍 정리 (후순위, 마이그레이션 체계 확립 후)

---

## 부록: 조사 방법 및 교차 검증 기록

본 문서는 두 차례의 독립 조사 결과를 통합한 것이다. 두 조사는 FK 15개 목록, `NO_CONSTRAINT` 부재, `validate` 가 FK를 검증하지 않는다는 점에서 **일치했다.**

상충한 부분은 `ddl-auto` 변경 커밋 참조였으며, 재검증하여 확정했다.

**원인:** `git log -S"ddl-auto"` 는 문자열의 **등장 횟수가 변한** 커밋만 잡아낸다. 값만 `update` → `validate` 로 바뀐 커밋은 횟수가 동일하므로 검색되지 않는다. 이 때문에 한쪽 조사가 전환 시점을 놓쳤다.

**확정에 사용한 명령:**

```bash
# 값 단위로 검색해야 전환 시점이 잡힌다
git log --all --reverse -S"ddl-auto=validate" --format="%h %ad %s" --date=short -- '*application*properties*'
git log --all --reverse -S"ddl-auto=update"   --format="%h %ad %s" --date=short -- '*application*properties*'

# FK 대상 연관관계가 추가된 시점
git log --all --reverse -S"@JoinColumn" --format="%h %ad %s" --date=short -- 'src/main/java/**/entity/*.java'
```

이 결과로 전환 커밋이 `a1801fb` (2025-07-09)임을 확정했고, FK 생성 가능 구간과 연관관계 추가 시점(2025-04-11 ~ 04-21)이 완전히 겹친다는 사실을 확인했다.
