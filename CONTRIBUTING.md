# Contributing to Blood Moon

Thank you for considering contributing to Blood Moon! We welcome contributions from everyone.

## Code of Conduct

Be respectful, inclusive, and constructive. This is a privacy-focused health app - treat user data and privacy with the utmost care.

## How Can I Contribute?

### Reporting Bugs

- Use the GitHub issue tracker
- Include Android version, device model
- Describe steps to reproduce
- Include logcat output if possible
- Screenshots are helpful

### Suggesting Features

- Check existing issues first
- Explain the use case
- Consider privacy implications
- Discuss on Discord if complex

### Pull Requests

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes**
4. **Write or update tests**
5. **Update documentation**
6. **Commit your changes** (`git commit -m 'Add amazing feature'`)
7. **Push to your fork** (`git push origin feature/amazing-feature`)
8. **Open a Pull Request**

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 26-34

### Building

```bash
git clone https://github.com/yourusername/Blood-Moon.git
cd Blood-Moon
./gradlew assembleDebug
```

### Running Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Comment complex logic
- Keep functions small and focused
- Prefer immutability (`val` over `var`)

## Privacy Guidelines

**CRITICAL**: Blood Moon is a privacy-first app. When contributing:

- ✅ **DO**: Keep all data local
- ✅ **DO**: Use encryption for sensitive data
- ✅ **DO**: Minimize permissions
- ✅ **DO**: Document data handling

- ❌ **DON'T**: Add internet connectivity
- ❌ **DON'T**: Add analytics or tracking
- ❌ **DON'T**: Log sensitive data
- ❌ **DON'T**: Add third-party SDKs without discussion

## Architecture

- **MVVM** pattern
- **Repository** for data abstraction
- **Jetpack Compose** for UI
- **Room + SQLCipher** for encrypted database
- **Kotlin Coroutines** for async operations

## Testing

- Unit tests for business logic (required)
- UI tests for critical flows (encouraged)
- All tests must pass before merge

## Documentation

Update documentation when you:
- Add new features
- Change public APIs
- Modify data structures
- Update dependencies

## Commit Messages

Use conventional commits:

```
feat: Add backup encryption
fix: Resolve PIN verification issue
docs: Update README
test: Add cycle prediction tests
refactor: Simplify database layer
```

## Review Process

1. Automated checks must pass
2. Code review by maintainer
3. Privacy review for data-related changes
4. Merge when approved

## Questions?

- Open an issue for questions
- Join Discord for discussion
- Email: [maintainer email]

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for helping make Blood Moon better! 🌙🩸
