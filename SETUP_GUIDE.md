# Spring Boot + React 토이 프로젝트 구축 가이드북

> **대상:** SSH 원격 서버에서 처음부터 끝까지 직접 시스템을 구축해보고 싶은 초심자
> **환경:** Ubuntu 26.04 LTS (SSH 원격 접속)
> **목표:** Spring Boot(백엔드) + React(프론트엔드) + MySQL + Redis + Jenkins(CI/CD) + Docker 시스템을 구축하고 간단한 배포까지 진행

---

## 📐 전체 아키텍처 다이어그램

```
┌──────────────────────────────────────────────────────────────────┐
│                        내 로컬 컴퓨터                              │
│   ┌─────────────┐                                                 │
│   │  VSCode     │  ──── SSH ────►  원격 서버 접속                  │
│   └─────────────┘                                                 │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                  원격 SSH 서버 (Ubuntu 26.04)                      │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                       Docker 컨테이너                        │  │
│  │                                                              │  │
│  │  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐ │  │
│  │  │ React    │──►│ Spring   │──►│ MySQL    │   │ Redis    │ │  │
│  │  │ (3000)   │   │ Boot     │   │ (3306)   │   │ (6379)   │ │  │
│  │  │ Nginx    │   │ (8080)   │   │          │   │          │ │  │
│  │  └──────────┘   └──────────┘   └──────────┘   └──────────┘ │  │
│  │                       ▲                                      │  │
│  │                       │                                      │  │
│  │                  ┌──────────┐                                │  │
│  │                  │ Jenkins  │  ◄── Git Push 트리거            │  │
│  │                  │ (8081)   │                                │  │
│  │                  └──────────┘                                │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              ▲                                     │
└──────────────────────────────│─────────────────────────────────────┘
                               │
                               │ git push
                               │
                    ┌──────────────────────┐
                    │   개인 GitHub        │
                    │   (소스 코드 저장소)  │
                    └──────────────────────┘
```

### 데이터 흐름 한눈에 보기
1. **개발자** → 코드를 작성하고 **GitHub**에 push
2. **GitHub** → Jenkins에게 "코드 바뀌었어!" 알림(webhook)
3. **Jenkins** → 코드를 가져와 빌드 → Docker 이미지 생성 → 컨테이너 재시작
4. **사용자** → 브라우저로 **React**(프론트) 접속 → **Spring Boot**(백엔드) API 호출 → **MySQL**(데이터 저장) / **Redis**(캐시) 활용

---

## ✅ 체크리스트 전체 요약

전체 과정은 이 순서로 진행합니다. 각 단계는 아래 상세 섹션에서 다룹니다.

- [x] **0단계** — 사전 준비 (SSH 접속, 기본 Linux 사용법 익히기)
- [x] **1단계** — 서버 기본 세팅 (패키지 업데이트, 필수 도구 설치)
- [x] **2단계** — Git 설치 및 GitHub 계정/저장소 만들기
- [x] **3단계** — Java(JDK) 17 설치
- [x] **4단계** — Node.js & npm 설치 (React용)
- [ ] **5단계** — Docker 설치
- [ ] **6단계** — MySQL 설치 (Docker로)
- [ ] **7단계** — Redis 설치 (Docker로)
- [ ] **8단계** — Spring Boot 프로젝트 만들기
- [ ] **9단계** — React 프로젝트 만들기
- [ ] **10단계** — GitHub에 프로젝트 push
- [ ] **11단계** — Docker로 Spring + React 빌드
- [ ] **12단계** — Jenkins 설치 및 CI/CD 파이프라인 구축
- [ ] **13단계** — 배포 및 확인

---

## 0️⃣ 사전 준비

### 0-1. SSH 접속 상태 확인

지금 SSH 원격으로 서버에 접속되어 있다면 아래 명령어로 본인이 어느 서버에 있는지 확인하세요.

```bash
whoami        # 현재 로그인한 사용자명
hostname      # 서버 이름
pwd           # 현재 디렉토리 (홈 디렉토리여야 함)
```

- [x] 서버에 정상 접속됨을 확인

### 0-2. 알아두면 좋은 기본 Linux 명령어

| 명령어 | 설명 |
|--------|------|
| `ls -la` | 현재 폴더의 파일/폴더 목록(숨김 포함) |
| `cd 폴더명` | 폴더 이동 |
| `cd ~` | 홈 디렉토리로 |
| `mkdir 폴더명` | 폴더 생성 |
| `rm 파일명` | 파일 삭제 (`rm -r 폴더`는 폴더 통째 삭제, 주의!) |
| `cat 파일명` | 파일 내용 보기 |
| `nano 파일명` | 텍스트 편집기 (초보자용) |
| `sudo 명령` | 관리자 권한으로 실행 |
| `apt list --installed` | 설치된 패키지 보기 |
| `curl -fsSL URL` | URL의 내용 가져오기 |

> 💡 **팁:** 명령어 도중 멈추고 싶으면 `Ctrl + C`, 화면 지우기는 `clear` 입니다.

---

## 1️⃣ 서버 기본 세팅

### 1-1. 패키지 목록 업데이트 & 업그레이드

