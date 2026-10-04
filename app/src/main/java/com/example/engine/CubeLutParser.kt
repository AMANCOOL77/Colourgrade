package com.example.engine

import com.example.model.CubeLut
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

object CubeLutParser {

    /**
     * Parses standard .cube LUT files (both 3D and 1D).
     * Returns Result.success(CubeLut) or Result.failure with descriptive exception.
     */
    fun parse(inputStream: InputStream, defaultName: String = "Custom LUT"): Result<CubeLut> {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream), 32 * 1024)
            var title: String? = null
            var lut3DSize: Int? = null
            var lut1DSize: Int? = null
            var domainMin = floatArrayOf(0f, 0f, 0f)
            var domainMax = floatArrayOf(1f, 1f, 1f)

            var is3D = true
            var size = 0
            var tableData: FloatArray? = null
            var dataIndex = 0
            var totalExpectedFloats = 0

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val rawLine = line!!.trim()
                if (rawLine.isEmpty() || rawLine.startsWith("#")) {
                    continue
                }

                // Check headers if table not allocated yet
                if (tableData == null) {
                    val upper = rawLine.uppercase()
                    when {
                        upper.startsWith("TITLE") -> {
                            val parts = rawLine.split(Regex("\\s+"), 2)
                            if (parts.size > 1) {
                                title = parts[1].replace("\"", "").trim()
                            }
                            continue
                        }
                        upper.startsWith("LUT_3D_SIZE") -> {
                            val parts = rawLine.split(Regex("\\s+"))
                            if (parts.size > 1) {
                                val parsedSize = parts[1].toIntOrNull()
                                if (parsedSize != null && parsedSize in 2..128) {
                                    lut3DSize = parsedSize
                                    is3D = true
                                    size = parsedSize
                                    totalExpectedFloats = size * size * size * 3
                                    tableData = FloatArray(totalExpectedFloats)
                                }
                            }
                            continue
                        }
                        upper.startsWith("LUT_1D_SIZE") -> {
                            val parts = rawLine.split(Regex("\\s+"))
                            if (parts.size > 1) {
                                val parsedSize = parts[1].toIntOrNull()
                                if (parsedSize != null && parsedSize in 2..4096) {
                                    lut1DSize = parsedSize
                                    is3D = false
                                    size = parsedSize
                                    totalExpectedFloats = size * 3
                                    tableData = FloatArray(totalExpectedFloats)
                                }
                            }
                            continue
                        }
                        upper.startsWith("DOMAIN_MIN") -> {
                            val parts = rawLine.split(Regex("\\s+"))
                            if (parts.size >= 4) {
                                domainMin = floatArrayOf(
                                    parts[1].toFloatOrNull() ?: 0f,
                                    parts[2].toFloatOrNull() ?: 0f,
                                    parts[3].toFloatOrNull() ?: 0f
                                )
                            }
                            continue
                        }
                        upper.startsWith("DOMAIN_MAX") -> {
                            val parts = rawLine.split(Regex("\\s+"))
                            if (parts.size >= 4) {
                                domainMax = floatArrayOf(
                                    parts[1].toFloatOrNull() ?: 1f,
                                    parts[2].toFloatOrNull() ?: 1f,
                                    parts[3].toFloatOrNull() ?: 1f
                                )
                            }
                            continue
                        }
                    }
                }

                // If table is allocated, parse data rows
                if (tableData != null && dataIndex < totalExpectedFloats) {
                    val tokens = rawLine.split(Regex("\\s+"))
                    if (tokens.size >= 3) {
                        val r = tokens[0].toFloatOrNull()
                        val g = tokens[1].toFloatOrNull()
                        val b = tokens[2].toFloatOrNull()
                        if (r != null && g != null && b != null) {
                            tableData[dataIndex++] = r
                            tableData[dataIndex++] = g
                            tableData[dataIndex++] = b
                        }
                    }
                }
            }

            if (tableData == null || size <= 1) {
                return Result.failure(IllegalArgumentException("Invalid or unsupported LUT file."))
            }

            if (dataIndex < totalExpectedFloats) {
                // If slightly less due to formatting, fill remaining or check if substantial
                if (dataIndex < (totalExpectedFloats * 0.9f)) {
                    return Result.failure(IllegalArgumentException("Invalid or unsupported LUT file: incomplete data."))
                }
            }

            val finalTitle = if (!title.isNullOrBlank()) title else defaultName
            val lut = CubeLut(
                id = UUID.randomUUID().toString(),
                name = finalTitle,
                size = size,
                is3D = is3D,
                domainMin = domainMin,
                domainMax = domainMax,
                tableData = tableData
            )
            Result.success(lut)
        } catch (e: OutOfMemoryError) {
            Result.failure(IllegalArgumentException("LUT size is too large for device memory."))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid or unsupported LUT file."))
        }
    }
}
