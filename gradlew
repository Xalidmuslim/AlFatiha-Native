#!/usr/bin/env bash
set -e
GRADLE_VERSION=8.9
BASE="$HOME/.gradle/manual-dists/gradle-$GRADLE_VERSION"
ZIP="$HOME/.gradle/manual-dists/gradle-$GRADLE_VERSION-bin.zip"
mkdir -p "$HOME/.gradle/manual-dists"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
if [ ! -x "$BASE/bin/gradle" ]; then
  echo "Gradle $GRADLE_VERSION is not installed. Downloading it once..."
  if [ ! -f "$ZIP" ]; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  fi
  rm -rf "$BASE" "$HOME/.gradle/manual-dists/gradle-$GRADLE_VERSION.tmp"
  unzip -q "$ZIP" -d "$HOME/.gradle/manual-dists"
fi
exec "$BASE/bin/gradle" "$@"