서버를 처음 받으면 시스템이 최신 상태가 아닐 수 있습니다. 우선 업데이트부터 합니다.

```bash
sudo apt update
sudo apt upgrade -y
```

> `-y`는 "모든 질문에 yes" 라는 뜻입니다.

- [x] 패키지 업데이트 완료

### 1-2. 필수 도구 설치

개발에 자주 쓰는 도구들을 한 번에 설치합니다.

```bash
sudo apt install -y \
    curl \
    wget \
    vim \
    git \
    unzip \
    build-essential \
    ca-certificates \
    gnupg \
    lsb-release \
    software-properties-common
```

설치 확인:
```bash
curl --version
git --version
```

- [x] 필수 도구 설치 완료

### 1-3. 작업 디렉토리 만들기

```bash
mkdir -p ~/project
cd ~/project
```

- [x] 작업 디렉토리 생성

---

## 2️⃣ Git & GitHub 세팅

이 가이드에서는 **개인 GitHub 계정**을 사용합니다. GitHub는 전 세계에서 가장 많이 쓰는 Git 호스팅 서비스라서, 토이 프로젝트 경험을 포트폴리오로도 활용할 수 있어 좋습니다.

### 2-1. GitHub 개인 계정 만들기

1. 브라우저에서 https://github.com 접속
2. 우측 상단 **Sign up** 클릭
3. 이메일 입력 → 비밀번호 설정 → 사용자명(username) 선택
   - 💡 사용자명은 URL에 그대로 노출됩니다(`github.com/사용자명`). 추후 변경 가능하나 깔끔하게 짓는 걸 추천
4. 이메일 인증 코드 입력 → **Create account**
5. 무료 플랜(Free) 선택
6. (선택) 프로필 설정 질문은 **Skip personalization** 가능

#### 이메일 인증 확인
1. 우측 상단 프로필 → **Settings** → **Emails**
2. **Primary email address** 가 **Verified** 인지 확인

- [x] GitHub 계정 생성 및 이메일 인증 완료

### 2-2. 서버에서 Git 사용자 설정

서버에서 git을 처음 쓰기 전에 본인 정보를 등록합니다. **GitHub 가입에 쓴 이메일과 동일하게** 입력하세요. (그래야 커밋이 GitHub 프로필에 연결됨)

```bash
git config --global user.name "GitHub사용자명"
git config --global user.email "GitHub가입이메일@example.com"
git config --global init.defaultBranch main
```

확인:
```bash
git config --global --list
```

- [x] git 사용자 정보 설정

### 2-3. SSH 키 생성 및 GitHub 등록

GitHub에 매번 비밀번호 안 치고 push 하려면 SSH 키를 등록해야 합니다.

**① SSH 키 생성**
```bash
ssh-keygen -t ed25519 -C "GitHub가입이메일@example.com"
# 질문 3개 모두 그냥 Enter (기본 경로, 빈 패스프레이즈)
```

**② 공개키 출력**
```bash
cat ~/.ssh/id_ed25519.pub
```

출력된 `ssh-ed25519 AAAA...` 로 시작하는 전체 한 줄을 복사합니다.

**③ GitHub에 등록**
1. GitHub 우측 상단 프로필 → **Settings** → 좌측 **SSH and GPG keys**
2. **New SSH key** 버튼 클릭
3. **Title**: 알아볼 수 있는 이름 (예: `dev-server`)
4. **Key type**: `Authentication Key` (기본값)
5. **Key** 칸에 복사한 공개키 붙여넣기
6. **Add SSH key** 클릭 → 비밀번호 재확인

**④ 연결 테스트**
```bash
ssh -T git@github.com
```
처음이면 `Are you sure you want to continue connecting?` 질문에 `yes` 입력.
`Hi 사용자명! You've successfully authenticated...` 메시지가 나오면 성공.

- [x] SSH 키 GitHub 등록 완료

### 2-4. GitHub에 새 저장소(Repository) 만들기

1. GitHub 우측 상단 **+** 버튼 → **New repository**
2. **Repository name**: `project` (또는 원하는 이름)
3. **Description**: (선택) `Spring Boot + React toy project`
4. **Visibility**:
   - **Private** 권장 (혼자 공부용)
   - **Public** 으로 하면 포트폴리오처럼 공개 가능
5. **Initialize this repository with** 옵션들은 **모두 체크 해제** (직접 push 할 거라서)
   - ❌ Add a README file
   - ❌ Add .gitignore
   - ❌ Choose a license
6. **Create repository** 클릭

생성된 저장소 페이지 상단의 **Code** 버튼 → **SSH** 탭에서 URL을 복사해둡니다.
예: `git@github.com:사용자명/project.git`

- [x] GitHub 저장소 생성, SSH URL 복사

> 💡 **나중에 10단계(`git remote add origin ...`)에서 이 SSH URL을 사용합니다.** 메모해두세요.

---

## 3️⃣ Java(JDK) 17 설치

Spring Boot 3.x는 Java 17 이상이 필요합니다.

