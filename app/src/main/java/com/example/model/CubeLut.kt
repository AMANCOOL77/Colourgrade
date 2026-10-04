package com.example.model

/**
 * Represents a parsed 3D or 1D Cube Color Lookup Table.
 */
class CubeLut(
    val id: String,
    val name: String,
    val size: Int,
    val is3D: Boolean,
    val domainMin: FloatArray = floatArrayOf(0f, 0f, 0f),
    val domainMax: FloatArray = floatArrayOf(1f, 1f, 1f),
    val tableData: FloatArray
) {
    private val sizeSq = size * size
    private val sizeMinusOne = (size - 1).toFloat()
    private val maxCoord = size - 2

    /**
     * Fast trilinear sampling for 3D LUT, or 1D linear sampling.
     * Inputs: r, g, b in [0.0f, 1.0f].
     * Writes interpolated RGB to outRgb array.
     */
    fun sample(r: Float, g: Float, b: Float, outRgb: FloatArray) {
        if (!is3D) {
            sample1D(r, g, b, outRgb)
            return
        }

        val rangeR = domainMax[0] - domainMin[0]
        val rangeG = domainMax[1] - domainMin[1]
        val rangeB = domainMax[2] - domainMin[2]

        val normR = if (rangeR > 0.0001f) ((r - domainMin[0]) / rangeR).coerceIn(0f, 1f) else r.coerceIn(0f, 1f)
        val normG = if (rangeG > 0.0001f) ((g - domainMin[1]) / rangeG).coerceIn(0f, 1f) else g.coerceIn(0f, 1f)
        val normB = if (rangeB > 0.0001f) ((b - domainMin[2]) / rangeB).coerceIn(0f, 1f) else b.coerceIn(0f, 1f)

        val fx = normR * sizeMinusOne
        val fy = normG * sizeMinusOne
        val fz = normB * sizeMinusOne

        val x0 = fx.toInt().coerceIn(0, maxCoord)
        val y0 = fy.toInt().coerceIn(0, maxCoord)
        val z0 = fz.toInt().coerceIn(0, maxCoord)

        val x1 = x0 + 1
        val y1 = y0 + 1
        val z1 = z0 + 1

        val dx = fx - x0
        val dy = fy - y0
        val dz = fz - z0

        val invDx = 1f - dx
        val invDy = 1f - dy
        val invDz = 1f - dz

        val row0 = z0 * sizeSq
        val row1 = z1 * sizeSq

        val i000 = (row0 + y0 * size + x0) * 3
        val i100 = (row0 + y0 * size + x1) * 3
        val i010 = (row0 + y1 * size + x0) * 3
        val i110 = (row0 + y1 * size + x1) * 3

        val i001 = (row1 + y0 * size + x0) * 3
        val i101 = (row1 + y0 * size + x1) * 3
        val i011 = (row1 + y1 * size + x0) * 3
        val i111 = (row1 + y1 * size + x1) * 3

        val data = tableData

        // Red
        val r00 = data[i000] * invDx + data[i100] * dx
        val r01 = data[i001] * invDx + data[i101] * dx
        val r10 = data[i010] * invDx + data[i110] * dx
        val r11 = data[i011] * invDx + data[i111] * dx
        val r0 = r00 * invDy + r10 * dy
        val r1 = r01 * invDy + r11 * dy
        outRgb[0] = (r0 * invDz + r1 * dz).coerceIn(0f, 1f)

        // Green
        val g00 = data[i000 + 1] * invDx + data[i100 + 1] * dx
        val g01 = data[i001 + 1] * invDx + data[i101 + 1] * dx
        val g10 = data[i010 + 1] * invDx + data[i110 + 1] * dx
        val g11 = data[i011 + 1] * invDx + data[i111 + 1] * dx
        val g0 = g00 * invDy + g10 * dy
        val g1 = g01 * invDy + g11 * dy
        outRgb[1] = (g0 * invDz + g1 * dz).coerceIn(0f, 1f)

        // Blue
        val b00 = data[i000 + 2] * invDx + data[i100 + 2] * dx
        val b01 = data[i001 + 2] * invDx + data[i101 + 2] * dx
        val b10 = data[i010 + 2] * invDx + data[i110 + 2] * dx
        val b11 = data[i011 + 2] * invDx + data[i111 + 2] * dx
        val b0 = b00 * invDy + b10 * dy
        val b1 = b01 * invDy + b11 * dy
        outRgb[2] = (b0 * invDz + b1 * dz).coerceIn(0f, 1f)
    }

    private fun sample1D(r: Float, g: Float, b: Float, outRgb: FloatArray) {
        val fr = r.coerceIn(0f, 1f) * sizeMinusOne
        val fg = g.coerceIn(0f, 1f) * sizeMinusOne
        val fb = b.coerceIn(0f, 1f) * sizeMinusOne

        val r0 = fr.toInt().coerceIn(0, maxCoord)
        val g0 = fg.toInt().coerceIn(0, maxCoord)
        val b0 = fb.toInt().coerceIn(0, maxCoord)

        val dr = fr - r0
        val dg = fg - g0
        val db = fb - b0

        outRgb[0] = (tableData[r0 * 3] * (1f - dr) + tableData[(r0 + 1) * 3] * dr).coerceIn(0f, 1f)
        outRgb[1] = (tableData[g0 * 3 + 1] * (1f - dg) + tableData[(g0 + 1) * 3 + 1] * dg).coerceIn(0f, 1f)
        outRgb[2] = (tableData[b0 * 3 + 2] * (1f - db) + tableData[(b0 + 1) * 3 + 2] * db).coerceIn(0f, 1f)
    }
}
