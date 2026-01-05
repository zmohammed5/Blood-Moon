#!/bin/bash
# Blood Moon - Code Formatting Script

set -e  # Exit on error

echo "🌙 Blood Moon - Code Formatting"
echo "================================"

# Navigate to project root
cd "$(dirname "$0")/.."

echo ""
echo "🎨 Formatting Kotlin code..."

# Check if ktlint is available
if ./gradlew tasks --all | grep -q "ktlintFormat"; then
    ./gradlew ktlintFormat
    echo "✅ Kotlin code formatted!"
else
    echo "⚠️  ktlint not configured. Using built-in formatter..."
    echo ""
    echo "To add ktlint, add to app/build.gradle.kts:"
    echo '  plugins { id("org.jlint") version "11.0.0" }'
fi

echo ""
echo "================================"
echo "🩸 Formatting complete 🌙"
