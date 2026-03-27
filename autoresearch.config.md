# Autoresearch Configuration — Jules-Driven Android Development

## Goal
Iteratively develop and improve the Android Video Downloader app using Google Jules as the autonomous coding agent.

## Metric
- **Name**: `feature_score`
- **Direction**: Higher is better
- **Components**:
  1. **Build Success** (0-20 points): Does the project compile?
  2. **Features Implemented** (0-50 points): Count of working features
  3. **Code Quality** (0-30 points): Architecture, tests, documentation

### Feature Checklist (10 points each, max 50):
- [ ] Project structure complete (Gradle files, manifest)
- [ ] URL input with paste functionality
- [ ] Video info extraction from URLs
- [ ] Quality selection (1080p, 720p, audio)
- [ ] Download manager with progress
- [ ] Download history with Room DB
- [ ] Settings screen
- [ ] Hilt dependency injection
- [ ] Material 3 UI with themes
- [ ] Unit tests

### Extract Command
```bash
# Check build
./gradlew assembleDebug 2>&1 | tail -5

# Count features
find app/src -name "*.kt" | wc -l
grep -r "fun " app/src --include="*.kt" | wc -l

# Check for key files
ls -la app/src/main/java/com/hendrik/videodownloader/ 2>/dev/null
```

## Target Files
- **All files in repo** — Jules can modify anything
- Primary focus: `app/src/main/java/com/hendrik/videodownloader/`

## Read-Only Files
- None — Jules has full autonomy

## Run Command
```bash
# Start Jules session for next development iteration
export JULES_API_KEY="${JULES_API_KEY}"
jules task "Continue developing the Android Video Downloader app. 

CURRENT STATE: Review the current codebase to understand what's implemented.

NEXT PRIORITIES:
1. Check what's missing from the feature list
2. Fix any compilation errors
3. Implement the highest-priority missing feature
4. Add tests if core features are done

Focus on ONE feature per iteration. Write production-quality code."
```

## Time Budget
- **Per experiment**: 10-15 minutes (Jules async)
- **Kill timeout**: 30 minutes
- **Polling interval**: 60 seconds

## Constraints
- Must maintain Kotlin code style
- Must use Jetpack Compose for UI
- Must follow Clean Architecture
- No external binary dependencies (like yt-dlp binary)
- Keep app under 50MB

## Branch
- `main` — Jules will create PRs automatically

## Jules Session Protocol

### Per-Iteration Workflow:

1. **ANALYZE current state:**
   ```bash
   # Check what Jules has pushed
   git pull origin main
   git log --oneline -5
   
   # Analyze codebase
   find . -name "*.kt" -type f | head -20
   ```

2. **FORM hypothesis:**
   - What's the next most valuable feature?
   - What's broken that needs fixing?
   - What can be improved?

3. **TRIGGER Jules:**
   ```bash
   jules task "Continue developing...
   CURRENT: [describe what exists]
   NEXT: [specific task for this iteration]"
   ```

4. **POLL for completion:**
   ```bash
   jules session list --limit 1
   jules session show <session_id>
   ```

5. **EVALUATE result:**
   ```bash
   git pull origin main
   ./gradlew assembleDebug
   # Score the iteration
   ```

6. **RECORD in results.tsv**

## Results File
`results.tsv` columns:
- `timestamp` — When iteration started
- `session_id` — Jules session ID
- `feature_added` — What was implemented
- `build_status` — success/fail
- `feature_score` — 0-100
- `status` — keep/discard/crash
- `notes` — Observations

## Notes
- Jules runs asynchronously — poll for status
- Jules creates PRs automatically with `automation.mode: AUTO_CREATE_PR`
- Each iteration should focus on ONE feature
- If build fails, next iteration should fix it first
- Keep prompting Jules until app is complete and polished

---

## Quick Reference

| Action | Command |
|--------|---------|
| Check Jules session | `jules session show <id>` |
| List sessions | `jules session list` |
| Pull changes | `git pull origin main` |
| Check build | `./gradlew assembleDebug` |
| Start new iteration | `jules task "Continue..."` |

---

**Setup Date:** 2026-03-27
**Repo:** https://github.com/hendr15k/android-video-downloader
**Current Jules Session:** 10519004549614842726 (IN_PROGRESS)