### 3-1. OpenJDK 17 설치

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```

### 3-2. 설치 확인

```bash
java -version
javac -version
```

`openjdk version "17.x.x"` 가 나오면 성공.

### 3-3. JAVA_HOME 환경변수 설정

```bash
echo 'export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

확인:
```bash
echo $JAVA_HOME
```

- [x] Java 17 설치 및 JAVA_HOME 설정 완료

---

## 4️⃣ Node.js & npm 설치 (React용)

React는 Node.js 위에서 동작합니다. LTS 버전(20.x)을 권장합니다.

### 4-1. NodeSource를 통한 Node.js 20 설치

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

### 4-2. 설치 확인

```bash
node -v       # v20.x.x
npm -v        # 10.x.x
```

- [x] Node.js & npm 설치 완료

---

## 5️⃣ Docker 설치

Docker는 MySQL/Redis/Jenkins 같은 인프라를 격리된 컨테이너로 실행해줍니다. 시스템이 깔끔하게 유지돼서 초심자에게 특히 좋습니다.

### 5-1. Docker 공식 저장소 등록 & 설치

```bash
# 이전 버전 제거 (있을 경우)
sudo apt remove docker docker-engine docker.io containerd runc 2>/dev/null

# Docker GPG 키 등록
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Docker 저장소 등록
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 설치
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 5-2. sudo 없이 docker 쓰기 (권한 추가)

```bash
sudo usermod -aG docker $USER
# 변경 적용 — 로그아웃 후 다시 SSH 접속하거나 아래 명령 실행
newgrp docker
```

### 5-3. 설치 확인

```bash
docker --version
docker compose version
docker run hello-world
```

`Hello from Docker!` 메시지가 나오면 성공.

- [ ] Docker 설치 및 권한 설정 완료

---

## 6️⃣ MySQL 설치 (Docker로)

직접 설치하지 않고 Docker 컨테이너로 띄우는 게 가장 깔끔합니다.

### 6-1. MySQL 컨테이너 실행

```bash
docker run -d \
  --name toy-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass1234 \
  -e MYSQL_DATABASE=toydb \
  -e MYSQL_USER=toyuser \
  -e MYSQL_PASSWORD=toypass1234 \
  -p 3306:3306 \
  -v toy-mysql-data:/var/lib/mysql \
  --restart unless-stopped \
  mysql:8.0
```

> 💡 **옵션 설명**
> - `-d`: 백그라운드 실행
> - `--name`: 컨테이너 이름
> - `-e`: 환경변수 (비밀번호, DB명 등 세팅)
> - `-p 3306:3306`: 호스트 3306 → 컨테이너 3306 포워딩
> - `-v toy-mysql-data:/var/lib/mysql`: 데이터 영구 저장 (컨테이너 지워져도 데이터 살아있음)

### 6-2. 컨테이너 확인

```bash
docker ps
```
`toy-mysql`이 `Up` 상태로 나와야 함.

### 6-3. MySQL 접속 테스트

```bash
docker exec -it toy-mysql mysql -u toyuser -ptoypass1234 toydb
```

들어가면:
```sql
SHOW DATABASES;
EXIT;
```

- [ ] MySQL 컨테이너 실행 및 접속 확인

### 6-4. 간단한 쿼리 테스트 (CRUD)

MySQL이 실제로 데이터를 잘 저장/조회하는지 확인합니다. 다시 컨테이너에 접속:

```bash
docker exec -it toy-mysql mysql -u toyuser -ptoypass1234 toydb
```

#### ① 현재 DB 확인
```sql
SELECT DATABASE();
-- → toydb 가 나오면 정상
```

#### ② 테이블 생성 (CREATE)
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### ③ 테이블 목록 & 구조 확인
```sql
SHOW TABLES;
-- → users 가 보여야 함

DESC users;
-- → 컬럼 구조가 표 형태로 출력
```

#### ④ 데이터 입력 (INSERT)
```sql
INSERT INTO users (name, email) VALUES ('홍길동', 'gildong@test.com');
INSERT INTO users (name, email) VALUES ('김철수', 'cs@test.com');
INSERT INTO users (name, email) VALUES ('이영희', 'yh@test.com');
```
각 명령어마다 `Query OK, 1 row affected` 가 떠야 합니다.

#### ⑤ 데이터 조회 (SELECT)
```sql
SELECT * FROM users;
```
표 형태로 3개 행이 출력되면 성공!

#### ⑥ 데이터 수정 (UPDATE)
```sql
UPDATE users SET name = '홍길순' WHERE email = 'gildong@test.com';
SELECT * FROM users WHERE email = 'gildong@test.com';
-- → name이 '홍길순'으로 바뀌어 있어야 함
```

#### ⑦ 데이터 삭제 (DELETE)
```sql
DELETE FROM users WHERE email = 'yh@test.com';
SELECT COUNT(*) FROM users;
-- → 2 가 나와야 함
```

#### ⑧ 종료
```sql
EXIT;
```

- [ ] CRUD 쿼리 정상 동작 확인

### 6-5. 데이터 영속성 테스트 (컨테이너 재시작해도 데이터 유지되는지)

`-v toy-mysql-data:/var/lib/mysql` 옵션 덕분에 컨테이너를 지웠다 다시 만들어도 데이터가 살아있는지 확인합니다.

#### ① 컨테이너 재시작
```bash
docker restart toy-mysql
```

#### ② 데이터 확인
```bash
docker exec -it toy-mysql mysql -u toyuser -ptoypass1234 toydb -e "SELECT * FROM users;"
```

> 💡 `-e "쿼리"` 옵션으로 접속 없이 한 줄 실행 가능!

재시작 전과 동일한 데이터(2개 행: 홍길순, 김철수)가 나오면 **영속성(Persistence) 정상 동작**.

#### ③ (선택) 컨테이너 완전 삭제 후 재생성 테스트
```bash
# 컨테이너 제거 (volume은 살아있음)
docker stop toy-mysql
docker rm toy-mysql

