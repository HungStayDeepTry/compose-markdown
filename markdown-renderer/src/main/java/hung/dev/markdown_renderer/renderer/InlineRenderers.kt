package hung.dev.markdown_renderer.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import hung.dev.markdown_renderer.model.BoldInline
import hung.dev.markdown_renderer.model.CodeInline
import hung.dev.markdown_renderer.model.ImageInline
import hung.dev.markdown_renderer.model.ItalicInline
import hung.dev.markdown_renderer.model.LineBreakInline
import hung.dev.markdown_renderer.model.LinkInline
import hung.dev.markdown_renderer.model.MarkdownInline
import hung.dev.markdown_renderer.model.StrikeInline
import hung.dev.markdown_renderer.model.TextInline
import hung.dev.markdown_renderer.style.MarkdownStyles

@Composable
internal fun RenderInlines(
    inlines: List<MarkdownInline>,
    styles: MarkdownStyles,
    onLinkClicked: (String) -> Unit,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit,
    overrideStyle: androidx.compose.ui.text.TextStyle? = null
) {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    var imageCounter = 0

    val annotated = buildAnnotatedString {
        appendInlines(inlines, inlineContent, { imageCounter++ }, imageResolver)
    }

    if (inlineContent.isNotEmpty()) {
        Text(
            text = annotated,
            style = overrideStyle ?: styles.paragraph,
            inlineContent = inlineContent
        )
    } else {
        ClickableText(
            text = annotated,
            style = overrideStyle ?: styles.paragraph,
            onClick = { offset ->
                annotated
                    .getStringAnnotations("URL", offset, offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        onLinkClicked(annotation.item)
                    }
            }
        )
    }
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<MarkdownInline>,
    inlineContent: MutableMap<String, InlineTextContent>,
    getImageId: () -> Int,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit = { _, _, str -> }
) {
    inlines.forEach { inline ->
        when (inline) {
            is TextInline -> append(inline.text)
            is BoldInline -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlines(inline.children, inlineContent, getImageId)
            }

            is ItalicInline -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlines(inline.children, inlineContent, getImageId)
            }

            is StrikeInline -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                appendInlines(inline.children, inlineContent, getImageId)
            }

            is CodeInline -> {
                val id = "code_${getImageId()}"
                appendInlineContent(id, "\u200B")

                inlineContent[id] = InlineTextContent(
                    placeholder = Placeholder(
                        width = (inline.code.length * 0.6 + 1).em,
                        height = 1.4.em,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Surface(
                        color = Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = inline.code,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            is LinkInline -> {
                val start = length
                appendInlines(inline.children, inlineContent, getImageId)
                addStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    ),
                    start,
                    length
                )
                addStringAnnotation("URL", inline.url, start, length)
            }

            is ImageInline -> {
                val imageId = "image_${getImageId()}"
                appendInlineContent(imageId, inline.alt ?: "[image]")

                inlineContent[imageId] = InlineTextContent(
                    placeholder = Placeholder(
                        width = 2.em,
                        height = 1.2.em,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    )
                ) {
                    imageResolver(inline.url, inline.alt, inline.title)
                }
            }

            is LineBreakInline -> append("\n")
        }
    }
}
