# 빠른 시작: AI 타로 상담 서비스 MVP

## 사전 준비

- Java 21
- Node 20+
- Python 3.12
- Docker 및 Compose
- 로컬 compose로 실행되는 MySQL 8.4, Redis 7

## 로컬 실행

1. 인프라를 실행한다.

```bash
docker compose up -d mysql redis
```

2. 백엔드를 실행한다.

```bash
cd backend
./gradlew bootRun
```

3. AI 서버를 실행한다.

```bash
cd ai-server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

4. 프론트엔드를 실행한다.

```bash
cd front
npm run dev
```

## 검증 시나리오

1. 새 이메일과 필수 약관 동의로 회원가입한다.
2. 로그아웃한 뒤 같은 계정으로 다시 로그인한다.
3. 10자 이상 1000자 이하의 유효한 고민을 제출한다.
4. draft 응답에 draft ID, deck size, expiresAt만 있는지 확인한다.
5. PRESENT, OBSTACLE, ADVICE에 서로 다른 카드 3장을 선택한다.
6. Idempotency-Key와 함께 상담을 생성한다.
7. 상담 이벤트를 구독하고 `meta`, `token`, `done`을 수신한다.
8. 완료된 상담의 기록 목록과 상세를 연다.
9. 다른 사용자가 해당 상담에 접근할 수 없는지 확인한다.
10. 상담을 기록에서 삭제하고 기본 목록에서 숨겨지는지 확인한다.
