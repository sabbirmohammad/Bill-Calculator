package com.powerunit.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.powerunit.core.common.DateUtils
import com.powerunit.core.common.Formatters
import com.powerunit.domain.model.BillBreakdown
import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.Reading
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportReadingsToCsv(
        readings: List<Reading>,
        meterName: String
    ): File = withContext(Dispatchers.IO) {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = DateUtils.formatDate(DateUtils.currentInstant(), "yyyyMMdd_HHmmss")
        val csvFile = File(exportsDir, "wattwise_readings_$timestamp.csv")

        FileWriter(csvFile).use { writer ->
            // Header
            writer.append("ID,Date,Time,Meter,ReadingValue,Estimated,MeterReset,Note\n")

            // Rows
            readings.forEach { r ->
                val dateStr = DateUtils.formatDate(r.recordedAt, "yyyy-MM-dd")
                val timeStr = DateUtils.formatDate(r.recordedAt, "HH:mm")
                val noteClean = (r.note ?: "").replace("\"", "\"\"")
                writer.append("${r.id},\"$dateStr\",\"$timeStr\",\"$meterName\",${r.readingValue},${r.isEstimated},${r.isMeterReset},\"$noteClean\"\n")
            }
        }

        csvFile
    }

    suspend fun exportBillToPdf(
        bill: BillBreakdown,
        meter: Meter?,
        cycleUsage: CycleUsage?
    ): File = withContext(Dispatchers.IO) {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = DateUtils.formatDate(DateUtils.currentInstant(), "yyyyMMdd_HHmmss")
        val pdfFile = File(exportsDir, "wattwise_bill_$timestamp.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72 dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = drawPdfHeader(canvas, meter, cycleUsage)
        y = drawPdfSlabsTable(canvas, bill, y)
        drawPdfSummaryAndFooter(canvas, bill, y)

        pdfDocument.finishPage(page)

        FileOutputStream(pdfFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        pdfFile
    }

    private fun drawPdfHeader(canvas: android.graphics.Canvas, meter: Meter?, cycleUsage: CycleUsage?): Float {
        val titlePaint = Paint().apply {
            color = Color.rgb(0, 105, 92) // Deep Teal
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headingPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 60f
        canvas.drawText("WattWise - Electricity Bill Statement", 50f, y, titlePaint)
        y += 24f

        val todayStr = DateUtils.formatDate(DateUtils.currentDhakaDate(), "dd MMMM yyyy")
        canvas.drawText("Generated on: $todayStr (Asia/Dhaka)", 50f, y, textPaint)
        y += 20f

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 30f

        val meterName = meter?.name ?: "Main Meter"
        val meterType = meter?.type?.name ?: "POSTPAID"
        canvas.drawText("Meter: $meterName ($meterType)", 50f, y, headingPaint)
        y += 20f

        val unitsText = cycleUsage?.let { "${Formatters.formatUnits(it.totalUnitsSoFar)} kWh" } ?: "0 kWh"
        canvas.drawText("Total Cycle Consumption: $unitsText", 50f, y, textPaint)
        y += 30f

        return y
    }

    private fun drawPdfSlabsTable(canvas: android.graphics.Canvas, bill: BillBreakdown, startY: Float): Float {
        val headingPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val rightTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = startY
        canvas.drawText("Tariff Slab Breakdown", 50f, y, headingPaint)
        y += 16f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 18f

        canvas.drawText("Slab", 50f, y, headingPaint)
        canvas.drawText("Units Billed", 180f, y, headingPaint)
        canvas.drawText("Rate (৳)", 320f, y, headingPaint)
        canvas.drawText("Amount (৳)", 545f, y, Paint(headingPaint).apply { textAlign = Paint.Align.RIGHT })
        y += 10f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        bill.slabCharges.forEach { slab ->
            val toStr = slab.toUnit?.let { "${it.toInt()}" } ?: "+"
            val slabName = "Slab ${slab.slabIndex} (${slab.fromUnit.toInt()}–$toStr)"
            canvas.drawText(slabName, 50f, y, textPaint)
            canvas.drawText("${Formatters.formatUnits(slab.unitsBilled)} kWh", 180f, y, textPaint)
            canvas.drawText(slab.ratePerUnit.toPlainString(), 320f, y, textPaint)
            canvas.drawText(Formatters.formatBDT(slab.amount), 545f, y, rightTextPaint)
            y += 20f
        }

        y += 10f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 25f

        return y
    }

    private fun drawPdfSummaryAndFooter(canvas: android.graphics.Canvas, bill: BillBreakdown, startY: Float) {
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val rightTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = startY
        canvas.drawText("Energy Charges Subtotal:", 50f, y, textPaint)
        canvas.drawText(Formatters.formatBDT(bill.energyCharge), 545f, y, rightTextPaint)
        y += 20f

        canvas.drawText("Demand Charge:", 50f, y, textPaint)
        canvas.drawText(Formatters.formatBDT(bill.demandCharge), 545f, y, rightTextPaint)
        y += 20f

        canvas.drawText("Service Charge:", 50f, y, textPaint)
        canvas.drawText(Formatters.formatBDT(bill.serviceCharge), 545f, y, rightTextPaint)
        y += 20f

        canvas.drawText("VAT (${bill.vatPercent}%):", 50f, y, textPaint)
        canvas.drawText(Formatters.formatBDT(bill.vatAmount), 545f, y, rightTextPaint)
        y += 20f

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 25f

        val totalPaint = Paint().apply {
            color = Color.rgb(0, 105, 92)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val totalRightPaint = Paint(totalPaint).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawText("Total Payable Amount:", 50f, y, totalPaint)
        canvas.drawText(Formatters.formatBDT(bill.totalBill), 545f, y, totalRightPaint)
        y += 50f

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }
        canvas.drawText("Generated 100% offline on device • WattWise Privacy Promise", 50f, y, footerPaint)
    }

    fun getShareIntent(file: File, mimeType: String): Intent {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
