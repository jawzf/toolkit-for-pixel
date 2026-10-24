/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Utilities - General
 * File: EmojiUtil.kt
 * Description: Validation helpers for single emoji input.
 */

package com.sameerasw.essentials.utils

import android.icu.lang.UCharacter
import android.icu.lang.UProperty
import android.os.Build
import java.text.BreakIterator

object EmojiUtil {
    private const val ZWJ = 0x200D
    private const val VARIATION_SELECTOR_16 = 0xFE0F
    private const val KEYCAP = 0x20E3

    fun isSingleEmoji(input: String): Boolean {
        val text = input.trim()
        if (text.isEmpty()) return false
        val graphemes = BreakIterator.getCharacterInstance()
        graphemes.setText(text)
        graphemes.first()
        if (graphemes.next() != text.length) return false

        val codePoints = text.codePoints().toArray()
        var hasPictographic = false
        for (i in codePoints.indices) {
            val cp = codePoints[i]
            when {
                cp == ZWJ || cp == VARIATION_SELECTOR_16 || cp == KEYCAP -> Unit
                cp in 0xE0020..0xE007F -> Unit
                isEmojiModifier(cp) -> Unit
                isEmoji(cp) -> {
                    val presented = cp > 0xFF || isEmojiPresentation(cp) ||
                        codePoints.getOrNull(i + 1).let { it == VARIATION_SELECTOR_16 || it == KEYCAP }
                    if (presented) hasPictographic = true
                }
                else -> return false
            }
        }
        return hasPictographic
    }

    private fun isEmoji(cp: Int): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            UCharacter.hasBinaryProperty(cp, UProperty.EMOJI)
        } else {
            isFallbackEmoji(cp) || cp in 0x30..0x39 || cp == 0x23 || cp == 0x2A
        }

    private fun isEmojiPresentation(cp: Int): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            UCharacter.hasBinaryProperty(cp, UProperty.EMOJI_PRESENTATION)
        } else {
            isFallbackEmoji(cp) && cp >= 0x1F000
        }

    private fun isEmojiModifier(cp: Int): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            UCharacter.hasBinaryProperty(cp, UProperty.EMOJI_MODIFIER)
        } else {
            cp in 0x1F3FB..0x1F3FF
        }

    private fun isFallbackEmoji(cp: Int): Boolean =
        cp in 0x1F000..0x1FAFF || cp in 0x2600..0x27BF || cp in 0x2300..0x23FF || cp in 0x2B00..0x2BFF
}
