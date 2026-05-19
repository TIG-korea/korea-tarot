# AGENTS.md instructions for c:\Users\adminn\Desktop\korea-tarot

<INSTRUCTIONS>
<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->

## Project Structure

- `specs/`: Spec Kit feature specifications, plans, tasks, and checklists.
- `front/`: Frontend application code.
- `backend/`: Backend API and service code.
- `ai-server/`: AI-serving, model orchestration, and related server code.
- `docs/project-rules.md`: Additional project workflow and package structure rules.
- `docs/issues/`: Local issue fallback documents when GitHub Issue creation is unavailable.

## Required Local Rules

Before implementation, read `docs/project-rules.md` together with the active spec documents.

Backend domain packages must be split by role instead of placing every class at the domain root:

```text
com.koreatarot.{domain}/
  controller/
  dto/
  service/
  entity/
  repository/
  enums/
  config/
  security/
```

JWT 인증/인가처럼 여러 도메인에서 공유되는 보안 인프라는 도메인 패키지가 아니라 `com.koreatarot.global.security`에서 관리한다.

Frontend UI is already user-managed. Do not create new UI unless explicitly requested; wire existing UI to API/state/validation/SSE only.
</INSTRUCTIONS>
