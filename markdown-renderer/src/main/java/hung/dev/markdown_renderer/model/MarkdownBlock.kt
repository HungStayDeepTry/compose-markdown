package hung.dev.markdown_renderer.model

sealed interface MarkdownBlock

data class ParagraphBlock(val inlines: List<MarkdownInline>) : MarkdownBlock
data class HeadingBlock(val level: Int, val inlines: List<MarkdownInline>) : MarkdownBlock
data class CodeBlock(val code: String, val language: String?) : MarkdownBlock
data class QuoteBlock(val children: List<MarkdownBlock>) : MarkdownBlock
data class ListBlock(val items: List<ListItem>, val ordered: Boolean, val start: Int = 1, val depth: Int = 0) : MarkdownBlock
data class DividerBlock(val dummy: Boolean = true) : MarkdownBlock
data class ImageBlock(val url: String, val alt: String?, val title: String?) : MarkdownBlock
data class TableBlock(val headers: List<List<MarkdownInline>>, val rows: List<List<List<MarkdownInline>>>, val alignments: List<TableAlignment> = emptyList()) : MarkdownBlock
data class ListItem(val children: List<MarkdownBlock>)

enum class TableAlignment {
    LEFT, CENTER, RIGHT, NONE
}