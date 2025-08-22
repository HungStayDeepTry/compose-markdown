package hung.dev.markdown_renderer

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import hung.dev.markdown_renderer.parser.MarkdownParser
import hung.dev.markdown_renderer.renderer.RenderMarkdown
import hung.dev.markdown_renderer.style.MarkdownStyles
import hung.dev.markdown_renderer.utils.openInCustomTab

@Composable
fun Markdown(
    text: String,
    styles: MarkdownStyles = MarkdownStyles.default(),
    onLinkClicked: ((String) -> Unit)? = null,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit = { _, alt, title ->
        Text(text = alt ?: title ?: "[image]")
    },
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultLinkHandler: (String) -> Unit = { url ->
        context.openInCustomTab(url)
    }

    val parser = MarkdownParser()
    val blocks = parser.parse(text)
    RenderMarkdown(blocks, styles, onLinkClicked ?: defaultLinkHandler, imageResolver, modifier)
}