# 동일한 volume으로 재생성
docker run -d \
  --name toy-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass1234 \
  -e MYSQL_DATABASE=toydb \
  -e MYSQL_USER=toyuser \
  -e MYSQL_PASSWORD=toypass1234 \
  -p 3306:3306 \
  -v toy-mysql-data:/var/lib/mysql \
  --restart unless-stopped \
  mysql:8.0

# 30초 정도 기다린 후 확인
sleep 30
docker exec -it toy-mysql mysql -u toyuser -ptoypass1234 toydb -e "SELECT * FROM users;"
```

여전히 데이터가 나오면 **volume 마운트가 제대로 동작 중**.

- [ ] 영속성 테스트 (재시작 후 데이터 유지 확인)

> 💡 **실무 팁:** 위 테스트로 만든 `users` 테이블은 나중에 Spring Boot JPA가 자동으로 다시 만들 수 있으니, 지우고 싶으면 접속해서 `DROP TABLE users;` 하면 됩니다.

---

## 7️⃣ Redis 설치 (Docker로)

```bash
docker run -d \
  --name toy-redis \
  -p 6379:6379 \
  -v toy-redis-data:/data \
  --restart unless-stopped \
  redis:7-alpine redis-server --appendonly yes
```

### 7-1. Redis 접속 테스트

```bash
docker exec -it toy-redis redis-cli
```

들어가면:
```
PING
# → PONG 이 나오면 성공
SET hello world
GET hello
# → "world"
EXIT
```

- [ ] Redis 컨테이너 실행 및 접속 확인

---

## 8️⃣ Spring Boot 프로젝트 만들기

### 8-1. start.spring.io에서 프로젝트 생성

브라우저에서 https://start.spring.io 에 접속해 아래처럼 설정:

| 항목 | 값 |
|------|------|
| Project | **Gradle - Groovy** |
| Language | **Java** |
| Spring Boot | 3.x.x (안정 버전) |
| Group | `com.example` |
| Artifact | `backend` |
| Packaging | **Jar** |
| Java | **17** |

**Dependencies** 추가:
- Spring Web
- Spring Data JPA
- Spring Data Redis
- MySQL Driver
- Lombok
- Spring Boot DevTools

**GENERATE** → zip 다운로드 → 서버로 업로드합니다.

### 8-2. 서버로 zip 업로드

로컬에서 다운받은 zip을 서버로 옮기는 가장 쉬운 방법:

**방법 A: VSCode 사용** — VSCode 파일 탐색기에서 zip 파일을 `~/project/`로 드래그&드롭

**방법 B: scp 명령어 (로컬 터미널에서 실행)**
```bash
scp ~/Downloads/backend.zip 사용자명@서버IP:~/project/
```

### 8-3. 압축 풀기

```bash
cd ~/project
unzip backend.zip
ls backend
```

### 8-4. application.properties 설정

```bash
nano ~/project/backend/src/main/resources/application.properties
```

아래 내용 붙여넣기:
```properties
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/toydb?useSSL=false&serverTimezone=Asia/Seoul
spring.datasource.username=toyuser
spring.datasource.password=toypass1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

저장: `Ctrl+O` → Enter → `Ctrl+X`

> ⚠️ **dialect 설정을 명시하지 않는 이유**
> Hibernate 6.x (Spring Boot 3.x 기본)부터는 `MySQL8Dialect` 클래스가 제거되고 `MySQLDialect`로 통합되었습니다. 그리고 **JDBC URL을 보고 Hibernate가 자동으로 적절한 dialect를 선택**하므로 굳이 명시하지 않는 게 안전합니다. 굳이 적고 싶다면 `org.hibernate.dialect.MySQLDialect` 를 사용하세요.

### 8-5. 빌드 전 사전 확인

Spring Boot가 기본 생성하는 `contextLoads()` 테스트는 **실제 Spring Context를 띄워서 MySQL/Redis 연결까지 검증**합니다. 따라서 빌드 전에 반드시 확인:

```bash
docker ps
```
`toy-mysql`, `toy-redis` 둘 다 `Up` 상태여야 합니다. 안 떠 있으면:
```bash
docker start toy-mysql toy-redis
```

### 8-6. 빌드 & 실행

```bash
cd ~/project/backend
chmod +x gradlew
./gradlew build
./gradlew bootRun
```

