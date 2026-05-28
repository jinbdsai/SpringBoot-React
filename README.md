# Spring Boot + React 토이 프로젝트

> SSH 원격 서버 위에 Spring Boot + React 풀스택 시스템을 Docker 기반으로 구축하고 Jenkins로 CI/CD까지 자동화한 학습용 프로젝트.
> 상세 구축 가이드는 [SETUP_GUIDE.md](./SETUP_GUIDE.md) 참고.

---

## 📁 1. 디렉토리 구조

```
~/project/                              ← 프로젝트 루트
│
├── 📄 README.md                        ← 프로젝트 개요 (이 파일)
├── 📄 SETUP_GUIDE.md                   ← 단계별 구축 가이드
├── 📄 docker-compose.yml               ← 전체 인프라 정의
├── 📄 .gitignore                       ← Git 무시 규칙
├── 📁 .git/                            ← Git 저장소
│
├── 📁 backend/                         ← Spring Boot
│   ├── 📄 Dockerfile                   ← 백엔드 컨테이너 이미지 정의
│   ├── 📄 build.gradle                 ← 의존성 + 빌드 설정
│   ├── 📄 settings.gradle
│   ├── 📄 gradlew / gradlew.bat        ← Gradle Wrapper
│   ├── 📄 .gitignore (Spring 생성)
│   ├── 📁 gradle/wrapper/
│   └── 📁 src/
│       ├── 📁 main/
│       │   ├── 📁 java/com/example/backend/
│       │   │   └── 📄 BackendApplication.java   ← 진입점
│       │   └── 📁 resources/
│       │       └── 📄 application.properties    ← DB/Redis 설정
│       └── 📁 test/
│           └── 📁 java/com/example/backend/
│               └── 📄 BackendApplicationTests.java
│
└── 📁 frontend/                        ← React + Vite
    ├── 📄 Dockerfile                   ← 프론트 컨테이너 이미지 정의
    ├── 📄 package.json                 ← npm 의존성
    ├── 📄 vite.config.js               ← Vite 설정
    ├── 📄 index.html                   ← 진입 HTML
    ├── 📄 .gitignore (Vite 생성)
    ├── 📁 public/                      ← 정적 자원
    │   ├── favicon.svg
    │   └── icons.svg
    └── 📁 src/
        ├── 📄 main.jsx                 ← React 진입점
        ├── 📄 App.jsx                  ← 메인 컴포넌트
        ├── 📄 App.css / index.css
        └── 📁 assets/
```

---

## 🐳 2. 실행 중인 Docker 컨테이너

