#!/usr/bin/env bash
#
# Publish the Crest documentation site to GitHub Pages.
#
# Builds the Hugo site and pushes the output to the gh-pages branch.
# The first run creates the branch automatically.
#
# Usage:
#   ./publish.sh            # Build and publish
#   ./publish.sh --dry-run  # Build only, show what would be published
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_DIR="$SCRIPT_DIR/public"
WORK_DIR=$(mktemp -d)
DRY_RUN=false

[[ "${1:-}" == "--dry-run" ]] && DRY_RUN=true

# ── Colors ──
red()   { printf '\033[0;31m%s\033[0m\n' "$*"; }
green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
bold()  { printf '\033[1m%s\033[0m\n' "$*"; }

cleanup() { rm -rf "$WORK_DIR"; }
trap cleanup EXIT

# ── Preflight ──
if ! command -v hugo &>/dev/null; then
    red "Hugo is not installed. See https://gohugo.io/installation/"
    exit 1
fi

cd "$REPO_ROOT"

if ! git remote get-url origin &>/dev/null; then
    red "No git remote 'origin' found."
    exit 1
fi

REMOTE_URL=$(git remote get-url origin)

# Check for uncommitted changes in docs/
if ! git diff --quiet -- docs/; then
    red "You have uncommitted changes in docs/. Commit or stash them first."
    exit 1
fi

COMMIT_SHA=$(git rev-parse --short HEAD)
COMMIT_MSG="Publish docs from $COMMIT_SHA"

# ── Build ──
bold "Building site..."
cd "$SCRIPT_DIR"
rm -rf "$BUILD_DIR"
hugo --minify
echo ""

FILE_COUNT=$(find "$BUILD_DIR" -type f | wc -l | tr -d ' ')
green "Built $FILE_COUNT files."

if $DRY_RUN; then
    echo ""
    bold "Dry run — skipping publish. Output is in docs/public/"
    exit 0
fi

# ── Publish ──
bold "Publishing to gh-pages..."

cd "$WORK_DIR"
git init -q
git remote add origin "$REMOTE_URL"

# Try to pull existing gh-pages branch
if git ls-remote --exit-code --heads origin gh-pages &>/dev/null; then
    git fetch -q origin gh-pages
    git checkout -q gh-pages
    # Clear old content but keep .git
    find . -maxdepth 1 ! -name '.git' ! -name '.' -exec rm -rf {} +
else
    git checkout -q --orphan gh-pages
fi

# Copy new build output
cp -a "$BUILD_DIR"/. .

# Add .nojekyll so GitHub serves the site as-is
touch .nojekyll

git add -A
if git diff --cached --quiet; then
    green "No changes to publish."
    exit 0
fi

git commit -q -m "$COMMIT_MSG"
git push -q origin gh-pages

echo ""
green "Published! Site will be available at:"
echo ""

# Derive the GitHub Pages URL from the remote
if [[ "$REMOTE_URL" =~ github\.com[:/]([^/]+)/([^/.]+) ]]; then
    ORG="${BASH_REMATCH[1]}"
    REPO="${BASH_REMATCH[2]}"
    echo "  https://$ORG.github.io/$REPO/"
else
    echo "  (check your GitHub Pages settings)"
fi
