package hung.dev.markdown_renderer.model

sealed interface MarkdownInline
data class TextInline(val text: String) : MarkdownInline
data class BoldInline(val children: List<MarkdownInline>) : MarkdownInline
data class ItalicInline(val children: List<MarkdownInline>) : MarkdownInline
data class StrikeInline(val children: List<MarkdownInline>) : MarkdownInline
data class CodeInline(val code: String) : MarkdownInline
data class LinkInline(val url: String, val children: List<MarkdownInline>) : MarkdownInline
data class ImageInline(val url: String, val alt: String?, val title: String?) : MarkdownInline
data class LineBreakInline(val dummy: Boolean = true) : MarkdownInline