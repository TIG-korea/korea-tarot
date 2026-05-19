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

Frontend UI is already user-managed. Do not create new UI unless explicitly requested; wire existing UI to API/state/validation/SSE only.
</INSTRUCTIONS>
