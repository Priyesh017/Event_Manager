#!/bin/bash

# Navigate to project root
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR/.."

if [ "$#" -ne 2 ]; then
    echo -e "\033[0;31mError: Missing arguments.\033[0m"
    echo "Usage: ./scripts/add-admin.sh <email> <password>"
    echo "Example: ./scripts/add-admin.sh newadmin@events.com SecurePass123!"
    exit 1
fi

EMAIL=$1
PASSWORD=$2

echo -e "\033[0;36m🚀 Starting EventHub Admin Creation Tool...\033[0m"
echo -e "\033[0;36mThis will briefly start the Spring Boot context to create the admin in the database.\033[0m"

# Run maven wrapper and pass the args. We disable the web server so it runs quickly and avoids port conflicts.
./mvnw spring-boot:run -Dspring-boot.run.arguments="--add-admin=${EMAIL}:${PASSWORD} --spring.main.web-application-type=none" -q

echo -e "\033[0;32m✅ Done.\033[0m"
