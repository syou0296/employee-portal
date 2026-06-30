# HR Portal — Employee Management System

> **Single Source of Truth** — 이 문서는 설계·구현·운영의 기준입니다. 코드와 반드시 일치해야 합니다.

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [아키텍처](#2-아키텍처)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [DB / Entity 설계](#4-db--entity-설계)
5. [JWT 및 권한 설계](#5-jwt-및-권한-설계)
6. [API 설계](#6-api-설계)
7. [API Error Handling 전략](#7-api-error-handling-전략)
8. [Validation 전략](#8-validation-전략)
9. [Background Check API 연동 전략](#9-background-check-api-연동-전략)
10. [Vue 프로젝트 구조](#10-vue-프로젝트-구조)
11. [구현 순서](#11-구현-순서)
12. [기술 스택](#12-기술-스택)
13. [로컬 실행 가이드](#13-로컬-실행-가이드)

---

## 1. 프로젝트 개요

직원용 **User Portal**과 관리자용 **Admin Dashboard**를 분리한 HR 관리 시스템.

| 포털 | 대상 | 주요 기능 |
|------|------|-----------|
| User Portal | 일반 직원 (ROLE_USER) | 로그인, 자신의 인적사항 조회·수정, 비밀번호 변경 |
| Admin Dashboard | 관리자 (ROLE_ADMIN) | 직원 계정 생성·수정, 목록·상세 조회, 퇴사 처리, Background Check 연동 |

```
외부 Background Check API (AWS API Gateway)
  https://54capvm12g.execute-api.ap-northeast-2.amazonaws.com
```

---

## 2. 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│  Browser                                                │
│  ┌──────────────────┐   ┌──────────────────────────┐   │
│  │  User Portal     │   │  Admin Dashboard         │   │
│  │  Vue 3 SPA       │   │  Vue 3 SPA               │   │
│  │  /               │   │  /admin                  │   │
│  └────────┬─────────┘   └────────────┬─────────────┘   │
└───────────┼─────────────────────────┼─────────────────┘
            │ HTTP (Axios)            │ HTTP (Axios)
            ▼                         ▼
┌─────────────────────────────────────────────────────────┐
│  Spring Boot 3.2.5  (localhost:8080)                    │
│                                                         │
│  ┌─────────────┐  ┌────────────────┐  ┌─────────────┐  │
│  │  AuthController  │  UserController │  AdminController│ │
│  └──────┬──────┘  └───────┬────────┘  └──────┬──────┘  │
│         │                 │                  │         │
│  ┌──────▼─────────────────▼──────────────────▼──────┐  │
│  │           Service Layer                          │  │
│  │  AuthService / EmployeeService /                 │  │
│  │  BackgroundCheckService                          │  │
│  └──────┬─────────────────────────────────┬─────────┘  │
│         │                                 │            │
│  ┌──────▼──────┐              ┌───────────▼─────────┐  │
│  │  H2 (dev)   │              │  WebClient          │  │
│  │  PostgreSQL │              │  (External API)     │  │
│  │  (prod)     │              └─────────────────────┘  │
│  └─────────────┘                                       │
└─────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │  Background Check API      │
                    │  (AWS API Gateway)         │
                    └───────────────────────────┘
```

### 설계 결정 사항

| 항목 | 결정 | 이유 |
|------|------|------|
| DB | H2 (dev) / PostgreSQL (prod) | 로컬 실행 간편성 + 운영 확장성 |
| 인증 | Stateless JWT | SPA + REST API 궁합, 서버 세션 불필요 |
| 외부 API 클라이언트 | Spring WebClient | 논블로킹, Retry/Timeout 설정 용이 |
| 퇴사 처리 | Soft Delete (status=TERMINATED) | 이력 보존, 즉시 로그인 차단 |
| CORS | Spring Security CorsConfigurationSource | 프론트엔드 개발 서버 허용 |

---

## 3. 프로젝트 구조

```
hr-portal/
├── backend/                          # Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/hrportal/
│       ├── HrPortalApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── WebClientConfig.java
│       │   └── DataInitializer.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   └── AdminController.java
│       ├── domain/
│       │   ├── Employee.java
│       │   └── enums/
│       │       ├── Role.java
│       │       └── EmploymentStatus.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── LoginRequest.java
│       │   │   ├── CreateEmployeeRequest.java
│       │   │   ├── UpdateEmployeeRequest.java
│       │   │   ├── UpdateProfileRequest.java
│       │   │   ├── ChangePasswordRequest.java
│       │   │   └── ForgotPasswordRequest.java
│       │   └── response/
│       │       ├── ApiResponse.java
│       │       ├── ErrorResponse.java
│       │       ├── LoginResponse.java
│       │       ├── EmployeeResponse.java
│       │       ├── EmployeeSummaryResponse.java
│       │       └── ForgotPasswordResponse.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── BusinessException.java
│       │   └── ErrorCode.java
│       ├── repository/
│       │   └── EmployeeRepository.java
│       ├── security/
│       │   ├── JwtTokenProvider.java
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtAuthenticationEntryPoint.java
│       │   ├── JwtAccessDeniedHandler.java
│       │   ├── CustomUserDetails.java
│       │   └── CustomUserDetailsService.java
│       └── service/
│           ├── AuthService.java
│           ├── EmployeeService.java
│           └── BackgroundCheckService.java
│
└── frontend/                         # Vue 3
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/
        │   ├── axios.js              # Axios 인스턴스 + 인터셉터
        │   ├── auth.js
        │   ├── user.js
        │   └── admin.js
        ├── components/
        │   ├── SearchBox.vue         # 직원 검색 컴포넌트 (debounce)
        │   └── Pagination.vue        # 페이지네이션 컴포넌트
        ├── router/
        │   └── index.js
        ├── stores/
        │   └── auth.js               # Pinia auth store
        ├── layouts/
        │   ├── UserLayout.vue
        │   └── AdminLayout.vue
        └── views/
            ├── auth/
            │   └── LoginView.vue
            ├── user/
            │   └── ProfileView.vue
            └── admin/
                ├── EmployeeListView.vue
                ├── EmployeeDetailView.vue
                ├── EmployeeCreateView.vue
                └── BackgroundCheckView.vue
```

---

## 4. DB / Entity 설계

### Employee 테이블

```sql
CREATE TABLE employees (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    employee_id     VARCHAR(50)     NOT NULL UNIQUE,  -- EMP-YYYY-NNN
    username        VARCHAR(100)    NOT NULL UNIQUE,  -- 로그인 ID
    password        VARCHAR(255)    NOT NULL,         -- BCrypt
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    phone           VARCHAR(50),
    department      VARCHAR(100),
    position        VARCHAR(100),
    date_of_birth   DATE,
    hire_date       DATE            NOT NULL,
    terminate_date  DATE,
    role            VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | TERMINATED
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 설계 포인트

- `employee_id` : Background Check API 요청 시 사용하는 외부 식별자 (`EMP-YYYY-NNN` 형식)
- `username` : Spring Security 인증에 사용하는 내부 로그인 ID
- `status = TERMINATED` : 퇴사 처리 시 설정. `CustomUserDetailsService`에서 `isEnabled()` false 반환 → 즉시 로그인 차단
- `role` : `ROLE_USER` / `ROLE_ADMIN` 단일 값 저장 (단순한 요구사항에 Join 테이블 불필요)

---

## 5. JWT 및 권한 설계

### 토큰 구조

```json
{
  "sub": "john.doe",          // username
  "role": "ROLE_USER",        // 권한
  "employeeId": "EMP-2024-001",
  "iat": 1700000000,
  "exp": 1700086400           // 24시간
}
```

### 인증 흐름

```
1. POST /api/auth/login  →  username + password
2. Spring Security authenticates  →  JWT 발급
3. 클라이언트: Authorization: Bearer <token>
4. JwtAuthenticationFilter: 토큰 검증 → SecurityContext 설정
5. @PreAuthorize("hasRole('ADMIN')") 로 엔드포인트 보호
```

### 퇴사자 즉시 차단

```
JwtAuthenticationFilter
  → JwtTokenProvider.validateToken()  // 서명·만료 검증
  → CustomUserDetailsService.loadUserByUsername()  // DB에서 사용자 조회
  → employee.status == TERMINATED  →  UserDetails.isEnabled() = false
  → AuthenticationException 발생  →  401 응답
```

퇴사 처리 즉시 DB status 변경 → 기존 JWT가 만료되기 전이라도 다음 요청에서 차단됨.

---

## 6. API 설계

### 공통 응답 형식

**성공:**
```json
{
  "success": true,
  "data": { ... }
}
```

**실패:**
```json
{
  "success": false,
  "error": {
    "code": "EMPLOYEE_NOT_FOUND",
    "message": "직원을 찾을 수 없습니다.",
    "status": 404
  }
}
```

### 인증 API

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/auth/login` | 없음 | 로그인, JWT 발급 |
| POST | `/api/auth/forgot-password` | 없음 | 임시 비밀번호 발급 |

**POST /api/auth/login**
```json
// Request
{ "username": "john.doe", "password": "password123" }

// Response 200
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "role": "ROLE_USER",
    "employeeId": "EMP-2024-001",
    "name": "John Doe"
  }
}
```

**POST /api/auth/forgot-password**
```json
// Request
{ "username": "john.doe", "dateOfBirth": "1990-01-01" }

// Response 200
{
  "success": true,
  "data": {
    "temporaryPassword": "Abc1@xyz9",
    "message": "임시 비밀번호가 발급되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요."
  }
}
```

### User Portal API

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| GET | `/api/me` | ROLE_USER | 본인 인적사항 조회 |
| PUT | `/api/me` | ROLE_USER | 본인 인적사항 수정 (전화번호) |
| PUT | `/api/me/password` | ROLE_USER | 본인 비밀번호 변경 |

**PUT /api/me/password**
```json
// Request
{
  "currentPassword": "oldPass1!",
  "newPassword": "newPass1!",
  "confirmPassword": "newPass1!"
}
```

### Admin Dashboard API

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/admin/employees` | ROLE_ADMIN | 직원 계정 생성 |
| GET | `/api/admin/employees` | ROLE_ADMIN | 직원 목록 조회 |
| GET | `/api/admin/employees/{employeeId}` | ROLE_ADMIN | 직원 상세 조회 |
| PUT | `/api/admin/employees/{employeeId}` | ROLE_ADMIN | 직원 정보 수정 |
| PUT | `/api/admin/employees/{employeeId}/terminate` | ROLE_ADMIN | 퇴사 처리 |
| POST | `/api/admin/employees/{employeeId}/background-checks` | ROLE_ADMIN | Background Check 요청 |
| GET | `/api/admin/employees/{employeeId}/background-checks` | ROLE_ADMIN | Background Check 목록 조회 |
| GET | `/api/admin/employees/{employeeId}/background-checks/{checkId}` | ROLE_ADMIN | Background Check 결과 조회 |

---

## 7. API Error Handling 전략

### 7.1 공통 응답 형식

모든 오류는 아래 형식으로 표준화한다.

```json
{
  "success": false,
  "error": {
    "code": "MACHINE_READABLE_CODE",
    "message": "Human-readable message",
    "status": 400,
    "fieldErrors": [            // Validation 실패 시만 포함
      { "field": "email", "message": "must be a well-formed email address" }
    ]
  }
}
```

### 7.2 ErrorCode 정의

```java
// 비즈니스 오류
EMPLOYEE_NOT_FOUND               (404, "직원을 찾을 수 없습니다.")
EMPLOYEE_ALREADY_TERMINATED      (400, "이미 퇴사 처리된 직원입니다.")
DUPLICATE_USERNAME               (409, "이미 사용 중인 아이디입니다.")
DUPLICATE_EMAIL                  (409, "이미 사용 중인 이메일입니다.")

// 인증/인가
INVALID_CREDENTIALS              (401, "아이디 또는 비밀번호가 올바르지 않습니다.")
TOKEN_EXPIRED                    (401, "인증 토큰이 만료되었습니다.")
TOKEN_INVALID                    (401, "유효하지 않은 인증 토큰입니다.")
ACCOUNT_DISABLED                 (401, "비활성화된 계정입니다. 관리자에게 문의하세요.")
ACCESS_DENIED                    (403, "접근 권한이 없습니다.")

// 비밀번호
INVALID_CURRENT_PASSWORD         (400, "현재 비밀번호가 올바르지 않습니다.")
PASSWORD_SAME_AS_CURRENT         (400, "새 비밀번호는 기존 비밀번호와 달라야 합니다.")
PASSWORD_CONFIRM_MISMATCH        (400, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.")
RESET_PASSWORD_VERIFICATION_FAILED (400, "아이디 또는 생년월일이 올바르지 않습니다.")

// Validation
VALIDATION_FAILED                (400, "입력값이 올바르지 않습니다.")

// 외부 API
BACKGROUND_CHECK_API_ERROR       (502, "Background Check API 오류가 발생했습니다.")
BACKGROUND_CHECK_API_TIMEOUT     (504, "Background Check API 응답 시간을 초과했습니다.")
BACKGROUND_CHECK_NOT_FOUND       (404, "Background Check 결과를 찾을 수 없습니다.")
```

### 7.3 GlobalExceptionHandler

```
Exception 계층:
  BusinessException                → ErrorCode 기반 응답 (4xx)
  MethodArgumentNotValidException  → VALIDATION_FAILED (400) + fieldErrors
  DisabledException                → ACCOUNT_DISABLED (401)
  BadCredentialsException          → INVALID_CREDENTIALS (401)
  AccessDeniedException            → ACCESS_DENIED (403)
  WebClientResponseException       → BACKGROUND_CHECK_API_ERROR (502)
  WebClientRequestException        → BACKGROUND_CHECK_API_TIMEOUT (504)
  Exception (catch-all)            → INTERNAL_SERVER_ERROR (500) + 로그
```

### 7.4 JWT 인증/인가 실패 처리

Spring Security 필터 레벨에서 발생하는 예외는 `@ControllerAdvice`에 도달하지 않으므로 별도 처리:

```
JwtAuthenticationEntryPoint  →  401 JSON 응답  (토큰 없음/만료/무효)
JwtAccessDeniedHandler       →  403 JSON 응답  (권한 부족)
```

두 핸들러 모두 위의 공통 응답 형식으로 직접 `HttpServletResponse`에 write.

### 7.5 외부 API 오류 및 Timeout 처리

```
BackgroundCheckService
  └── WebClient 호출
       ├── .timeout(Duration.ofSeconds(30))       // 30초 Timeout
       ├── onStatus(4xx) → BusinessException(BACKGROUND_CHECK_API_ERROR)
       ├── onStatus(5xx) → BusinessException(BACKGROUND_CHECK_API_ERROR)
       └── TimeoutException → BusinessException(BACKGROUND_CHECK_API_TIMEOUT)

GlobalExceptionHandler
  ├── WebClientResponseException → 502 + "Background Check API 오류"
  └── BusinessException(TIMEOUT) → 504 + "응답 시간 초과"
```

**Retry 전략:**
- POST (체크 요청): 재시도 없음 (멱등성 미보장)
- GET (결과 조회): 최대 2회 재시도, 1초 고정 딜레이

프론트엔드는 `pending` 상태인 경우 5초 간격으로 최대 12회 폴링 후 포기.

### 7.6 프론트엔드 에러 처리

Axios interceptor가 `error.response.data.error` 객체를 직접 reject하므로, 모든 catch 블록에서 `err?.message`로 서버 ErrorCode 메시지를 참조한다.

```javascript
// axios.js interceptor
return Promise.reject(error.response?.data?.error || error)

// catch에서 사용
} catch (err) {
  errorMessage.value = err?.message || '네트워크 오류가 발생했습니다.'
}
```

---

## 8. Validation 전략

### 8.1 Backend (Bean Validation)

```java
// CreateEmployeeRequest
@NotBlank username
@Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d).{8,}$") password  // 8자 이상, 영문+숫자
@NotBlank firstName / lastName
@Email email
@NotNull hireDate
@NotNull role

// UpdateProfileRequest (본인 수정 가능 필드)
phone (선택)

// ChangePasswordRequest
@NotBlank currentPassword
@Size(min=8, max=20) @Pattern newPassword  // 영문+숫자+특수문자 각 1자 이상
@NotBlank confirmPassword

// ForgotPasswordRequest
@NotBlank username
@NotNull dateOfBirth
```

`@Validated` + `MethodArgumentNotValidException` → `VALIDATION_FAILED` + fieldErrors 응답.

### 8.2 Frontend (클라이언트 사전 검증)

- 필수 필드 비어있음 → 즉시 인라인 에러 표시 (서버 요청 전)
- 이메일 형식, 비밀번호 최소 길이 → 정규식 클라이언트 검증
- 서버 Validation 오류 → `fieldErrors` 파싱하여 해당 필드에 표시

---

## 9. Background Check API 연동 전략

### 외부 API 스펙 (Swagger 기준)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/background-checks` | 체크 요청 생성 → `checkId` 반환 |
| GET | `/background-checks?employeeId={id}` | 직원별 전체 이력 조회 |
| GET | `/background-checks/{checkId}` | 특정 체크 결과 조회 |

**상태값:** `pending` → `clear` / `flagged`

### 연동 흐름

```
1. Admin: POST /api/admin/employees/{employeeId}/background-checks
2. Backend → External API: POST /background-checks
   {employeeId, firstName, lastName, dateOfBirth}
3. 응답 status가 'pending'이면 checkId만 반환
4. Frontend: 5초마다 GET /api/admin/employees/{employeeId}/background-checks/{checkId} 폴링
5. status가 clear/flagged → 폴링 종료, 결과 표시
```

### BackgroundCheckService 설계

```java
@Service
public class BackgroundCheckService {
    private final WebClient webClient;  // timeout 설정 포함

    // POST → 외부 API 호출, 결과 반환
    public Object requestCheck(Employee employee);

    // GET by checkId → 외부 API 호출
    public Object getCheckResult(String checkId);

    // GET by employeeId → 외부 API 호출
    public Object listChecksByEmployee(String employeeId);
}
```

### WebClient 설정

```java
WebClient.builder()
    .baseUrl("https://54capvm12g.execute-api.ap-northeast-2.amazonaws.com")
    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30))
    ))
    .build();
```

---

## 10. Vue 프로젝트 구조

### 라우팅 설계

```
/login                   → LoginView (public)

/                        → UserLayout
  /profile               → ProfileView (ROLE_USER)

/admin                   → AdminLayout
  /admin/employees                                    → EmployeeListView (ROLE_ADMIN)
  /admin/employees/new                                → EmployeeCreateView (ROLE_ADMIN)
  /admin/employees/:employeeId                        → EmployeeDetailView (ROLE_ADMIN)
  /admin/employees/:employeeId/background-checks/:checkId → BackgroundCheckView (ROLE_ADMIN)
```

**Navigation Guard:**
```javascript
router.beforeEach((to, from) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) return '/login'
  if (to.meta.requiresAdmin && !auth.isAdmin) return '/profile'
  if (to.path === '/login' && auth.isAuthenticated) {
    return auth.isAdmin ? '/admin/employees' : '/profile'
  }
})
```

### Pinia Auth Store

```javascript
state: {
  token: localStorage.getItem('token'),
  role: localStorage.getItem('role'),
  employeeId: localStorage.getItem('employeeId'),
  name: localStorage.getItem('name'),
}
```

### Axios 인터셉터

```
Request  interceptor → Authorization: Bearer <token> 자동 주입
Response interceptor → 401 수신 시 로그아웃 + /login 리다이렉트 (/auth/login 제외)
                     → 에러 응답 → error.response.data.error 추출하여 reject
```

### 직원 목록 기능 (EmployeeListView)

- **검색**: `SearchBox` 컴포넌트로 직원 ID / 이름 실시간 검색 (debounce 250ms)
- **상태 필터**: 전체 / 재직 중 / 퇴사 탭, 탭별 인원 수 배지 표시
- **페이지네이션**: `Pagination` 컴포넌트, 10명 초과 시 자동 페이지 분할
- **빈 상태**: 검색 결과 없을 때 안내 메시지 표시

---

## 11. 구현 순서

```
1. Backend
   1-1. 프로젝트 셋업 (pom.xml, application.yml)
   1-2. Domain / Entity / Repository
   1-3. JWT (Provider, Filter, EntryPoint, AccessDeniedHandler)
   1-4. SecurityConfig
   1-5. 공통 응답 / ErrorCode / GlobalExceptionHandler
   1-6. AuthService + AuthController (로그인, 임시 비밀번호 발급)
   1-7. EmployeeService + UserController (조회, 프로필 수정, 비밀번호 변경)
   1-8. EmployeeService(Admin) + AdminController (직원 CRUD, 퇴사 처리)
   1-9. BackgroundCheckService + AdminController 연동
   1-10. DataInitializer (관리자 계정 초기 데이터)

2. Frontend
   2-1. 프로젝트 셋업 (Vite, Pinia, Router)
   2-2. Axios 인스턴스 + 인터셉터
   2-3. Auth Store + Router Guard
   2-4. LoginView (로그인 + 임시 비밀번호 발급 모달)
   2-5. User: ProfileView (조회/수정/비밀번호 변경)
   2-6. SearchBox / Pagination 공통 컴포넌트
   2-7. Admin: EmployeeListView (검색, 상태 필터, 페이지네이션)
   2-8. Admin: EmployeeCreateView
   2-9. Admin: EmployeeDetailView + 퇴사 처리 + 직원 정보 수정
   2-10. Admin: BackgroundCheckView + 폴링
```

---

## 12. 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Backend Language | Java | 17 |
| Backend Framework | Spring Boot | 3.2.5 |
| Security | Spring Security | 6.x |
| JWT | jjwt (io.jsonwebtoken) | 0.12.x |
| Build Tool | Maven | 3.x |
| DB (dev) | H2 (in-memory) | - |
| HTTP Client | Spring WebFlux WebClient | - |
| Frontend Framework | Vue | 3.5 |
| Build Tool | Vite | 5.x |
| Router | Vue Router | 4.x |
| State Management | Pinia | 2.x |
| HTTP Client | Axios | 1.x |

---

## 13. 로컬 실행 가이드

### Backend

```bash
cd backend
./mvnw spring-boot:run
# http://localhost:8080

# 초기 계정 (DataInitializer)
# Admin:  admin / admin1234
# User:   john.doe / password123
```

H2 Console: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:hrportal`

### Frontend

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

### 환경 변수 (frontend/.env)

```
VITE_API_BASE_URL=http://localhost:8080
```
