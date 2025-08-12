# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Passage** is a Kotlin Multiplatform library for Firebase authentication, supporting Android and iOS platforms. It abstracts authentication providers ("Gatekeepers") like Google, Apple, and Email/Password into a unified API.

## Key Architecture

### Core Concepts
- **Passage**: Main entry point class - platform-specific implementations (`PassageAndroid`, `PassageIos`)
- **Gatekeepers**: Authentication provider abstractions (`PassageGatekeeper<T>`) - handle specific auth methods
- **Entrants**: Authenticated users returned from Firebase operations

### Module Structure
- `passage/`: Main KMP library module with platform-specific source sets
- `sample/composeApp/`: Demo app showing library usage
- `buildSrc/`: Build configuration and custom Gradle tasks

### Platform Architecture
- `commonMain/`: Shared business logic and abstractions
- `androidMain/`: Android-specific implementations (Activity context handling)
- `iosMain/`: iOS-specific implementations (CocoaPods integration)

## Development Commands

### Building
```bash
./gradlew build                    # Full build for all targets
./gradlew assemble                 # Assemble without tests
./gradlew clean                    # Clean build artifacts
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew allTests                 # Run tests for all targets with report
./gradlew iosSimulatorArm64Test    # Run iOS simulator tests
./gradlew testDebugUnitTest        # Run Android debug tests only
```

### Verification & Quality
```bash
./gradlew check                    # Run all verification tasks
./gradlew lint                     # Run Android lint
./gradlew lintFix                  # Apply lint fixes automatically
```

### Documentation
```bash
./gradlew dokkaHtml                # Generate API documentation
./gradlew dokkaGfm                 # Generate markdown docs
```

### Publishing (Library Maintainers)
```bash
./gradlew publishToMavenCentral    # Publish to Maven Central
```

## Firebase Integration

The library is built on Firebase Authentication using the GitLive Firebase KMP SDK (`dev.gitlive.firebase`). Key integration points:

- Firebase initialization happens in platform-specific `initializeFirebase()` methods
- Authentication state flows through `FirebaseAuth.authStateChanged`
- Universal links for email actions are handled via Firebase Dynamic Links

## Platform-Specific Implementation Notes

### Android
- Google Sign-In requires Activity context binding via `bindToView()` in Compose
- Uses Credential Manager API and GoogleSignIn dependencies
- Targets minSDK 24, compileSDK 35

### iOS  
- CocoaPods integration for GoogleSignIn framework
- iOS deployment target: 12.0
- Framework binaries built as static frameworks

## Authentication Providers ("Gatekeepers")

Each gatekeeper implements the `PassageGatekeeper<SignInParams>` abstract class:

- `PassageGoogleGatekeeper`: Handles Google Sign-In
- `PassageAppleGatekeeper`: Handles Apple Sign-In (iOS only)
- `PassageEmailGatekeeper`: Handles email/password and email link authentication

## Universal Links & Email Actions

The library handles Firebase Dynamic Links for:
- Email verification
- Password reset flows  
- Sign-in link authentication

Processing occurs through `PassageUniversalLinkHandler` which exposes a `StateFlow<PassageUniversalLink?>`.

## Configuration Management

Build configuration is centralized in `buildSrc/src/main/kotlin/ProjectConfiguration.kt`:
- Version management
- SDK versions and compatibility
- Maven publishing metadata
- Platform-specific settings

## Testing Strategy

- Unit tests target the common module business logic
- Platform-specific tests verify integration points
- Sample app serves as integration testing vehicle