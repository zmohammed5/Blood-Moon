# 🔮 Cycle Prediction Algorithm

## Overview

Blood Moon uses a scientifically-informed algorithm to predict menstrual cycles, ovulation, and fertile windows. The approach balances biological accuracy with adaptability to individual cycle patterns.

---

## Core Algorithm

### 1. **Cycle Length Calculation**

The app uses a **weighted rolling average** to calculate cycle length:

```
Cycle Length = Days between consecutive period starts
```

**Weighting Formula:**
- More recent cycles receive higher weight
- Weight = 1.0 + (cycle_index / total_cycles)
- This ensures the prediction adapts to recent patterns

**Example:**
```
Cycles: [28, 30, 27, 29]
Weights: [1.25, 1.5, 1.75, 2.0]

Weighted Average = (28×1.25 + 30×1.5 + 27×1.75 + 29×2.0) / (1.25+1.5+1.75+2.0)
                 = 28.5 days (rounded to 29)
```

---

### 2. **Luteal Phase**

The **luteal phase** (post-ovulation to next period) is remarkably consistent across individuals:

- **Default**: 14 days
- **Range**: 12-16 days for most people
- **Consistency**: Luteal phase varies much less than follicular phase

**Why This Matters:**
Even with irregular cycles, the luteal phase stays relatively constant. This makes it the most reliable anchor point for prediction.

---

### 3. **Ovulation Prediction**

Ovulation is calculated by working **backward from the predicted period**:

```
Ovulation Date = Next Period Start - Luteal Phase Length
```

**Example:**
```
Next Period: January 28
Luteal Phase: 14 days
Ovulation: January 14
```

This is more accurate than counting forward from the last period, especially for irregular cycles.

---

### 4. **Fertile Window**

The fertile window accounts for:
- **Sperm survival**: Up to 5 days in optimal conditions
- **Egg lifespan**: 12-24 hours after ovulation

**Formula:**
```
Fertile Window Start = Ovulation - 5 days
Fertile Window End   = Ovulation + 1 day
```

**Example:**
```
Ovulation: January 14
Fertile Window: January 9 - January 15 (7 days total)
```

---

### 5. **Cycle Regularity Detection**

The app classifies cycles as regular or irregular based on **variance**:

```python
def is_irregular(cycle_lengths):
    average = mean(cycle_lengths)
    max_deviation = max(|length - average| for length in cycle_lengths)
    return max_deviation > 3 days
```

**Thresholds:**
- **Regular**: Variance ≤ 3 days
- **Irregular**: Variance > 3 days

---

### 6. **Confidence Levels**

Prediction confidence is based on data quality and regularity:

| Confidence | Criteria | Period Range | Ovulation Range |
|------------|----------|--------------|-----------------|
| **HIGH** | 3+ regular cycles | ±1 day | ±1 day |
| **MEDIUM** | 1-2 cycles, regular | ±2 days | ±2 days |
| **LOW** | Irregular or <1 cycle | ±4 days | ±3 days |

**Confidence Calculation:**
```kotlin
fun calculateConfidence(cycleCount: Int, isIrregular: Boolean): ConfidenceLevel {
    return when {
        cycleCount >= 3 && !isIrregular -> HIGH
        cycleCount >= 1 && !isIrregular -> MEDIUM
        else -> LOW
    }
}
```

---

## Test Cases

### Test Case 1: Regular 28-Day Cycle

**Input:**
- Last 3 periods: Day 0, Day 28, Day 56
- Today: Day 84

**Output:**
- Next Period: Day 112 (84 + 28)
- Ovulation: Day 98 (112 - 14)
- Fertile Window: Day 93 - Day 99
- Confidence: HIGH

---

### Test Case 2: Irregular Cycle

**Input:**
- Cycle lengths: [25, 31, 27] days

**Calculation:**
- Average: ~27.7 days
- Max deviation: 3.7 days (31 - 27.7)
- Classification: IRREGULAR

**Output:**
- Confidence: MEDIUM or LOW
- Wider prediction range: ±4 days

---

### Test Case 3: Short Cycle (21 Days)

**Input:**
- Last 3 periods: All 21 days apart

**Output:**
- Next Period: Day 21
- Ovulation: Day 7 (21 - 14)
- Fertile Window: Day 2 - Day 8
- Confidence: HIGH (if regular)

**Note**: Some individuals have naturally shorter luteal phases (~10-12 days). The app allows customization.

---

### Test Case 4: Long Cycle (35 Days)

**Input:**
- Last 3 periods: All 35 days apart

**Output:**
- Next Period: Day 35
- Ovulation: Day 21 (35 - 14)
- Fertile Window: Day 16 - Day 22
- Confidence: HIGH (if regular)

---

### Test Case 5: Insufficient Data

**Input:**
- No logged periods

**Output:**
- Uses default values (28-day cycle, 5-day period)
- Confidence: LOW
- Prediction range: ±4 days

---

## Edge Cases

### 1. **Single Period Logged**
- Cannot calculate cycle length
- Uses default (28 days)
- Confidence: LOW to MEDIUM

### 2. **Very Irregular Cycles**
- Example: [22, 35, 26, 31]
- Uses weighted average but with low confidence
- Provides wider prediction ranges

### 3. **Outlier Filtering**
- Cycle lengths <14 days or >45 days are filtered out
- Prevents data entry errors from skewing predictions

---

## Biological Accuracy

### Luteal Phase Consistency
**Research shows:**
- 85% of people have luteal phases of 12-16 days
- Average: 14 days across populations
- Varies <2 days for an individual across cycles

**Reference**:
- Creinin MD, Keverline S, Meyn LA. "How regular is regular?" (Contraception, 2004)

### Fertile Window
**Research shows:**
- Sperm can survive up to 5 days in fertile cervical mucus
- Most pregnancies occur from intercourse 1-2 days before ovulation
- Egg survives 12-24 hours

**Reference**:
- Wilcox AJ, Weinberg CR, Baird DD. "Timing of sexual intercourse in relation to ovulation." (NEJM, 1995)

---

## Limitations

1. **Not Contraception**: This app predicts cycles but is NOT reliable birth control
2. **PCOS/Irregular Cycles**: May require more data for accurate predictions
3. **Hormonal Birth Control**: Not designed for users on hormonal contraceptives
4. **Perimenopause**: May not handle highly irregular patterns well

---

## Future Enhancements

Potential algorithm improvements:
- Machine learning for pattern recognition
- Symptom-based ovulation detection
- Basal body temperature integration
- Cervical mucus tracking correlation

---

## Algorithm Validation

The algorithm has been tested with:
- 13 comprehensive unit tests
- Regular cycles (21-35 days)
- Irregular cycles (variance 3-10 days)
- Edge cases (no data, single cycle)

All tests pass with expected outputs within ±1 day of manual calculations.

---

**See**: `CyclePredictionEngine.kt` for implementation
**Tests**: `CyclePredictionEngineTest.kt` for validation
