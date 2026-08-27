# Sha Development Rules

## Core principle

Sha is the assistant/orchestrator.
The LLM is replaceable and must not be treated as Sha itself.

## Project

- Sha backend is a Java 21 / Spring Boot project.
- Backend source code is primarily Java files ending in `.java`.
- When looking for a class, search the repository for the actual `.java` file.
- Never invent or assume file paths, packages, classes, methods, or APIs.
- Before explaining how a class works, read the actual source file.

## Development rules

- Talk less and in bullet points. 
- Inspect the existing code before proposing changes.
- Preserve the existing architecture unless there is a real reason to change it.
- Reuse existing Skills and contracts.
- Do not rewrite working components unnecessarily.
- Do not invent classes, methods, or APIs without checking the repository.
- Keep changes small and incremental.
- Explain important architectural changes before implementing them.
- Do not modify files unless explicitly instructed.
- Do not delete or overwrite project files without explicit permission.
- Run appropriate tests after making changes.

## Tool usage

- Do not use the web to understand the local Sha codebase unless explicitly requested.
- Prefer repository/filesystem tools for project inspection.
- When asked to inspect the project, search the actual repository first.

## Accuracy

- Never reconstruct or summarize code from assumptions.
- When describing a class, method, field, or interaction, inspect the actual source first.
- If the source has not been read, explicitly say that it has not been verified.
- Do not provide example implementations while describing the existing code unless clearly labeled as examples.
- Distinguish between "the code currently does" and "what could be implemented."

## When investigating code
1. Search the repository first.
2. Use Glob/Search to locate files.
3. Read the actual source.
4. Trace references between classes.
5. Only then provide conclusions.