`Started BackendApplication in X seconds` 메시지가 나오면 성공. `Ctrl+C`로 중지.

---

#### 🚨 빌드 실패 시 진단 순서

테스트가 실패하는 건 보통 **진짜 문제가 있다는 신호**입니다. 차근차근 원인을 찾으세요:

**① 에러 로그를 끝까지 읽기**
빌드 출력 마지막에 보이는 경로를 열어보세요:
```
file:///home/jk/project/backend/build/reports/tests/test/index.html
```
브라우저로 열면 어떤 테스트가 왜 실패했는지 보입니다. 또는 터미널에서 핵심 원인 찾기:
```bash
./gradlew build --info 2>&1 | grep -A 5 "Caused by"
```

**② 자주 만나는 원인별 처방**

| 에러 키워드 | 원인 | 해결 |
|------------|------|------|
| `ClassNotFoundException ... Dialect` | Hibernate 6에서 사라진 dialect 명시 | `application.properties`에서 `hibernate.dialect=...MySQL8Dialect` 줄 삭제 |
| `Communications link failure` | MySQL 컨테이너가 안 떠 있음 | `docker ps` 확인 후 `docker start toy-mysql` |
| `Unable to connect to Redis` | Redis 컨테이너가 안 떠 있음 | `docker start toy-redis` |
| `Access denied for user` | 비밀번호/계정 불일치 | `application.properties`의 username/password 재확인 |
| `Unknown database 'toydb'` | DB 이름 오타 | MySQL 컨테이너 환경변수와 일치 확인 |

**③ 정말 급할 때만 임시 우회 (비추천)**
```bash
./gradlew build -x test
```
`-x test`는 "test 태스크 제외"라는 뜻인데, 이건 **버그 알람을 끄는 안티패턴**입니다. 토이 프로젝트 단계라도 위 ①②번으로 진짜 원인을 잡고 넘어가는 게 좋습니다.

- [ ] Spring Boot 빌드 및 실행 확인

---

## 9️⃣ React 프로젝트 만들기

### 9-1. Vite로 React 프로젝트 생성

옛날 `create-react-app`보다 빠른 **Vite**를 사용합니다.

```bash
cd ~/project
npm create vite@latest frontend -- --template react
cd frontend
npm install
```

### 9-2. 개발 서버 실행 테스트

```bash
npm run dev -- --host 0.0.0.0
```

`http://서버IP:5173` 로 접속하면 React 기본 화면이 보입니다. `Ctrl+C`로 중지.

> 💡 외부에서 접속 안 되면 서버 방화벽에서 해당 포트를 열어야 합니다.
> ```bash
> sudo ufw allow 5173
> ```

- [ ] React 프로젝트 생성 및 실행 확인

### 9-3. 백엔드 API 호출 테스트용 간단 수정

`~/project/frontend/src/App.jsx`를 편집해 fetch 호출 한 줄 넣어보면 좋지만, 이 가이드에서는 생략. (나중에 자유롭게)

---

## 🔟 GitHub에 프로젝트 push

### 10-1. 디렉토리 구조 정리

```bash
cd ~/project
ls
# backend  frontend  backend.zip
rm backend.zip       # zip 파일은 이제 필요 없음
```

### 10-2. .gitignore 만들기

루트에 `.gitignore` 파일 생성:
```bash
nano ~/project/.gitignore
```

내용:
```
# Backend
backend/build/
backend/.gradle/
backend/out/

# Frontend
frontend/node_modules/
frontend/dist/

# IDE
.vscode/
.idea/
*.iml

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Env
.env
.env.local
```

### 10-3. Git 초기화 & 첫 커밋

```bash
cd ~/project
git init
git add .
git commit -m "Initial commit: spring boot + react"
```

### 10-4. GitHub과 연결 후 push

2-4 단계에서 복사해둔 GitHub SSH URL을 사용합니다.

```bash
git remote add origin git@github.com:사용자명/project.git
git branch -M main
git push -u origin main
```

GitHub 저장소 페이지 새로고침 → 파일들이 올라가 있으면 성공.

- [ ] GitHub에 최초 push 완료

---

## 1️⃣1️⃣ Docker로 Spring + React 빌드

### 11-1. Backend Dockerfile 작성

```bash
nano ~/project/backend/Dockerfile
```

내용:
```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew bootJar -x test

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 11-2. Frontend Dockerfile 작성

```bash
nano ~/project/frontend/Dockerfile
```

내용:
```dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 11-3. docker-compose.yml 작성

전체 인프라 한 번에 띄우기:

```bash
nano ~/project/docker-compose.yml
```

내용:
```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: toy-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpass1234
      MYSQL_DATABASE: toydb
      MYSQL_USER: toyuser
      MYSQL_PASSWORD: toypass1234
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: toy-redis
    command: redis-server --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    restart: unless-stopped

  backend:
    build: ./backend
    container_name: toy-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/toydb?useSSL=false&serverTimezone=Asia/Seoul
      SPRING_DATASOURCE_USERNAME: toyuser
      SPRING_DATASOURCE_PASSWORD: toypass1234
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      - mysql
      - redis
    restart: unless-stopped

  frontend:
    build: ./frontend
    container_name: toy-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:
```

