# 🛠️ Developer Guide

## Project Setup

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or newer
- **Android SDK**: API 26-34
- **Gradle**: 8.2+ (included via wrapper)
- **Git**: For version control

### Clone & Build

```bash
# Clone repository
git clone https://github.com/[username]/Blood-Moon.git
cd Blood-Moon

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## Project Structure

```
Blood-Moon/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bloodmoon/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Database, entities, DAOs
│   │   │   │   │   ├── repository/     # Data repositories
│   │   │   │   │   └── music/          # Music playback service
│   │   │   │   ├── domain/             # Business logic
│   │   │   │   │   ├── model/          # Domain models
│   │   │   │   │   └── CyclePredictionEngine.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # Reusable UI components
│   │   │   │   │   ├── screens/        # App screens
│   │   │   │   │   ├── theme/          # Compose theme
│   │   │   │   │   └── MainViewModel.kt
│   │   │   │   ├── util/               # Utilities
│   │   │   │   ├── BloodMoonApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                    # Resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                       # Unit tests
│   │   └── androidTest/                # Instrumented tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── assets/                             # SVG assets (Moony, decorations)
├── docs/                               # Documentation
├── scripts/                            # Build & utility scripts
├── gradle/                             # Gradle wrapper
├── build.gradle.kts                    # Root build file
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## Architecture

### MVVM Pattern

**Model-View-ViewModel architecture:**

```
┌─────────────────┐
│   UI (Compose)  │  ← View
└────────┬────────┘
         │
┌────────▼────────┐
│   ViewModel     │  ← Presentation logic
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │  ← Data abstraction
└────────┬────────┘
         │
┌────────▼────────┐
│  Room + DAO     │  ← Data persistence
└─────────────────┘
```

### Layers

1. **Data Layer**
   - Entities (database models)
   - DAOs (data access objects)
   - Database (Room + SQLCipher)
   - Repositories (abstraction)

2. **Domain Layer**
   - Business logic (CyclePredictionEngine)
   - Domain models (CyclePrediction, CycleStatus)
   - Use cases (if needed)

3. **Presentation Layer**
   - ViewModels (state management)
   - UI State (data classes)
   - Compose UI (screens, components)

---

## Key Technologies

### Jetpack Compose

**UI Framework:**
- Declarative UI
- Kotlin-first
- Material 3 components
- Custom theme

**Best Practices:**
- Use `remember` for state
- Hoist state when possible
- Keep composables small and focused
- Preview composables with `@Preview`

**Example:**
```kotlin
@Composable
fun CycleInfoCard(prediction: CyclePrediction) {
    Card {
        Text("Cycle Day ${prediction.cycleDay}")
    }
}

@Preview
@Composable
fun PreviewCycleInfoCard() {
    BloodMoonTheme {
        CycleInfoCard(samplePrediction)
    }
}
```

### Room + SQLCipher

**Encrypted Database:**
```kotlin
@Database(
    entities = [PeriodLog::class, PartnerNote::class, AppSettings::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class BloodMoonDatabase : RoomDatabase()
```

**TypeConverters:**
- LocalDate ↔ String
- List<String> ↔ String (comma-separated)
- Map<String, String> ↔ String (key:value;key:value)
- FlowIntensity ↔ String (enum name)

### Kotlin Coroutines & Flow

**Asynchronous Operations:**
```kotlin
viewModelScope.launch {
    periodRepository.getAllLogs().collect { logs ->
        // Update UI state
    }
}
```

**Repository Pattern:**
```kotlin
fun getAllLogs(): Flow<List<PeriodLog>> = periodLogDao.getAllLogs()
```

---

## Adding New Features

### 1. Add Database Entity

```kotlin
// 1. Create entity
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: String
)

// 2. Create DAO
@Dao
interface NewEntityDao {
    @Query("SELECT * FROM new_table")
    fun getAll(): Flow<List<NewEntity>>

    @Insert
    suspend fun insert(entity: NewEntity)
}

// 3. Add to database
@Database(entities = [/* existing */, NewEntity::class], version = 2)
abstract class BloodMoonDatabase : RoomDatabase() {
    abstract fun newEntityDao(): NewEntityDao
}

// 4. Create repository
class NewEntityRepository(private val dao: NewEntityDao) {
    fun getAll() = dao.getAll()
    suspend fun insert(entity: NewEntity) = dao.insert(entity)
}
```

### 2. Add New Screen

```kotlin
// 1. Create screen composable
@Composable
fun NewScreen(/* params */) {
    Column {
        Text("New Screen")
    }
}

// 2. Add to navigation
sealed class Screen(val route: String) {
    // existing
    object NewScreen : Screen("new_screen", "New", Icons.Default.Star)
}

// 3. Add to NavHost
composable(Screen.NewScreen.route) {
    NewScreen()
}
```

### 3. Add ViewModel

```kotlin
class NewViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewUiState())
    val uiState: StateFlow<NewUiState> = _uiState.asStateFlow()

    fun onAction() {
        viewModelScope.launch {
            // Handle action
        }
    }
}
```

---

## Testing

### Unit Tests

**Location:** `app/src/test/`

**Example:**
```kotlin
class CyclePredictionEngineTest {
    private lateinit var engine: CyclePredictionEngine

    @Before
    fun setup() {
        engine = CyclePredictionEngine()
    }

    @Test
    fun `test regular cycle prediction`() {
        val logs = listOf(/* test data */)
        val prediction = engine.predictNextCycle(logs)

        assertEquals(28, prediction.cycleLength)
    }
}
```

**Run:**
```bash
./gradlew test
```

### Instrumented Tests

