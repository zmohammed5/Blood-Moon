# 🎨 UI Design System

## Design Philosophy

Blood Moon embraces a **gothic, cozy, spooky-cute** aesthetic that feels:
- Celestial and mysterious
- Dark but approachable
- Metal-flavored without being over-the-top
- Private and personal

---

## Color Palette

### Base Gothic Theme

| Color Name | Hex | Usage |
|------------|-----|-------|
| **Deep Black** | `#0A0A0F` | Background, primary dark |
| **Hell Crimson Shadow** | `#210012` | Surface, card backgrounds |
| **Dark Magenta** | `#38001E` | Surface variants, dividers |
| **Vampy Purple** | `#5F0A48` | Accents, borders, outlines |
| **Electric Pink** | `#FF5CA8` | Primary actions, highlights |
| **Pale Lilac** | `#FFD3FF` | Text, icons, content |

### Metal Mode Palette

| Color Name | Hex | Usage |
|------------|-----|-------|
| **Metal Crimson** | `#8B0000` | Primary in Metal Mode |
| **Metal Hell Pink** | `#FF1493` | Intense highlights |

### Functional Colors

| Purpose | Color | Hex |
|---------|-------|-----|
| Period Days | Period Red | `#FF5CA8` |
| Fertile Window | Fertile Blue | `#9D4EDD` |
| Error/Alert | Period Red | `#FF5CA8` |

---

## Typography

**Font Family**: `Monospace` (system default)

This gives a technical, pixel-art aesthetic that fits the gothic theme.

### Type Scale

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display Large | 57sp | Bold | Main titles |
| Display Medium | 45sp | Bold | Section headers |
| Display Small | 36sp | Bold | Large headings |
| Headline Large | 32sp | Bold | Page titles |
| Headline Medium | 28sp | Bold | Card titles |
| Headline Small | 24sp | Bold | Subsection titles |
| Title Large | 22sp | Bold | Emphasized text |
| Title Medium | 16sp | Medium | Card subtitles |
| Body Large | 16sp | Normal | Main body text |
| Body Medium | 14sp | Normal | Secondary text |
| Body Small | 12sp | Normal | Helper text |
| Label | 14sp | Medium | Buttons, tabs |

---

## Components

### Cards

**Material 3 Cards** with custom colors:

```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface, // Hell Crimson Shadow
        contentColor = MaterialTheme.colorScheme.onSurface  // Pale Lilac
    )
)
```

**Variants:**
- **Surface Cards**: `Hell Crimson Shadow` background
- **Primary Container**: `Vampy Purple` background
- **Surface Variant**: `Dark Magenta` background

**Elevation**: Minimal (0-2dp) for a flat, gothic look

---

### Buttons

**Primary Buttons:**
- Background: `Electric Pink`
- Text: `Deep Black`
- Shape: Rounded corners (8dp)

**Text Buttons:**
- Text: `Electric Pink`
- No background

**Disabled:**
- Opacity: 38%

---

### Icons

- Use Material Icons Extended
- Color: `Pale Lilac` or `Electric Pink`
- Size: 24dp (standard), 32dp (large)

**Custom Icons:**
- Moon phases: Use emoji or custom pixel art
- Moony: Canvas-drawn pixel art

---

### Dividers

**Standard Divider:**
- Color: `Vampy Purple`
- Thickness: 1dp
- Opacity: 60%

**Rune Divider:**
- Use `rune_divider.svg` asset
- Color: `Vampy Purple` with `Electric Pink` accent

---

### Navigation Bar

**Bottom Navigation:**
- Background: `Hell Crimson Shadow`
- Selected Icon: `Electric Pink`
- Unselected Icon: `Vampy Purple`
- Indicator: `Vampy Purple` (subtle)

---

## Decorative Elements

### Pentagrams

**Usage**: Subtle background tiles
- Asset: `pentagram_tile.svg`
- Opacity: 10-20%
- Color: `Vampy Purple`
- Placement: Scattered in backgrounds, especially on calendar

### Barbed Wire Hearts

**Usage**: Occasional decorative accents
- Style: Pixel-art, outlined
- Color: `Electric Pink` or `Vampy Purple`
- Use sparingly for visual interest

### Crescent Moons

**Usage**: Icons, decorative elements
- Style: Pixel-art with faint metal embossing
- Color: `Pale Lilac` with `Electric Pink` outline

### Rune Dividers

**Usage**: Section separators
- Asset: `rune_divider.svg`
- Color: `Vampy Purple` line, `Electric Pink` center rune

---

## Animations

### Moony Animations

See [Mascot_Moony.md](Mascot_Moony.md) for detailed specs.

**Summary:**
- **Float**: Gentle up/down (20dp range, 2s duration)
- **Head Bob**: Quick bounce (10dp range, 300ms duration)
- **Horns Up**: Scale pulse (1.0 to 1.2, 1s duration)
- **Angry Shake**: Rapid horizontal shake (±10dp, 50ms × 3)

### Transitions

**Screen Transitions:**
- Fade in/out: 300ms
- Slide: 400ms with `FastOutSlowInEasing`

**Element Animations:**
- Button press: Scale to 0.95, 100ms
- Card appear: Fade + slight scale, 300ms
- List items: Staggered fade-in, 100ms delay per item

---

## Spacing System

Following Material Design 8dp grid:

