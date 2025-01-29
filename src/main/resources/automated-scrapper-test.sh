#!/bin/bash

# Server URL
API_URL="http://localhost:8081/api/v0/search?keyword="

# List of 50 different keywords
KEYWORDS=("linux" "ubuntu" "bash" "terminal" "docker" "kubernetes" "java" "python"
          "golang" "javascript" "typescript" "spring" "nodejs" "nestjs" "mysql"
          "postgresql" "mongodb" "redis" "elasticsearch" "git" "github"
          "bitbucket" "devops" "cloud" "aws" "azure" "gcp" "ci/cd" "terraform"
          "ansible" "kafka" "rabbitmq" "nginx" "apache" "serverless" "graphql"
          "restapi" "microservices" "bigdata" "ai" "machinelearning" "deeplearning"
          "pytorch" "tensorflow" "numpy" "pandas" "linuxcommands" "vim" "emacs"
          "zsh" "powershell" "bashscripting")

# Counter for total links retrieved
TOTAL_LINKS=0

# Loop through each keyword
for keyword in "${KEYWORDS[@]}"; do
    echo "Searching for: $keyword"

    # Call the API and capture the response
    RESPONSE=$(curl -s "${API_URL}${keyword}")

    # Extract the number of results using jq
    LINKS_COUNT=$(echo "$RESPONSE" | jq '. | length')

    # Add to total count
    TOTAL_LINKS=$((TOTAL_LINKS + LINKS_COUNT))

    # Print results for this keyword
    echo "Links found for '$keyword': $LINKS_COUNT"
    echo "--------------------------------------------"

    # Wait for 1 second before next request
    sleep 1
done

# Print final result
echo "✅ Total links retrieved from all searches: $TOTAL_LINKS"
echo "🎉 Search process completed successfully!"

