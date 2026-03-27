#!/bin/bash
# Autoresearch Loop Script for Jules-Driven Development
# Usage: ./autoresearch.sh [iteration_number]

set -e

# Configuration
REPO_DIR="/tmp/android-video-downloader"
REPO_URL="https://github.com/hendr15k/android-video-downloader"
JULES_CLI="/home/openclaw/.local/bin/jules"
RESULTS_FILE="$REPO_DIR/results.tsv"
POLL_INTERVAL=60
MAX_WAIT=1800  # 30 minutes max per iteration

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { echo -e "[$(date '+%H:%M:%S')] $1"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠️  $1${NC}"; }

# Check if Jules API key is set
if [ -z "$JULES_API_KEY" ]; then
    error "JULES_API_KEY not set"
    exit 1
fi

# Pull latest changes
pull_latest() {
    cd "$REPO_DIR"
    git pull origin main 2>/dev/null || true
    log "Pulled latest from main"
}

# Analyze current state
analyze_state() {
    log "Analyzing current codebase..."
    
    cd "$REPO_DIR"
    
    # Check for Gradle files
    if [ -f "build.gradle.kts" ] || [ -f "app/build.gradle.kts" ]; then
        success "Gradle files exist"
        GRADLE_EXISTS=1
    else
        warn "No Gradle files found"
        GRADLE_EXISTS=0
    fi
    
    # Count Kotlin files
    KOTLIN_COUNT=$(find . -name "*.kt" -type f 2>/dev/null | wc -l)
    log "Kotlin files: $KOTLIN_COUNT"
    
    # Check for key directories
    if [ -d "app/src/main/java" ]; then
        success "Source directory exists"
        SRC_EXISTS=1
    else
        warn "No source directory"
        SRC_EXISTS=0
    fi
    
    # Check for AndroidManifest
    if [ -f "app/src/main/AndroidManifest.xml" ]; then
        success "AndroidManifest exists"
        MANIFEST_EXISTS=1
    else
        warn "No AndroidManifest"
        MANIFEST_EXISTS=0
    fi
    
    # Calculate feature score
    SCORE=0
    [ $GRADLE_EXISTS -eq 1 ] && SCORE=$((SCORE + 10))
    [ $SRC_EXISTS -eq 1 ] && SCORE=$((SCORE + 10))
    [ $MANIFEST_EXISTS -eq 1 ] && SCORE=$((SCORE + 10))
    SCORE=$((SCORE + KOTLIN_COUNT))
    
    log "Current feature score: $SCORE/100"
    
    echo "$GRADLE_EXISTS $SRC_EXISTS $MANIFEST_EXISTS $KOTLIN_COUNT $SCORE"
}

# Generate next task prompt for Jules
generate_task_prompt() {
    local state_output=$1
    
    read GRADLE SRC MANIFEST KOTLIN SCORE <<< "$state_output"
    
    if [ $GRADLE -eq 0 ]; then
        TASK="Set up the Android project structure: create build.gradle.kts files (root and app module), settings.gradle.kts, and gradle.properties. Use Kotlin DSL."
    elif [ $SRC -eq 0 ]; then
        TASK="Create the source directory structure for Clean Architecture: app/src/main/java/com/hendrik/videodownloader/ with ui/, domain/, data/, di/ packages."
    elif [ $MANIFEST -eq 0 ]; then
        TASK="Create AndroidManifest.xml with required permissions: Internet, Write External Storage, Foreground Service, Post Notifications."
    elif [ $KOTLIN -lt 5 ]; then
        TASK="Create the core Kotlin files: VideoDownloaderApp.kt (Hilt application), MainActivity.kt (Compose activity), and basic theme files."
    elif [ $KOTLIN -lt 15 ]; then
        TASK="Implement the URL input feature: HomeScreen.kt with TextField, paste button, and video URL validation. Add ViewModel with StateFlow."
    elif [ $KOTLIN -lt 30 ]; then
        TASK="Implement video extraction: Create VideoExtractor.kt that parses HTML for video URLs, handles m3u8 streams, and extracts metadata."
    elif [ $KOTLIN -lt 50 ]; then
        TASK="Implement download manager: Create DownloadManager.kt using WorkManager for background downloads with progress notifications."
    else
        TASK="Polish the app: Add error handling, improve UI animations, write unit tests for core components, and update documentation."
    fi
    
    echo "$TASK"
}

# Start Jules session
start_jules_session() {
    local task="$1"
    
    log "Starting Jules session..."
    log "Task: $task"
    
    cd "$REPO_DIR"
    
    $JULES_CLI task "Continue developing the Android Video Downloader app.

CURRENT STATE: Review the existing codebase to understand what's implemented.

NEXT TASK: $task

Focus on production-quality code with proper error handling. Follow Clean Architecture principles." 2>&1 || true
}

# Poll for session completion
poll_session() {
    local session_id="$1"
    local waited=0
    
    log "Polling session $session_id (max ${MAX_WAIT}s)..."
    
    while [ $waited -lt $MAX_WAIT ]; do
        STATE=$($JULES_CLI session show "$session_id" 2>/dev/null | grep '"state"' | head -1 | sed 's/.*"state": "\([^"]*\)".*/\1/')
        
        case "$STATE" in
            "COMPLETED")
                success "Session completed!"
                return 0
                ;;
            "FAILED")
                error "Session failed!"
                return 1
                ;;
            "IN_PROGRESS"|"QUEUED")
                log "State: $STATE (waited ${waited}s)"
                sleep $POLL_INTERVAL
                waited=$((waited + POLL_INTERVAL))
                ;;
            *)
                warn "Unknown state: $STATE"
                sleep $POLL_INTERVAL
                waited=$((waited + POLL_INTERVAL))
                ;;
        esac
    done
    
    error "Timeout waiting for session"
    return 1
}

# Record result
record_result() {
    local session_id="$1"
    local feature="$2"
    local build_status="$3"
    local score="$4"
    local status="$5"
    local notes="$6"
    
    TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%SZ')
    echo -e "$TIMESTAMP\t$session_id\t$feature\t$build_status\t$score\t$status\t$notes" >> "$RESULTS_FILE"
    log "Recorded result to $RESULTS_FILE"
}

# Main loop
main() {
    local iteration=${1:-1}
    
    log "=========================================="
    log "AutoResearch Iteration #$iteration"
    log "=========================================="
    
    # Pull latest
    pull_latest
    
    # Analyze state
    STATE_OUTPUT=$(analyze_state)
    STATE_ARRAY=($STATE_OUTPUT)
    SCORE=${STATE_ARRAY[4]}
    
    # Check if we're done
    if [ $SCORE -ge 90 ]; then
        success "App is nearly complete! Score: $SCORE/100"
        log "Consider manual review and polish."
        exit 0
    fi
    
    # Generate next task
    TASK=$(generate_task_prompt "$STATE_OUTPUT")
    
    # Start Jules session (we'll need to do this differently since jules CLI has timeout)
    log "To continue, run:"
    echo ""
    echo "  $JULES_CLI task \"$TASK\""
    echo ""
    log "Then poll with: $JULES_CLI session show <session_id>"
    
    # For now, output the current status
    log "Current state summary:"
    log "  - Gradle files: ${STATE_ARRAY[0]}"
    log "  - Source dir: ${STATE_ARRAY[1]}"
    log "  - Manifest: ${STATE_ARRAY[2]}"
    log "  - Kotlin files: ${STATE_ARRAY[3]}"
    log "  - Score: ${STATE_ARRAY[4]}/100"
}

main "$@"