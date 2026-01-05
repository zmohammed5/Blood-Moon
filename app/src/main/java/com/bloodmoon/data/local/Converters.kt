package com.bloodmoon.data.local

import androidx.room.TypeConverter
import com.bloodmoon.data.local.entities.FlowIntensity
import com.bloodmoon.data.local.entities.MedicationFrequency
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else value.split(",").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return if (map.isEmpty()) {
            ""
        } else {
            map.entries.joinToString(";") { "${it.key}:${it.value}" }
        }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return if (value.isBlank()) {
            emptyMap()
        } else {
            value.split(";")
                .filter { it.contains(":") }
                .associate {
                    val (key, v) = it.split(":", limit = 2)
                    key to v
                }
        }
    }

    @TypeConverter
    fun fromFlowIntensity(intensity: FlowIntensity?): String? {
        return intensity?.name
    }

    @TypeConverter
    fun toFlowIntensity(value: String?): FlowIntensity? {
        return value?.let { FlowIntensity.valueOf(it) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromLocalTimeList(list: List<LocalTime>): String {
        return list.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toLocalTimeList(value: String): List<LocalTime> {
        return if (value.isBlank()) emptyList()
        else value.split(",").filter { it.isNotBlank() }.map { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromMedicationFrequency(frequency: MedicationFrequency?): String? {
        return frequency?.name
    }

    @TypeConverter
    fun toMedicationFrequency(value: String?): MedicationFrequency? {
        return value?.let { MedicationFrequency.valueOf(it) }
    }
}
