package hung.dev.markdown_renderer.parser

import hung.dev.markdown_renderer.model.BoldInline
import hung.dev.markdown_renderer.model.CodeBlock
import hung.dev.markdown_renderer.model.CodeInline
import hung.dev.markdown_renderer.model.DividerBlock
import hung.dev.markdown_renderer.model.HeadingBlock
import hung.dev.markdown_renderer.model.ImageBlock
import hung.dev.markdown_renderer.model.ImageInline
import hung.dev.markdown_renderer.model.ItalicInline
import hung.dev.markdown_renderer.model.LineBreakInline
import hung.dev.markdown_renderer.model.LinkInline
import hung.dev.markdown_renderer.model.ListBlock
import hung.dev.markdown_renderer.model.ListItem
import hung.dev.markdown_renderer.model.MarkdownBlock
import hung.dev.markdown_renderer.model.MarkdownInline
import hung.dev.markdown_renderer.model.ParagraphBlock
import hung.dev.markdown_renderer.model.QuoteBlock
import hung.dev.markdown_renderer.model.StrikeInline
import hung.dev.markdown_renderer.model.TableAlignment
import hung.dev.markdown_renderer.model.TableBlock
import hung.dev.markdown_renderer.model.TextInline
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.ext.gfm.tables.TableBlock as CmTable
import org.commonmark.node.ListBlock as CmListBlock
import org.commonmark.node.ListItem as CmListItem

class NodeMapper {

    fun map(node: Node): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        var child = node.firstChild
        while (child != null) {
            blocks += mapBlock(child)
            child = child.next
        }
        return blocks
    }

    private fun mapBlock(node: Node): List<MarkdownBlock> = when (node) {
        is Paragraph -> {
            val inlines = mapInlines(node)
            val cleaned = inlines.filterNot { it is TextInline && it.text.isBlank() }

            if (cleaned.size == 1 && cleaned.first() is ImageInline) {
                val img = cleaned.first() as ImageInline
                listOf(ImageBlock(img.url, img.alt, img.title))

            } else if (cleaned.isNotEmpty() && cleaned.last() is ImageInline) {
                val lastImage = cleaned.last() as ImageInline
                val before = cleaned.dropLast(1)
                buildList {
                    if (before.isNotEmpty()) add(ParagraphBlock(before))
                    add(ImageBlock(lastImage.url, lastImage.alt, lastImage.title))
                }

            } else {
                listOf(ParagraphBlock(inlines))
            }
        }

        is Heading -> listOf(HeadingBlock(node.level, mapInlines(node)))
        is FencedCodeBlock -> listOf(CodeBlock(node.literal, node.info))
        is IndentedCodeBlock -> listOf(CodeBlock(node.literal, null))
        is BlockQuote -> listOf(QuoteBlock(map(node)))
        is BulletList -> listOf(ListBlock(mapListItems(node), ordered = false))
        is OrderedList -> listOf(
            ListBlock(
                mapListItems(node),
                ordered = true,
                start = node.startNumber
            )
        )

        is ThematicBreak -> listOf(DividerBlock())
        is CmTable -> listOf(mapTable(node))
        else -> listOf(ParagraphBlock(mapInlines(node)))
    }

    private fun mapListItems(list: CmListBlock): List<ListItem> {
        val result = mutableListOf<ListItem>()
        var item = list.firstChild
        while (item is CmListItem) {
            result += ListItem(map(item))
            item = item.next as? CmListItem
        }
        return result
    }

    private fun mapInlines(parent: Node): List<MarkdownInline> {
        val list = mutableListOf<MarkdownInline>()
        var child = parent.firstChild
        while (child != null) {
            list += when (child) {
                is Text -> TextInline(child.literal)
                is Emphasis -> ItalicInline(mapInlines(child))
                is StrongEmphasis -> BoldInline(mapInlines(child))
                is Strikethrough -> StrikeInline(mapInlines(child))
                is Code -> CodeInline(child.literal)
                is Link -> LinkInline(child.destination, mapInlines(child))
                is Image -> ImageInline(
                    url = child.destination,
                    alt = (child.firstChild as? Text)?.literal,
                    title = child.title
                )

                is SoftLineBreak, is HardLineBreak -> LineBreakInline()
                else -> TextInline("")
            }
            child = child.next
        }
        return list
    }

    private fun mapTable(table: CmTable): TableBlock {
        val headers = mutableListOf<List<MarkdownInline>>()
        val rows = mutableListOf<List<List<MarkdownInline>>>()
        val alignments = mutableListOf<TableAlignment>()

        var child = table.firstChild
        while (child != null) {
            when (child) {
                is TableHead -> {
                    val headerRow = child.firstChild as? TableRow
                    headerRow?.let { row ->
                        var cell = row.firstChild
                        while (cell is TableCell) {
                            headers.add(mapInlines(cell))
                            alignments.add(
                                when (cell.alignment) {
                                    TableCell.Alignment.LEFT -> TableAlignment.LEFT
                                    TableCell.Alignment.CENTER -> TableAlignment.CENTER
                                    TableCell.Alignment.RIGHT -> TableAlignment.RIGHT
                                    else -> TableAlignment.NONE
                                }
                            )
                            cell = cell.next as? TableCell
                        }
                    }
                }

                is TableBody -> {
                    var row = child.firstChild
                    while (row is TableRow) {
                        val rowCells = mutableListOf<List<MarkdownInline>>()
                        var cell = row.firstChild
                        while (cell is TableCell) {
                            rowCells.add(mapInlines(cell))
                            cell = cell.next as? TableCell
                        }
                        rows.add(rowCells)
                        row = row.next as? TableRow
                    }
                }
            }
            child = child.next
        }

        return TableBlock(headers, rows, alignments)
    }
}