### 11-4. 기존 단독 컨테이너 정리 후 통합 실행

```bash
# 6,7단계에서 띄운 컨테이너 정리
docker stop toy-mysql toy-redis 2>/dev/null
docker rm toy-mysql toy-redis 2>/dev/null

# 통합 실행
cd ~/project
docker compose up -d --build
docker compose ps
```

모든 서비스가 `Up` 상태로 보이면 성공.

브라우저에서:
- `http://서버IP/` → React 화면
- `http://서버IP:8080/` → Spring Boot 응답 (Whitelabel Error Page도 OK, 동작은 한다는 뜻)

- [ ] Docker Compose로 전체 스택 실행 확인

---

## 1️⃣2️⃣ Jenkins 설치 및 CI/CD 구축

### 12-1. Jenkins 컨테이너 실행

```bash
docker run -d \
  --name toy-jenkins \
  -p 8081:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --restart unless-stopped \
  jenkins/jenkins:lts
```

> 💡 `/var/run/docker.sock` 마운트는 Jenkins 안에서 호스트 Docker를 쓰기 위함입니다.

### 12-2. 초기 비밀번호 확인

```bash
docker exec toy-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

출력된 비밀번호를 복사합니다.

### 12-3. Jenkins 웹 UI 초기 설정

1. 브라우저에서 `http://서버IP:8081` 접속
2. 복사한 비밀번호 입력
3. **Install suggested plugins** 선택 (몇 분 소요)
4. **관리자 계정** 생성 (이름/비밀번호/이메일)
5. **Jenkins URL**: `http://서버IP:8081/` 그대로 → **Save and Finish**

- [ ] Jenkins 웹 UI 진입 완료

### 12-4. Jenkins에서 사용할 도구 플러그인 확인

좌측 **Manage Jenkins** → **Plugins** → **Available plugins**에서 검색 후 설치:
- **Git** (보통 기본 설치됨)
- **GitHub** / **GitHub Branch Source** / **GitHub Integration**
- **Docker Pipeline**
- **Pipeline**

### 12-5. GitHub Personal Access Token (PAT) 발급

Jenkins가 GitHub 저장소를 **HTTPS로 클론**하거나 **API 호출**할 때 사용할 인증 토큰을 만듭니다.

1. GitHub 우측 상단 프로필 → **Settings**
2. 좌측 가장 아래 **Developer settings**
3. **Personal access tokens** → **Tokens (classic)** → **Generate new token (classic)**
4. **Note**: `jenkins`
5. **Expiration**: 원하는 기간 (예: `90 days`)
6. **Scopes**:
   - ✅ `repo` (전체) — 저장소 클론/접근용
   - ✅ `admin:repo_hook` (선택) — webhook 관리용
7. **Generate token** 클릭

> 🚨 **토큰은 발급 직후 한 번만 표시됩니다. 절대 한 번에 화면을 닫지 마세요!**
> 다음 중 한 곳에 즉시 저장:
> - ⭐ **비밀번호 관리자** (1Password, Bitwarden, Apple Keychain 등) — 가장 안전
> - 메모장에 임시 복사 → 12-6 등록 후 삭제
> - 본인만 접근 가능한 로컬 파일 (`~/secrets.txt` 등)
>
> **절대 git에 올라가는 위치에 두면 안 됩니다.** 잃어버리면 GitHub에서 새 토큰을 발급받아야 하고(기존 토큰은 못 봄), Jenkins 등록도 다시 해야 합니다.

- [ ] PAT 발급 및 안전한 곳에 저장 완료

### 12-6. Jenkins Credentials Store에 토큰 등록

> 💡 **왜 Credentials Store?** Jenkins가 비밀을 자동 암호화 저장 + 로그 마스킹까지 해줍니다. 절대 git이나 코드에 토큰을 직접 넣지 마세요.

이 가이드는 **HTTPS 방식으로 GitHub 저장소를 클론**합니다. (SSH 방식은 Jenkins 컨테이너 내부 SSH 키 세팅이 복잡하고 `Host key verification failed` 에러가 잘 납니다.)

#### 등록 절차

1. Jenkins → **Manage Jenkins** 클릭
2. **Credentials** 클릭
3. **Stores scoped to Jenkins** 영역의 **System** 클릭
4. **Global credentials (unrestricted)** 클릭
5. 우측 상단 **+ Add Credentials** 클릭
6. 폼 입력:
   - **Kind**: `Username with password`
   - **Username**: `[본인의 GitHub 사용자명]` (예: `jinbdsai`)
   - **Password**: `[12-5에서 복사한 GitHub PAT 붙여넣기]`
   - **ID**: `github-https`
   - **Description**: `GitHub HTTPS auth (PAT)`
7. 하단 **Create** 버튼 클릭

> 💡 이 한 개의 credential로:
> - **저장소 클론 (12-8)** — Pipeline Job 설정에서 `github-https` 선택
> - **API 호출 (12-7)** — Jenkinsfile에서 `credentials('github-https')` 로 환경변수 주입

