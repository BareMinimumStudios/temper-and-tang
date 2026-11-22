#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew check build --console=plain "$@"
python3 scripts/verify_resources.py
python3 scripts/verify_artifacts.py
python3 scripts/prepare_release.py
