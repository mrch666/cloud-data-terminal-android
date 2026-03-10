package com.cloudterminal.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Размер области сканирования (80% от минимальной стороны)
        val scanAreaSize = minOf(canvasWidth, canvasHeight) * 0.8f
        val scanAreaLeft = (canvasWidth - scanAreaSize) / 2
        val scanAreaTop = (canvasHeight - scanAreaSize) / 2
        val scanAreaRight = scanAreaLeft + scanAreaSize
        val scanAreaBottom = scanAreaTop + scanAreaSize
        
        // Полупрозрачный фон вокруг области сканирования
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = Size(canvasWidth, canvasHeight)
        )
        
        // Прозрачная область сканирования
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(scanAreaLeft, scanAreaTop),
            size = Size(scanAreaSize, scanAreaSize)
        )
        
        // Рамка области сканирования
        drawRect(
            color = Color.Green,
            topLeft = Offset(scanAreaLeft, scanAreaTop),
            size = Size(scanAreaSize, scanAreaSize),
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Уголки рамки
        val cornerLength = 40.dp.toPx()
        val cornerWidth = 8.dp.toPx()
        
        // Левый верхний угол
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaLeft, scanAreaTop),
            end = Offset(scanAreaLeft + cornerLength, scanAreaTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaLeft, scanAreaTop),
            end = Offset(scanAreaLeft, scanAreaTop + cornerLength),
            strokeWidth = cornerWidth
        )
        
        // Правый верхний угол
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaRight, scanAreaTop),
            end = Offset(scanAreaRight - cornerLength, scanAreaTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaRight, scanAreaTop),
            end = Offset(scanAreaRight, scanAreaTop + cornerLength),
            strokeWidth = cornerWidth
        )
        
        // Левый нижний угол
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaLeft, scanAreaBottom),
            end = Offset(scanAreaLeft + cornerLength, scanAreaBottom),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaLeft, scanAreaBottom),
            end = Offset(scanAreaLeft, scanAreaBottom - cornerLength),
            strokeWidth = cornerWidth
        )
        
        // Правый нижний угол
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaRight, scanAreaBottom),
            end = Offset(scanAreaRight - cornerLength, scanAreaBottom),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanAreaRight, scanAreaBottom),
            end = Offset(scanAreaRight, scanAreaBottom - cornerLength),
            strokeWidth = cornerWidth
        )
        
        // Анимационная линия сканирования
        val scanLineY = scanAreaTop + (scanAreaSize * 0.5f)
        drawLine(
            color = Color.Green.copy(alpha = 0.7f),
            start = Offset(scanAreaLeft, scanLineY),
            end = Offset(scanAreaRight, scanLineY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Текст инструкции
        val instructionText = "Наведите камеру на штрих-код"
        drawContext.canvas.nativeCanvas.apply {
            // TODO: Добавить текст инструкции
            // Для этого нужно использовать Paint и drawText
            // но в Compose Canvas нет простого способа рисовать текст
        }
    }
}