---
name: git
description: Git workflow helpers
---

# Git workflow

1. Inspect status: `git status -sb`.
2. Stage intentionally: prefer `git add -p` for risky hunks.
3. Write commits that match repository conventions (message style, scope).
4. Before pushing: `git pull --rebase` when the team uses rebasing.
