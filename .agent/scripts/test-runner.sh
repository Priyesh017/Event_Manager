#!/bin/bash
# test-runner.sh — Run all tests and generate report

set -e

echo "=== Running EventHub Tests ==="

# Run tests with H2 profile
./mvnw test -Dspring.profiles.active=test

echo ""
echo "=== Test Report ==="
echo "Reports available at: target/surefire-reports/"
