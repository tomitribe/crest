#!/usr/bin/env bash
#
# Crest documentation site — build and serve
#
# Usage:
#   ./site.sh              # Build and serve with live reload
#   ./site.sh build        # Build only (output to public/)
#   ./site.sh serve        # Serve with live reload
#   ./site.sh clean        # Remove generated files
#   ./site.sh help         # Show this help
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PORT="${PORT:-1313}"
BIND="${BIND:-127.0.0.1}"

# ── Colors ──
red()   { printf '\033[0;31m%s\033[0m\n' "$*"; }
green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
bold()  { printf '\033[1m%s\033[0m\n' "$*"; }

# ── Check Hugo ──
check_hugo() {
    if ! command -v hugo &>/dev/null; then
        red "Hugo is not installed."
        echo ""
        echo "Install it with:"
        echo "  brew install hugo          # macOS"
        echo "  sudo apt install hugo      # Debian/Ubuntu"
        echo "  sudo dnf install hugo      # Fedora"
        echo ""
        echo "Or visit https://gohugo.io/installation/"
        exit 1
    fi
}

# ── Commands ──
cmd_build() {
    check_hugo
    bold "Building site..."
    hugo --minify
    echo ""
    green "Build complete. Output in public/"
}

cmd_serve() {
    check_hugo

    # Kill any existing server on our port
    lsof -ti :"$PORT" 2>/dev/null | xargs kill 2>/dev/null || true
    sleep 0.5

    bold "Starting dev server on port $PORT..."
    echo ""
    hugo server \
        --bind "$BIND" \
        --port "$PORT" \
        --buildDrafts \
        --navigateToChanged \
        --disableFastRender
}

cmd_clean() {
    bold "Cleaning generated files..."
    rm -rf public/ resources/ .hugo_build.lock
    green "Clean."
}

cmd_help() {
    echo "Crest documentation site"
    echo ""
    echo "Usage: ./site.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build    Build the site (output to public/)"
    echo "  serve    Start dev server with live reload (default)"
    echo "  clean    Remove generated files"
    echo "  help     Show this help"
    echo ""
    echo "Environment variables:"
    echo "  PORT     Server port (default: 1313)"
    echo "  BIND     Bind address (default: 127.0.0.1)"
}

# ── Main ──
case "${1:-serve}" in
    build)  cmd_build ;;
    serve)  cmd_serve ;;
    clean)  cmd_clean ;;
    help|-h|--help) cmd_help ;;
    *)
        red "Unknown command: $1"
        echo ""
        cmd_help
        exit 1
        ;;
esac
