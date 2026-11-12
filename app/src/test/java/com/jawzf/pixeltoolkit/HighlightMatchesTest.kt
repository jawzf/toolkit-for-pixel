package com.jawzf.pixeltoolkit

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.jawzf.pixeltoolkit.core.design.components.highlightMatches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HighlightMatchesTest {
    private val style = SpanStyle(fontWeight = FontWeight.Bold)

    @Test
    fun singleMatchHighlightsCorrectRange() {
        val result = highlightMatches("Notification lighting", "light", style)
        assertEquals("Notification lighting", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(13, result.spanStyles[0].start)
        assertEquals(18, result.spanStyles[0].end)
    }

    @Test
    fun caseInsensitiveMatch() {
        val result = highlightMatches("Notification lighting", "LIGHT", style)
        assertEquals("Notification lighting", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(13, result.spanStyles[0].start)
        assertEquals(18, result.spanStyles[0].end)
    }

    @Test
    fun multipleMatchesHighlightAllOccurrences() {
        val result = highlightMatches("Flashlight light", "light", style)
        assertEquals("Flashlight light", result.text)
        assertEquals(2, result.spanStyles.size)
        assertEquals(5, result.spanStyles[0].start)
        assertEquals(10, result.spanStyles[0].end)
        assertEquals(11, result.spanStyles[1].start)
        assertEquals(16, result.spanStyles[1].end)
    }

    @Test
    fun blankQueryReturnsNoSpans() {
        val result = highlightMatches("Notification lighting", "  ", style)
        assertEquals("Notification lighting", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun noMatchReturnsNoSpans() {
        val result = highlightMatches("Notification lighting", "xyz", style)
        assertEquals("Notification lighting", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }
}
