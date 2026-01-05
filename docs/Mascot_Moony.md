# 👻 Moony - The Blood Moon Mascot

## Character Overview

**Moony** is a small pixel-art ghost/moon hybrid creature that serves as the Blood Moon mascot and companion.

**Personality:**
- Cute but slightly edgy
- Gothic aesthetic
- Playful but mysterious
- Supportive without being saccharine

---

## Visual Design

### Base Form (Default Variant)

**Shape:**
- Circular body (moon-like)
- Diameter: 64dp (default), scales based on context
- Pixel-art style with clean, sharp edges

**Colors:**
- Body: `Pale Lilac` (#FFD3FF)
- Outline: `Electric Pink` (#FF5CA8), 2px stroke
- Eyes: `Deep Black` (#0A0A0F), 3px radius each
- Smile: `Deep Black`, curved line

**Features:**
- Two small circular eyes
- Simple curved smile
- Optional glowing outline for special states

**File:** `assets/moony_pixel_default.svg`

---

## Variants

### 1. Default Moony

**When to Use:**
- Normal app state
- No special conditions active
- Daytime or non-full moon

**Visual Details:**
- Round, friendly face
- Gentle smile
- Slight glow effect (opacity 30%)

**Emotional Tone:** Calm, supportive, cozy

---

### 2. Horns Variant (Metal Mode)

**When to Use:**
- Metal Mode is enabled
- User has unlocked Metal Mode (6 taps)

**Visual Changes:**
- **Devil horns** added to top of head
  - Small triangular horns
  - Color: `Metal Crimson` (#8B0000)
  - Outline: `Metal Hell Pink` (#FF1493)
- Eyes slightly larger and more intense
- Outline glow increased (opacity 50%)
- Smile more mischievous (slightly wider curve)

**Emotional Tone:** Playful rebellion, metal energy, confident

**File:** `assets/moony_pixel_horns.svg`

---

### 3. Corpse Paint Variant (Black Metal)

**When to Use:**
- Full moon detected
- Special occasions (rare)
- Can appear randomly at low frequency

**Visual Changes:**
- **Black eye paint** streaking down
  - Large black circles around eyes (8px radius)
  - White dot pupils (3px radius)
  - Vertical black lines from eyes downward
- **Black lipstick** style smile (thicker line, 4px)
- Otherwise same pale moon body

**Emotional Tone:** Mysterious, theatrical, black metal aesthetic

**File:** `assets/moony_pixel_corpsepaint.svg`

---

## Animations

### 1. Float Animation

**Type:** Continuous idle animation

**Motion:**
- Vertical oscillation
- Range: 0dp to 20dp
- Duration: 2000ms per cycle
- Easing: Linear
- Repeat: Infinite, reverse mode

**Code:**
```kotlin
val floatOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 20f,
    animationSpec = infiniteRepeatable(
        animation = tween(2000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )
)
```

**When to Use:** Default state, always active

---

### 2. Head Bob Animation

**Type:** Music playback indicator

**Motion:**
- Quick vertical bounce
- Range: 0dp to 10dp
- Duration: 300ms per cycle
- Easing: Linear
- Repeat: Infinite while music plays, reverse mode

**Trigger:** Active only when local music is playing

**Visual Effect:**
- Syncs with music tempo (if possible)
- Gives impression Moony is "vibing"

**Code:**
```kotlin
val bobOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = if (isPlaying) 10f else 0f,
    animationSpec = infiniteRepeatable(
        animation = tween(300, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    )
)
```

---

### 3. Horns Up Gesture 🤘

**Type:** Celebratory animation

**Motion:**
- Not Moony itself, but a separate gesture icon
- Shows rock/metal "horns up" hand sign
- Scale pulse: 0.8 to 1.2
- Duration: 500ms per cycle
- Easing: FastOutSlowInEasing

**Trigger:**
- Metal Mode unlock moment
- Randomly during Metal Mode (5% chance per screen load)
- User taps Moony 3 times quickly

**Display:**
- Small icon (32dp) near Moony
- `Metal Hell Pink` color
- Fades in, pulses, fades out after 2 seconds

---

### 4. Angry Shake

**Type:** Feedback animation

**Motion:**
- Rapid horizontal shake
- Range: ±10dp
- Duration: 50ms per shake
- Repeat: 3 times
- Easing: Snap

**Trigger:**
- Wrong PIN entered
- Error state
- Moony "rejects" user action

**Visual Effect:**
- Moony looks "upset" but in a cute way
- Quick and attention-grabbing

**Code:**
```kotlin
val shakeOffset by animateFloatAsState(
    targetValue = if (isShaking) 10f else 0f,
    animationSpec = repeatable(
        iterations = 3,
        animation = tween(50),
        repeatMode = RepeatMode.Reverse
    ),
    finishedListener = { isShaking = false }
)
```

---

## Interactive Behaviors

### Tap Interactions

**Single Tap:**
- Moony "bounces" slightly (scale 0.9 → 1.0, 100ms)
- No other effect

**6 Rapid Taps (within 3 seconds):**
- **Unlocks Metal Mode**
- Sequence:
  1. Count tap increments
  2. At tap #6, trigger unlock
  3. Moony transforms to horns variant (300ms fade)
  4. Show Metal Mode unlock dialog
  5. "Horns Up" gesture appears

**Hold for 3 Seconds:**
- Alternative Metal Mode unlock
- Visual feedback: Glow intensifies during hold
- At 3 seconds: Trigger unlock

---

## Context-Aware Display

### Dashboard
- **Size:** 80dp
- **Position:** Center of screen, between cards
- **Animation:** Float (always)
- **Variant:** Based on mode/moon phase

### Lock Screen
- **Size:** 100dp
- **Position:** Top center, above PIN display
- **Animation:** Float OR Angry Shake (on wrong PIN)
- **Variant:** Default (no special variants on lock screen)

### Settings
- **Size:** 64dp
- **Position:** Next to Metal Mode toggle
- **Animation:** Static or subtle float
- **Variant:** Horns if Metal Mode enabled

---

## Full Moon Behavior

**Detection:**
```kotlin
val isFullMoon = MoonPhaseCalculator.isFullMoon()
```

**Behavior When True:**
1. Moony switches to **Corpse Paint variant**
2. Outline glow pulses red (not pink)
3. Soft gong sound plays ONCE (first time full moon is detected that day)
4. Special "Full Moon" indicator appears near Moony

**Duration:**
- Active for the entire day of the full moon
- Reverts to normal at next moon phase

---

## Sound Effects

### Gong Sound (Full Moon)

**Properties:**
- Format: OGG or MP3
- Duration: ~2 seconds
- Volume: 30% max
- Frequency: Once per full moon day
- Trigger: First app open on full moon day

**File Location:** `app/src/main/res/raw/gong.ogg`

**Implementation:**
```kotlin
if (isFullMoon && !hasPlayedTodayGong) {
    mediaPlayer.play("gong.ogg")
    saveFullMoonGongPlayed(LocalDate.now())
}
```

---

## Pixel Art Specifications

### Grid System
- Base size: 64×64 pixel canvas
- Character occupies ~40×40 center area
- 12px margin on all sides for glow effects

### Color Limitations
- Max 6 colors per variant (pixel art style)
- No gradients (except for glow effects)
- Sharp edges, no anti-aliasing on shapes

### SVG Export Settings
- ViewBox: "0 0 64 64"
- Preserve pixel alignment
- Export at 1x, 2x, 3x for different densities

---

## Animation Performance

### Optimization
- Use Compose `remember` for animation states
- Only animate when visible on screen
- Pause animations when app is backgrounded
- Use `infiniteTransition` for continuous animations

### Target FPS
- Smooth animations: 60 FPS
- Fallback for low-end devices: 30 FPS

### Battery Impact
- Float animation: Negligible (<0.1% per hour)
- Head bob (music mode): ~0.2% per hour
- All animations combined: <0.5% battery per hour

---

## Accessibility Considerations

### Screen Readers
- Content description: "Moony, the Blood Moon mascot"
- Variant descriptions:
  - Default: "Moony in default form"
  - Horns: "Moony with devil horns"
  - Corpse Paint: "Moony with black metal face paint"

### Reduced Motion
- Respect system reduced motion settings
- Disable float/bob animations
- Keep Moony static but still display variants

### High Contrast
- Moony outline increases to 3px
- Glow effects removed for clarity

---

## Easter Egg: Rare Variants

**Future Possibilities:**
- **Tiny Witch Hat** (October only)
- **Scythe Accessory** (random 1% chance)
- **Glowing Red Eyes** (during period logging)
- **Sleeping Zzz** (after 10pm)

**Implementation:** Can be added in future updates

---

## Moony's "Voice"

While Moony doesn't speak, the affirmations and partner notes represent Moony's supportive presence.

**Tone:**
- Grounded, not preachy
- Supportive, not coddling
- Slightly dark humor
- Metal/goth-friendly

**Examples:**
- "The world's a pit. Good thing you mosh through it."
- "You've survived every day so far. That's metal."

See [README.md](../README.md#-affirmations-style) for more examples.

---

## Technical Implementation

### Compose Component

**File:** `app/src/main/java/com/bloodmoon/ui/components/Moony.kt`

**Parameters:**
```kotlin
@Composable
fun Moony(
    variant: MoonyVariant = MoonyVariant.DEFAULT,
    animation: MoonyAnimation = MoonyAnimation.FLOAT,
    isPlaying: Boolean = false,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
)
```

**Variants Enum:**
```kotlin
enum class MoonyVariant {
    DEFAULT,
    HORNS,
    CORPSE_PAINT
}
```

**Animations Enum:**
```kotlin
enum class MoonyAnimation {
    FLOAT,
    HEAD_BOB,
    HORNS_UP,
    ANGRY_SHAKE
}
```

---

## Design Credits

Moony embodies the spirit of Blood Moon:
- Private and personal (offline, no tracking)
- Dark but approachable (gothic aesthetic)
- Supportive but real (grounded affirmations)
- Playful but mysterious (easter eggs, variants)

**Made for a very special person.** 🌙👻🤘
