package com.example.text2ai

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun parseMarkdownToAnnotatedString(input: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val regex = Regex("""(\*\*[^*]+\*\*|\*[^*]+\*)""")
    var lastIndex = 0

    regex.findAll(input).forEach { matchResult ->
        val start = matchResult.range.first
        val end = matchResult.range.last + 1

        if (lastIndex < start) {
            builder.append(input.substring(lastIndex, start))
        }
        val match = matchResult.value
        val isBold = match.startsWith("**")
        val isItalic = match.startsWith("*") && !isBold

        val content = if (isBold) {
            match.substring(2, match.length - 2)
        } else {
            match.substring(1, match.length - 1)
        }
        builder.withStyle(
            style = SpanStyle(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
            )
        ) {
            append(content)
        }
        lastIndex = end
    }
    if (lastIndex < input.length) {
        builder.append(input.substring(lastIndex))
    }
    return builder.toAnnotatedString()
}