#!/bin/bash
# setup.sh — Initial project setup script

set -e

echo "=== EventHub Setup Script ==="

# 1. Check Java
echo "[1/4] Checking Java version..."
java -version 2>&1 | head -1

# 2. Check Maven wrapper
echo "[2/4] Verifying Maven wrapper..."
chmod +x ./mvnw

# 3. Copy .env.example to .env if .env doesn't exist
if [ ! -f .env ]; then
  echo "[3/4] Creating .env from .env.example..."
  cp .env.example .env
  echo "  → Edit .env and fill in your credentials!"
else
  echo "[3/4] .env already exists, skipping."
fi

# 4. Download dependencies
echo "[4/4] Downloading Maven dependencies..."
./mvnw dependency:resolve -q

echo ""
echo "=== Setup complete! ==="
echo "Next steps:"
echo "  1. Edit .env with your PostgreSQL and Resend credentials"
echo "  2. Run: ./mvnw spring-boot:run"
echo "  3. Open: http://localhost:8080"
