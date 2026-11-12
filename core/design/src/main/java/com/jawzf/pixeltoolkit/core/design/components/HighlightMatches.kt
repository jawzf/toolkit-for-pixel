package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/** [text] with every case-insensitive occurrence of [query] styled with [style]; a blank query returns plain text. */
fun highlightMatches(
    text: String,
    query: String,
    style: SpanStyle,
): AnnotatedString {
    val q = query.trim()
    if (q.isEmpty() || text.isEmpty()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = text.indexOf(q, startIndex, ignoreCase = true)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            }
            if (matchIndex > startIndex) {
                append(text.substring(startIndex, matchIndex))
            }
            val matchEnd = matchIndex + q.length
            withStyle(style) {
                append(text.substring(matchIndex, matchEnd))
            }
            startIndex = matchEnd
        }
    }
}