```
┌─────────────────────────────────────────────────────────────────────┐
│                    호스트 서버 (Ubuntu 26.04)                          │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │                    Docker 데몬                                │    │
│  │                                                                │    │
│  │  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  │    │
│  │  │ toy-frontend   │  │ toy-backend    │  │ toy-jenkins    │  │    │
│  │  │ (Nginx)        │  │ (Spring Boot)  │  │                │  │    │
│  │  │ Port: 80       │  │ Port: 8080     │  │ Port: 8081     │  │    │
│  │  └────────────────┘  └────────────────┘  └────────────────┘  │    │
│  │                                                                │    │
│  │  ┌────────────────┐  ┌────────────────┐                       │    │
│  │  │ toy-mysql      │  │ toy-redis      │                       │    │
│  │  │ MySQL 8.0      │  │ Redis 7        │                       │    │
│  │  │ Port: 3306     │  │ Port: 6379     │                       │    │
│  │  └────────────────┘  └────────────────┘                       │    │
│  │                                                                │    │
│  │  📦 Volumes:                                                  │    │
│  │     • toy-mysql-data    (DB 영속 저장)                         │    │
│  │     • toy-redis-data    (Redis 영속 저장)                      │    │
│  │     • jenkins-data      (Jenkins 설정/히스토리)                │    │
│  └──────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🌐 3. 데이터 흐름 (런타임)

```
                 [브라우저 사용자]
                        │
                        │ HTTP
                        ▼
              ┌─────────────────┐
              │  toy-frontend   │  (Nginx, port 80)
              │   React 정적    │
              │   파일 서빙      │
              └─────────────────┘
                        │
                        │ /api/* 요청
                        ▼
              ┌─────────────────┐
              │  toy-backend    │  (Spring Boot, port 8080)
              │  비즈니스 로직   │
              └─────────────────┘
                  │         │
            JPA  │         │ Spring Data Redis
                  ▼         ▼
       ┌──────────────┐  ┌──────────────┐
       │  toy-mysql   │  │  toy-redis   │
       │  영구 저장    │  │  캐시/세션    │
       └──────────────┘  └──────────────┘
```

---

## 🔁 4. CI/CD 흐름 (배포 자동화)

```
   [개발자]
      │
      │ ① 코드 수정 + git push
      ▼
┌──────────────┐
│   GitHub     │  jinbdsai/SpringBoot-React.git
│   저장소      │
└──────────────┘
      │
      │ ② webhook 알림
      ▼
┌──────────────┐         ③ Jenkinsfile 실행
│ toy-jenkins  │ ────────────────────────┐
│  (port 8081) │                          │
└──────────────┘                          │
      │                                   │
      │ ④ /var/run/docker.sock 통해       │
      │   호스트 Docker 조종              │
      ▼                                   ▼
┌─────────────────────────────────────────────┐
│  ⑤ docker compose down                       │
│  ⑥ docker compose up -d --build              │
│     → frontend, backend 이미지 재빌드        │
│     → 모든 컨테이너 재시작                    │
└─────────────────────────────────────────────┘
      │
      ▼
   [새 버전 배포 완료]
```

---

## 🗺️ 5. 전체 시스템 한 장 요약

```
┌─── 로컬 개발 환경 ───┐         ┌─────────── 원격 서버 (192.168.0.43, Ubuntu) ──────────┐
│                      │         │                                                         │
│   VSCode             │  SSH    │  ┌──────────── Docker 데몬 ──────────────────────┐     │
│  (Remote-SSH)  ──────┼─────────┼─►│                                                │     │
│                      │         │  │  ┌──── Docker Compose 스택 ────┐               │     │
└──────────────────────┘         │  │  │                              │               │     │
                                  │  │  │  Nginx ──► Spring ──► MySQL  │               │     │
                                  │  │  │              │     ──► Redis │               │     │
                                  │  │  └──────────────┼───────────────┘               │     │
                                  │  │                 ▲                                │     │
                                  │  │                 │ docker.sock 마운트            │     │
                                  │  │  ┌──── 단독 컨테이너 ────┐                       │     │
                                  │  │  │  Jenkins (8081)        │                       │     │
                                  │  │  │  자동 빌드 파이프라인   │                       │     │
                                  │  │  └────────────────────────┘                       │     │
                                  │  └────────────────────────────────────────────────┘     │
                                  │                 ▲                                        │
                                  └─────────────────┼────────────────────────────────────────┘
                                                    │ webhook
                                  ┌─────────────────┴───────┐
                                  │      GitHub 저장소        │
                                  │   (jinbdsai/...git)       │
                                  └──────────────────────────┘
                                          ▲
                                          │ git push
                                  ┌───────┴────────┐
                                  │    개발자       │
                                  └────────────────┘
```

> 💡 **구조 핵심:**
> - **모든 서비스는 Docker 컨테이너로 동작** (Jenkins 포함)
> - **다만 Jenkins는 `docker-compose.yml`에 없고 단독 `docker run`으로 띄움** — 앱 배포와 Jenkins 자체의 생명주기를 분리하기 위함 (배포 때마다 Jenkins가 재시작되면 빌드가 끊김)
> - **Jenkins가 호스트 Docker 데몬을 조종**할 수 있는 비결은 `-v /var/run/docker.sock:/var/run/docker.sock` 마운트

---

## 📊 6. 진행 상황 체크

| 단계 | 항목 | 상태 |
|------|------|------|
| 0~4 | 환경 준비 (Linux, Git, Java, Node) | ✅ 완료 |
| 5 | Docker 설치 | ✅ 완료 |
| 6 | MySQL 컨테이너 | ✅ UP |
| 7 | Redis 컨테이너 | ✅ UP |
| 8 | Spring Boot 프로젝트 | ✅ 빌드 완료 |
| 9 | React 프로젝트 (Vite) | ✅ 생성 완료 |
| 10 | GitHub push | ✅ 완료 |
| 11 | Docker Compose 통합 | ⚠️ 진행 중 |
| 12 | Jenkins 설치 | ✅ UP |
| 13 | 최종 배포 확인 | ⏳ 남음 |

---

## 🛠️ 기술 스택

| 영역 | 기술 |
|------|------|
| **Backend** | Spring Boot 3.x, Java 17, JPA (Hibernate), Spring Data Redis |
| **Frontend** | React 18, Vite, Nginx (배포) |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |
| **CI/CD** | Jenkins (LTS) |
| **Infra** | Docker, Docker Compose |
| **VCS** | Git, GitHub |
| **OS** | Ubuntu 26.04 LTS (원격 SSH 서버) |

---

## 🚀 빠른 시작

```bash
# 전체 스택 실행
docker compose up -d --build

# 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f

# 전체 종료
docker compose down
```

### 접속 URL
- React 화면: `http://서버IP/`
- Spring Boot API: `http://서버IP:8080/`
- Jenkins: `http://서버IP:8081/`