> 💡 **다른 비밀(DB 비밀번호, API 키 등)은 어디에?**
> - 앱이 직접 쓰는 비밀 → 프로젝트 루트의 `.env` 파일 (반드시 `.gitignore`에 포함!)
> - 협업용 공유 비밀 → Vault, AWS Secrets Manager 등 (실무에서)
>
> **공통 원칙: 비밀은 절대 git에 올리지 않는다.** 실수로 push했다면 즉시 GitHub에서 해당 토큰을 revoke(폐기)하고 새로 발급받으세요.

- [ ] Jenkins Credentials Store에 `github-https` 등록 완료

### 12-7. Jenkinsfile 작성

프로젝트 루트에 파이프라인 정의 파일 만들기. (VSCode 파일 탐색기에서 `~/project/` 우클릭 → New File → `Jenkinsfile` 로 만들어도 됩니다.)

```bash
nano ~/project/Jenkinsfile
```

아래 내용 전체를 그대로 붙여넣기:

```groovy
pipeline {
    agent any

    // 12-6에서 등록한 GitHub credentials를 환경변수로 주입.
    // 'Username with password' 형식이면 자동으로 두 변수가 만들어짐:
    //   GITHUB_AUTH      → 'username:token' 합본
    //   GITHUB_AUTH_USR  → username
    //   GITHUB_AUTH_PSW  → token (API 호출에 사용)
    // 로그에는 자동으로 ****로 마스킹됨.
    environment {
        GITHUB_AUTH = credentials('github-https')
    }

    stages {
        // ① GitHub API 접근이 잘 되는지 토큰 검증 (선택, 디버깅용)
        stage('Verify GitHub Token') {
            steps {
                sh 'curl -s -H "Authorization: token $GITHUB_AUTH_PSW" https://api.github.com/user | head -5'
            }
        }

        // ② 저장소에서 최신 코드 받아오기 (Job 설정의 SCM 정보를 사용)
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ③ 빌드 + 컨테이너 재시작
        stage('Build & Deploy') {
            steps {
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }

        // ④ 헬스 체크
        stage('Health Check') {
            steps {
                sh 'sleep 20'
                sh 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo '✅ 배포 성공!'
        }
        failure {
            echo '❌ 배포 실패. 로그 확인 필요.'
        }
    }
}
```

> ⚠️ **자주 하는 실수: `stages` 블록은 반드시 하나!**
> Declarative Pipeline은 `stages { ... }` 블록을 **딱 1개만** 가질 수 있습니다. 여러 `stage` 는 모두 하나의 `stages` 블록 안에 모아 넣으세요. 두 개로 나뉘어 있으면 문법 에러로 빌드 자체가 안 됩니다.

> 💡 **`Verify GitHub Token` 스테이지가 필요 없다면?**
> 단순 배포만 한다면 `environment {}` 블록과 `Verify GitHub Token` 스테이지는 생략 가능합니다. 단, 이후 PR 코멘트 자동 작성, 빌드 상태 GitHub에 표시 등 GitHub API를 호출할 일이 생기면 미리 넣어두는 게 편합니다.

커밋 & push:
```bash
cd ~/project
git add Jenkinsfile
git commit -m "Add Jenkinsfile"
git push
```

- [ ] Jenkinsfile 작성 및 GitHub push 완료

### 12-8. Jenkins에 파이프라인 Job 생성

1. Jenkins 메인 화면 → **New Item** (또는 우측 상단 + 버튼)
2. **이름**: `project-pipeline` 입력 → **Pipeline** 선택 → **OK**
3. 설정 페이지에서 아래로 스크롤하여 **Pipeline** 섹션 찾기:
   - **Definition**: `Pipeline script from SCM` 선택
   - **SCM**: `Git` 선택
   - **Repository URL**: `https://github.com/사용자명/저장소명.git`
     - ⚠️ 반드시 **HTTPS URL** 사용 (예: `https://github.com/jinbdsai/SpringBoot-React.git`)
     - GitHub 저장소 페이지 → 초록색 **Code** 버튼 → **HTTPS** 탭에서 복사 가능
   - **Credentials**: 드롭다운에서 **12-6에서 등록한 `github-https`** 선택
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
4. 하단 **Save** 클릭

> ✅ **Save 직후 에러 메시지가 사라졌으면** Jenkins가 GitHub에 정상 접근 가능한 상태입니다.
>
> ❌ `Failed to connect to repository` 또는 `Authentication failed` 가 뜨면:
> - URL이 `https://` 로 시작하는지 확인 (SSH 형식 `git@github.com:...` 이면 안 됨)
> - Credentials의 username이 본인 GitHub 사용자명인지 확인
> - PAT의 `repo` scope 가 체크돼 있는지 확인
> - PAT 만료/폐기 여부 확인 — 의심되면 12-5부터 새 토큰 발급 후 12-6 재등록

### 12-9. 수동 빌드 실행

좌측 메뉴의 **Build Now** 클릭 → 파이프라인이 돌면서 모든 스테이지가 초록색이면 성공.

- [ ] Jenkins 파이프라인 빌드 성공

### 12-10. (선택) GitHub Webhook 자동 트리거 연결

