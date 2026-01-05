# 파일 확장자 차단 관리 시스템

보안상 위험한 파일 확장자(exe, sh 등)를 관리하는 웹 애플리케이션입니다.

## 테스트 환경

| 항목 | 버전 |
|------|------|
| OS | Windows 10 |
| Browser | Chrome |
| Java | 23 |
| Node.js | 22 |

## 기술 스택

- **Backend**: Spring Boot 3.2.1 + JPA + H2 (파일 기반 DB)
- **Frontend**: Vue.js 3 + Vite + Axios

## 주요 기능

### 고정 확장자 관리
- 7개 기본 확장자: bat, cmd, com, cpl, exe, scr, js
- 체크박스로 활성화/비활성화 토글
- 상태는 DB에 저장되어 새로고침 시에도 유지

### 커스텀 확장자 관리
- 사용자 정의 확장자 추가/삭제
- 최대 200개, 각 20자 제한
- 태그 형태로 표시, X 버튼으로 삭제

### 입력값 검증
- 소문자 자동 변환 (SH → sh)
- 앞뒤 공백 제거
- 앞 점(.) 자동 제거 (.exe → exe)
- 영문/숫자만 허용, 특수문자 거부
- 중복 체크 (고정 확장자와도 비교)
- **경로 문자 차단** (/, \\, ..) - 보안 강화

## 실행 방법

### 1. Backend 실행

```bash
cd backend
./mvnw spring-boot:run
```

Windows:
```bash
cd backend
mvnw.cmd spring-boot:run
```

서버 실행 후: http://localhost:8080

### 2. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

개발 서버: http://localhost:5173

## API 명세

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /api/extensions | 전체 확장자 목록 조회 |
| PATCH | /api/extensions/fixed/{ext} | 고정 확장자 활성화 토글 |
| POST | /api/extensions/custom | 커스텀 확장자 추가 |
| DELETE | /api/extensions/custom/{ext} | 커스텀 확장자 삭제 |

## 프로젝트 구조

```
flow-file-extension-blocker/
├── backend/
│   ├── src/main/java/com/flow/blocker/
│   │   ├── controller/    # REST API 컨트롤러
│   │   ├── service/       # 비즈니스 로직
│   │   ├── repository/    # 데이터 액세스
│   │   ├── domain/        # 엔티티
│   │   ├── dto/           # 요청/응답 DTO
│   │   ├── exception/     # 예외 처리
│   │   └── config/        # CORS 설정
│   └── src/main/resources/
│       ├── application.yml
│       └── data.sql       # 초기 데이터
│   └── src/test/java/com/flow/blocker/
│       ├── service/
│       │   ├── ExtensionServiceTest.java      # 서비스 단위 테스트 (31개)
│       │   └── ExtensionServiceConcurrencyTest.java  # 동시성 테스트 (4개)
│       └── controller/
│           └── ExtensionControllerTest.java   # API 통합 테스트 (14개)
│
└── frontend/
    └── src/
        ├── api/           # API 연동
        ├── components/    # Vue 컴포넌트
        └── App.vue        # 메인 앱
```

## 설계 고려사항

1. **단순한 DB 구조**: 파일 기반 H2 DB로 별도 설치 없이 동작
2. **입력값 정규화**: 다양한 형태의 입력을 일관되게 처리
3. **중복 방지**: 고정 확장자와 커스텀 확장자 간 중복도 체크
4. **즉각적인 피드백**: 에러 메시지와 로딩 상태 표시
5. **직관적인 UX**: 현재 등록 개수 표시, 태그 형태의 삭제 UI
6. **보안 강화**: 경로 탐색 공격 방지 (/, \\, .. 차단)
7. **데이터 영속성**: 서버 재시작 후에도 데이터 유지 (`sql.init.mode: embedded`)

## JUnit 테스트

### 테스트 실행
```bash
cd backend
./mvnw test
```

Windows:
```bash
cd backend
mvnw.cmd test
```

### 테스트 구성 (총 49개)

#### ExtensionServiceTest (31개)
- 전체 확장자 조회
- 고정 확장자 토글
- 커스텀 확장자 추가 (정상/실패 케이스)
- 경로 문자 차단 (보안)
- 커스텀 확장자 삭제
- 개수 제한 (200개)

#### ExtensionControllerTest (14개)
- GET /api/extensions
- PATCH /api/extensions/fixed/{extension}
- POST /api/extensions/custom
- DELETE /api/extensions/custom/{extension}

#### ExtensionServiceConcurrencyTest (4개)
- 동시에 같은 확장자 추가 시 1개만 성공
- 동시에 서로 다른 확장자 추가 시 모두 성공
- 동시에 추가/삭제 시 데이터 정합성 유지
- 200개 제한 동시성 테스트

### 동시성 테스트 설명