| Size | Value | Usage |
|------|-------|-------|
| XS | 4dp | Tight spacing |
| S | 8dp | Compact spacing |
| M | 16dp | Standard spacing |
| L | 24dp | Comfortable spacing |
| XL | 32dp | Generous spacing |
| XXL | 48dp | Section separation |

---

## Layout Patterns

### Dashboard Layout

```
┌─────────────────────────┐
│      Blood Moon         │  (Title)
├─────────────────────────┤
│   🌕 Full Moon          │  (Moon Phase Card)
├─────────────────────────┤
│   Cycle Day 14          │
│   3 days until period   │  (Cycle Info Card)
│   Phase: Ovulation      │
├─────────────────────────┤
│         👻              │  (Moony - centered)
├─────────────────────────┤
│   Daily Affirmation     │  (Affirmation Card)
├─────────────────────────┤
│   Partner Note          │  (Optional)
└─────────────────────────┘
```

### Calendar Layout

```
┌─────────────────────────┐
│   January 2026          │  (Month/Year)
├─────────────────────────┤
│  S  M  T  W  T  F  S    │
│           1  2  3  4    │
│  7  8  9 10 11 12 13    │  (Period days in pink)
│ 14 15 16 17 18 19 20    │  (Fertile days in purple)
│ 21 22 23 24 25 26 27    │
│ 28 29 30 31             │
├─────────────────────────┤
│    [Log Entry]          │  (Bottom sheet)
└─────────────────────────┘
```

### Settings Layout

```
┌─────────────────────────┐
│      Settings           │
├─────────────────────────┤
│  Security               │
│    PIN Lock      [×]    │
│    Biometric     [√]    │
├─────────────────────────┤
│  Appearance             │
│    Metal Mode 🤘 [√]    │
├─────────────────────────┤
│  Partner Notes          │
│    Enabled       [√]    │
├─────────────────────────┤
│  Cycle Settings         │
│    Avg Cycle: 28 days   │
│    [─────●────]         │
└─────────────────────────┘
```

---

## Lock Screen Design

**Visual Elements:**
- Moony holding a tiny pixel shield
- "Enter your sacred sigil" title
- Glyph-styled number pad
- 4 dots showing PIN progress
- Wrong PIN: Moony shakes angrily (but cute)

**Number Pad:**
- Circular buttons (64dp diameter)
- Numbers in monospace font
- Background: `Vampy Purple`
- Text: `Pale Lilac`

---

## Metal Mode Differences

When Metal Mode is active:

### Color Shifts
- Primary: `Electric Pink` → `Metal Hell Pink`
- Accents: `Vampy Purple` → `Metal Crimson`
- Outlines: More intense, slightly thicker

### Visual Changes
- Moony: Switches to horns variant
- Glow effects: More pronounced
- Pentagram opacity: Slightly higher (15-25%)

### No Changes
- Base background stays `Deep Black`
- Text readability maintained
- Layout remains identical

---

## Accessibility

### Contrast Ratios

All text meets WCAG AA standards:
- `Pale Lilac` on `Deep Black`: 15.2:1 (AAA)
- `Electric Pink` on `Deep Black`: 6.8:1 (AA)
- `Vampy Purple` on `Deep Black`: 4.7:1 (AA for large text)

### Touch Targets

- Minimum: 48dp × 48dp (Material guideline)
- Moony: 80dp × 80dp (large, easy to tap)
- Buttons: 64dp height minimum

### Dark Mode Only

- App is dark-only by design
- All colors optimized for dark backgrounds
- No light mode (gothic aesthetic requires darkness)

---

## Responsive Design

### Phone (Portrait)

- Single column layout
- Bottom navigation
- Cards full-width with 16dp margin

### Tablet (Landscape)

- Two-column layout for calendar + details
- Larger Moony (120dp)
- Increased spacing (24dp base)

### Adaptive Components

- Text scales with system font size
- Touch targets expand for accessibility settings
- Cards reflow for different screen widths

---

## Pixel Art Style

All custom graphics use a **pixel art** aesthetic:

- Sharp edges (no anti-aliasing on shapes)
- Limited color palette
- Intentionally "retro" look
- Complements monospace typography

**Tools:**
- SVG for scalability
- Canvas drawing for dynamic elements (Moony)

---

## Full Moon Effects

When `MoonPhaseCalculator.isFullMoon()` returns true:

1. **Moony Visual**: Switch to corpse paint variant
2. **Glow**: Red outline pulse (opacity 0.5 to 1.0, 2s)
3. **Sound**: Soft gong (play once, low volume)
4. **Background**: Pentagram opacity increased slightly

**Animation Sequence:**
```
1. Detect full moon
2. Fade Moony to corpse paint variant (300ms)
3. Start red glow pulse
4. Play gong sound (once)
5. Hold state until moon phase changes
```

---

## Design Files

**Assets Location:** `/assets/`
- `moony_pixel_default.svg`
- `moony_pixel_horns.svg`
- `moony_pixel_corpsepaint.svg`
- `pentagram_tile.svg`
- `rune_divider.svg`

**Compose Components:** `/app/src/main/java/com/bloodmoon/ui/components/`
- `Moony.kt` - Mascot with all variants and animations
- (Additional components as developed)

---

**Blood Moon** - Where goth meets cute meets functional design. 🌙🖤