push할 때마다 자동으로 Jenkins가 빌드하게 하려면:

**① Jenkins Job에서 webhook 트리거 활성화**
1. Jenkins → 해당 파이프라인 → **Configure**
2. **Build Triggers** 섹션 → **GitHub hook trigger for GITScm polling** 체크 → **Save**

**② GitHub 저장소에 Webhook 등록**
1. GitHub 저장소 → **Settings** → 좌측 **Webhooks** → **Add webhook**
2. **Payload URL**: `http://서버IP:8081/github-webhook/` (끝 슬래시 필수!)
3. **Content type**: `application/json`
4. **Which events?**: `Just the push event` 선택
5. **Active** 체크 → **Add webhook**
6. 등록 후 webhook 목록에 ✅ 초록색 체크 표시가 뜨면 정상

> 💡 서버가 외부 접속 불가하면 GitHub가 webhook을 못 보냅니다. 이 경우 [ngrok](https://ngrok.com)으로 임시 터널을 만들거나, 서버 공인 IP/방화벽 8081 포트를 열어야 합니다.

- [ ] GitHub webhook 연동 (선택)

---

## 1️⃣3️⃣ 배포 및 최종 확인

### 13-1. 최종 동작 확인 체크리스트

- [ ] `docker compose ps` → 모든 컨테이너 `Up`
- [ ] `http://서버IP/` → React 화면 정상
- [ ] `http://서버IP:8080/actuator/health` (또는 메인 API) → 200 OK
- [ ] `http://서버IP:8081/` → Jenkins 정상
- [ ] 코드 변경 → `git push` → Jenkins 자동 빌드 → 새 버전 반영

### 13-2. 자주 쓰는 운영 명령어

```bash
# 전체 로그 보기
docker compose logs -f

# 특정 서비스 로그
docker compose logs -f backend

# 전체 재시작
docker compose restart

# 컨테이너 전부 내리기
docker compose down

# 다시 빌드해서 올리기
docker compose up -d --build

# 디스크 청소 (사용 안 하는 이미지 정리)
docker system prune -a
```

### 13-3. 포트 정리

| 서비스 | 포트 | URL |
|---------|------|-----|
| React (Nginx) | 80 | http://서버IP/ |
| Spring Boot | 8080 | http://서버IP:8080/ |
| Jenkins | 8081 | http://서버IP:8081/ |
| MySQL | 3306 | (내부 접속) |
| Redis | 6379 | (내부 접속) |

> 💡 외부에서 접속 안 되면:
> ```bash
> sudo ufw allow 80
> sudo ufw allow 8080
> sudo ufw allow 8081
> sudo ufw status
> ```

---

## 🛠️ 문제 해결 (자주 발생하는 이슈)

### Q1. `docker` 명령어가 `permission denied` 라고 나와요
→ 5-2 단계의 `usermod -aG docker $USER` 후 **로그아웃 후 재접속**이 필요합니다.

### Q2. Spring Boot가 MySQL에 연결 못 합니다
→ `application.properties`의 host가 docker-compose 내부에서는 `localhost`가 아닌 **서비스명(`mysql`)**이어야 합니다. compose에서는 환경변수로 오버라이드됨.

### Q3. React 빌드가 너무 느려요
→ `node_modules`이 docker volume에 마운트되지 않은지 확인. Dockerfile에서 `COPY` → `npm install` 순서가 맞아야 캐시됨.

### Q4. Jenkins에서 `docker: command not found`
→ Jenkins 컨테이너에 docker CLI가 없습니다. 아래로 해결:
```bash
docker exec -u root -it toy-jenkins bash
apt-get update && apt-get install -y docker.io
exit
```

### Q5. 포트가 이미 사용 중이라고 에러
```bash
sudo lsof -i :8080      # 어떤 프로세스가 점유 중인지 확인
sudo kill -9 PID번호
```

### Q6. `git push` 시 SSH 권한 거부
→ `ssh -T git@github.com` 테스트부터. 안 되면 2-3단계 SSH 키 다시 확인.

---

## 📚 다음에 공부하면 좋을 것들

- [ ] **Nginx 리버스 프록시**로 80번 포트 하나로 백/프론트 통합
- [ ] **HTTPS** (Let's Encrypt + Certbot)로 인증서 적용
- [ ] **환경변수 분리** (`.env` 파일 활용)
- [ ] **Spring Security** 로 인증/인가
- [ ] **GitHub Actions** (`.github/workflows/*.yml`)로 Jenkins 대체
- [ ] **Kubernetes (k8s)** 로 컨테이너 오케스트레이션

---

## 🎯 마무리

이 가이드를 모두 완료하면 다음을 직접 해낸 셈입니다:

✓ 리눅스 서버에 개발 환경 직접 구축
✓ Docker로 인프라 격리 운영
✓ 풀스택 앱(Spring + React) 빌드 & 배포
✓ Git 기반 협업 환경 구성
✓ Jenkins 자동 빌드 파이프라인

막히는 단계가 생기면 해당 섹션의 명령어 결과를 보고 어떤 메시지가 나왔는지 확인하는 게 가장 빠른 디버깅입니다. 화이팅!