동시성 테스트는 멀티스레드 환경에서 데이터 정합성을 검증합니다.
테스트 검증 조건은 **최종 DB 상태**에 초점을 맞추고 있습니다:

```java
// 검증: DB에 정확히 1개만 존재 (최종 결과가 중요)
assertThat(repository.findByFixedFalse()).hasSize(1);
assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
```

**이유**: Race condition으로 인해 "체크 → 저장" 사이에 다른 스레드가 저장할 수 있어
정확히 1개만 성공한다고 보장하기 어렵습니다. 중요한 것은:
- 최종적으로 DB에 중복 없이 1개만 존재
- 적어도 일부 요청이 중복 에러를 받음

## Postman 테스트

`postman_collection.json` 파일을 Postman에 import하여 API 테스트를 수행할 수 있습니다.

### Import 방법
1. Postman 실행
2. Import 버튼 클릭
3. `postman_collection.json` 파일 선택
4. Collection 실행

### 테스트 항목 (35개 이상)
- 기본 CRUD 테스트
- 입력값 검증 테스트
- 보안 테스트 (XSS, SQL Injection, Path Traversal)
- 경계값 테스트

## 테스트 체크리스트

### 고정 확장자 테스트
- [ ] 체크박스 클릭 시 활성화/비활성화 토글 동작
- [ ] 새로고침 후에도 체크 상태 유지
- [ ] 7개 확장자(bat, cmd, com, cpl, exe, scr, js) 모두 표시

### 커스텀 확장자 추가 테스트
- [ ] 정상적인 확장자 추가 (예: sh, py, php)
- [ ] 추가 후 태그 영역에 표시
- [ ] 추가 후 개수 카운터 증가 (n/200)
- [ ] Enter 키로 추가 동작
- [ ] 추가 버튼 클릭으로 추가 동작

### 입력값 검증 테스트
- [ ] 대문자 입력 → 소문자 변환 (SH → sh)
- [ ] 앞뒤 공백 제거 ("  sh  " → "sh")
- [ ] 앞에 점 제거 (".exe" → "exe")
- [ ] 빈 문자열 거부 (에러 메시지 표시)
- [ ] 20자 초과 거부
- [ ] 특수문자 포함 시 거부 (예: sh!, py@, a/b)
- [ ] 한글 입력 거부

### 중복 체크 테스트
- [ ] 이미 등록된 커스텀 확장자 중복 추가 시도 → 거부
- [ ] 고정 확장자와 동일한 확장자 추가 시도 → 거부 (예: exe, js)

### 삭제 테스트
- [ ] 커스텀 확장자 태그의 X 버튼 클릭 시 삭제
- [ ] 삭제 후 개수 카운터 감소
- [ ] 삭제 후 새로고침해도 삭제 상태 유지

### 개수 제한 테스트
- [ ] 200개 등록 후 추가 시도 → 거부 (에러 메시지 표시)

### UI/UX 테스트
- [ ] API 호출 중 로딩 상태 표시 (버튼 비활성화 등)
- [ ] 에러 발생 시 사용자에게 메시지 표시
- [ ] 에러 메시지가 다음 입력 시 사라짐
- [ ] 반응형 레이아웃 (화면 크기 조절)

### 데이터 영속성 테스트
- [ ] 서버 재시작 후 데이터 유지
- [ ] 브라우저 새로고침 후 데이터 유지

---

## 추가 고려사항

### 보안
- [x] XSS 공격 방지 (입력값 이스케이프) - 영숫자만 허용하여 원천 차단
- [x] SQL Injection 방지 (JPA 파라미터 바인딩 사용 중)
- [x] 확장자에 경로 문자 포함 시 거부 (/, \\, ..)

### 동시성
- [x] 동시에 같은 확장자 추가 시 중복 처리 - JUnit 테스트로 검증
- [x] 동시 요청 시 데이터 정합성 - JUnit 테스트로 검증

### 성능
- [ ] 200개 확장자 렌더링 성능
- [ ] API 응답 시간

### 접근성 (A11y)
- [ ] 키보드만으로 모든 기능 사용 가능
- [ ] 스크린 리더 지원
- [ ] 적절한 색상 대비

### 에러 처리
- [ ] 네트워크 오류 시 재시도 안내
- [ ] 서버 500 에러 시 사용자 친화적 메시지

---

## 추가 개선 아이디어

### 기능 개선
- 확장자 검색/필터링 기능
- 일괄 삭제 기능
- 확장자 그룹 관리 (이미지, 문서 등)
- 확장자별 차단 통계
- CSV/JSON으로 내보내기/가져오기

### 관리 기능
- 사용자 인증 및 권한 관리
- 변경 이력 로깅 (audit log)
- 확장자 변경 알림

### UX 개선
- 다크 모드 지원
- 드래그 앤 드롭으로 순서 변경
- 확장자 설명 추가 (exe: 실행 파일)
- 자주 사용하는 확장자 프리셋 제공
