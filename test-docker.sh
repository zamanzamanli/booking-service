#!/bin/bash
# Script to run tests in Docker and clean up afterward

set -e

echo "🧪 Running tests in Docker..."
docker-compose --profile test run --rm test ./mvnw test "$@"

echo ""
echo "✅ Tests completed!"
echo ""
echo "🧹 Cleaning up Testcontainers..."
# Clean up any leftover Testcontainers (since Ryuk is disabled on macOS)
docker ps -a | grep testcontainers | awk '{print $1}' | xargs docker rm -f 2>/dev/null || true

echo "✨ Done!"
