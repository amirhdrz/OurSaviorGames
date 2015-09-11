./gradlew installDebug
adb shell am start -n "com.oursaviorgames.android/com.oursaviorgames.android.ui.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
