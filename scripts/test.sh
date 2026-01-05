#!/bin/bash
# Blood Moon - Test Script

set -e  # Exit on error

echo "🌙 Blood Moon - Running Tests"
echo "================================"

# Navigate to project root
cd "$(dirname "$0")/.."

echo ""
echo "🧪 Running unit tests..."
./gradlew test

echo ""
echo "📊 Test results:"
echo "   app/build/reports/tests/testDebugUnitTest/index.html"

echo ""
echo "✅ Unit tests complete!"

# Check for connected devices for instrumented tests
echo ""
echo "🔍 Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)

if [ "$DEVICES" -gt 0 ]; then
    echo "   Found $DEVICES device(s)"
    echo ""
    read -p "Run instrumented tests? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo ""
        echo "🧪 Running instrumented tests..."
        ./gradlew connectedAndroidTest
        echo ""
        echo "📊 Instrumented test results:"
        echo "   app/build/reports/androidTests/connected/index.html"
    fi
else
    echo "   No devices connected. Skipping instrumented tests."
    echo "   Connect a device or start an emulator to run instrumented tests."
fi

echo ""
echo "================================"
echo "🩸 Testing complete 🌙"
