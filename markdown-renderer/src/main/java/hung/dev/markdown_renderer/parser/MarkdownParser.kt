package hung.dev.markdown_renderer.parser

import hung.dev.markdown_renderer.model.MarkdownBlock
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

class MarkdownParser {

    private val extensions: List<Extension> = listOf(
        AutolinkExtension.create(),
        StrikethroughExtension.create(),
        TablesExtension.create()
    )

    private val parser: Parser = Parser.builder().extensions(extensions).build()

    fun parse(text: String): List<MarkdownBlock> {
        val root = parser.parse(text)
        return NodeMapper().map(root)
    }
}