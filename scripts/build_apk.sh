#!/bin/bash
# Blood Moon - Build APK Script

set -e  # Exit on error

echo "🌙 Blood Moon - Building APK"
echo "================================"

# Navigate to project root
cd "$(dirname "$0")/.."

echo ""
echo "📦 Cleaning previous builds..."
./gradlew clean

echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug

echo ""
echo "✅ Build complete!"
echo ""
echo "📱 APK location:"
echo "   app/build/outputs/apk/debug/app-debug.apk"
echo ""

# Check if APK exists
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
    echo "   Size: $APK_SIZE"
    echo ""
    echo "🚀 To install on connected device:"
    echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "   Or use: ./gradlew installDebug"
else
    echo "❌ Error: APK not found!"
    exit 1
fi

echo "================================"
echo "🩸 Blood Moon build complete 🌙"