**Location:** `app/src/androidTest/`

**Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    @Test
    fun testDatabaseInsertion() {
        // Test with actual Android context
    }
}
```

**Run:**
```bash
./gradlew connectedAndroidTest
```

---

## Code Style

### Kotlin Coding Conventions

- **Naming:**
  - Classes: PascalCase
  - Functions: camelCase
  - Constants: SCREAMING_SNAKE_CASE
  - Private vars: camelCase (no underscore prefix)

- **Formatting:**
  - 4-space indentation
  - Line length: 120 characters max
  - Use trailing commas in multiline

- **Best Practices:**
  - Prefer `val` over `var`
  - Use data classes for models
  - Null safety (avoid `!!`)
  - Prefer lambdas with trailing syntax

**Auto-format:**
```bash
./gradlew ktlintFormat
```

### Compose Guidelines

- **State Hoisting:** Lift state up when shared
- **Reusability:** Extract common UI to components
- **Modifiers:** Pass as parameter for flexibility
- **Previews:** Add `@Preview` for all composables

---

## Debugging

### Common Issues

**1. Database Encryption Error**
```
net.sqlcipher.database.SQLiteException: file is not a database
```
**Fix:** Delete app data or change encryption key algorithm

**2. Compose Recomposition Issues**
```kotlin
// Bad: Creates new instance on every recomposition
val state = remember { mutableStateOf(0) }

// Good: Stable reference
val state by remember { mutableStateOf(0) }
```

**3. Flow Collection in Composable**
```kotlin
// Use collectAsState for compose
val uiState by viewModel.uiState.collectAsState()
```

### Debugging Tools

**Logcat:**
```bash
adb logcat -s BloodMoon
```

**Database Inspector:**
- Android Studio → View → Tool Windows → App Inspection
- (Note: SQLCipher databases won't show content)

**Layout Inspector:**
- Tools → Layout Inspector
- Inspect Compose UI hierarchy

---

## Building for Release

### 1. Generate Release APK

```bash
./gradlew assembleRelease
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

### 2. Sign APK (Production)

**Create keystore:**
```bash
keytool -genkey -v -keystore bloodmoon.keystore \
  -alias bloodmoon -keyalg RSA -keysize 2048 -validity 10000
```

**Configure in `app/build.gradle.kts`:**
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("bloodmoon.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "bloodmoon"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ...
    }
}
```

**Build:**
```bash
KEYSTORE_PASSWORD=xxx KEY_PASSWORD=xxx ./gradlew assembleRelease
```

### 3. ProGuard

**Enabled in release builds:**
- Obfuscates code
- Shrinks APK size
- Rules in `proguard-rules.pro`

**Keep rules:**
```proguard
-keep class com.bloodmoon.data.local.entities.** { *; }
-keep class * extends androidx.room.RoomDatabase
```

---

## Performance Optimization

### Database Queries

**Use Flow for Reactive Queries:**
```kotlin
// Automatically updates UI when data changes
dao.getAllLogs(): Flow<List<PeriodLog>>
```

**Index Frequently Queried Columns:**
```kotlin
@Entity(
    tableName = "period_logs",
    indices = [Index(value = ["date"])]
)
```

### Compose Performance

**Remember Expensive Calculations:**
```kotlin
val expensiveResult = remember(input) {
    calculateExpensiveValue(input)
}
```

**Use derivedStateOf:**
```kotlin
val filteredList by remember {
    derivedStateOf {
        list.filter { it.isActive }
    }
}
```

### Animation Performance

**Limit Concurrent Animations:**
- Max 2-3 simultaneous animations
- Pause when app backgrounded

---

## Git Workflow

### Branching Strategy

```
main         ← Production-ready code
  ├── develop   ← Development branch
  │   ├── feature/cycle-tracking
  │   ├── feature/calendar-ui
  │   └── bugfix/pin-validation
  └── release/v1.0.0
```

### Commit Messages

**Format:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructure
- `test`: Add tests
- `chore`: Maintenance

**Example:**
```
feat(predictions): Add irregular cycle smoothing

Implements weighted average for cycle length calculation
with higher weight on recent cycles.

Closes #42
```

### Code Review Checklist

- [ ] Code follows style guide
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No hardcoded values (use constants)
- [ ] No sensitive data logged
- [ ] Privacy-preserving (no network calls)

---

## Contributing

### Setup Development Environment

1. Fork repository
2. Clone your fork
3. Create feature branch
4. Make changes
5. Run tests
6. Submit pull request

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Instrumented tests added/updated
- [ ] Manual testing completed

## Screenshots (if UI changes)
[Add screenshots]

## Checklist
- [ ] Code follows project style
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests pass
```

---

## Troubleshooting

### Build Issues

**Gradle sync fails:**
```bash
./gradlew clean build --refresh-dependencies
```

**Compilation errors:**
1. Check Kotlin version (1.9.20)
2. Verify SDK versions (compileSdk = 34)
3. Invalidate caches: File → Invalidate Caches / Restart

### Runtime Issues

**App crashes on launch:**
1. Check Logcat for stack trace
2. Verify database migrations
3. Clear app data

**UI not updating:**
1. Check Flow collection
2. Verify StateFlow updates
3. Use `collectAsState()` in Compose

---

## Resources

### Documentation
- [Kotlin Docs](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [SQLCipher](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/)

### Community
- [r/androiddev](https://reddit.com/r/androiddev)
- [Kotlin Slack](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up)

---

## License

[To be determined - MIT recommended]

---

## Questions?

Open an issue or discussion on GitHub!

---

**Happy Coding! 🌙💻🤘**
