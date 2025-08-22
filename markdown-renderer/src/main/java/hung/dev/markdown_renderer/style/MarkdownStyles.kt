package hung.dev.markdown_renderer.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MarkdownStyles(
    val paragraph: TextStyle,
    val codeInline: TextStyle,
    val codeBlock: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val quoteBarColor: Color,
    val dividerColor: Color,
    val blockSpacing: androidx.compose.ui.unit.Dp,
) {
    companion object {
        @Composable
        fun default(): MarkdownStyles {
            val t = MaterialTheme.typography
            return MarkdownStyles(
                paragraph = t.bodyLarge,
                codeInline = t.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                codeBlock = t.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, lineHeight = 22.sp),
                h1 = t.headlineLarge,
                h2 = t.headlineMedium,
                h3 = t.headlineSmall,
                h4 = t.titleLarge,
                h5 = t.titleMedium,
                h6 = t.titleSmall,
                quoteBarColor = MaterialTheme.colorScheme.outlineVariant,
                dividerColor = MaterialTheme.colorScheme.outlineVariant,
                blockSpacing = 8.dp
            )
        }
    }
}