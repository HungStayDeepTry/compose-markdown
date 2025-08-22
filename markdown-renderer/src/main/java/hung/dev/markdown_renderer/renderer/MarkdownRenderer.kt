package hung.dev.markdown_renderer.renderer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hung.dev.markdown_renderer.model.MarkdownBlock
import hung.dev.markdown_renderer.style.MarkdownStyles

@Composable
fun RenderMarkdown(
    blocks: List<MarkdownBlock>,
    styles: MarkdownStyles,
    onLinkClicked: (String) -> Unit,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(styles.blockSpacing),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
    ) {
        blocks.forEach { block ->
            RenderBlock(block, styles, onLinkClicked, imageResolver)
        }
    }
}
