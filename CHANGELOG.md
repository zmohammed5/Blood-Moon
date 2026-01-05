# Changelog

All notable changes to Blood Moon will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Data export and import with encryption
- Calendar screen with period/fertile day highlighting
- Period logging screen (flow, symptoms, mood, notes)
- Personal notes management screen
- GitHub Actions release workflow
- F-Droid metadata
- Comprehensive documentation

### Fixed
- PIN verification now works correctly (was broken due to async issue)
- Import organization in MainActivity
- Removed unused kotlinx.serialization imports

### Changed
- "Metal Mode" renamed to "Alternate Theme" in UI
- Improved coroutine usage (removed GlobalScope)

## [1.0.0] - 2026-01-05

### Added
- Initial release
- 100% offline operation (no internet permission)
- Full SQLCipher database encryption
- Period tracking with spotting option (dates, flow, symptoms, mood, notes)
- Scientific prediction engine based on peer-reviewed research
- Normal distribution-based period probability calculations
- Conception probability tracking (Wilcox et al. 1995)
- Safe days calculation (<5% conception probability)
- PCOS medication tracking (Metformin, Progesterone, Spironolactone)
- Adaptive luteal phase calculation
- Fertile window prediction with toggleable display
- PIN and biometric lock support
- Moony pixel-art mascot with animations
- Moon phase calculation and display
- Gothic dark theme + alternate theme
- Personal notes system
- Daily affirmations
- Comprehensive test coverage
- Complete documentation with scientific citations

### Privacy Features
- No internet permission
- No analytics or tracking
- No data collection
- Full encryption at rest
- Optional PIN/biometric lock
- All data stays on device

