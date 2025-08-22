package hung.dev.markdown_renderer.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hung.dev.markdown_renderer.model.CodeBlock
import hung.dev.markdown_renderer.model.DividerBlock
import hung.dev.markdown_renderer.model.HeadingBlock
import hung.dev.markdown_renderer.model.ImageBlock
import hung.dev.markdown_renderer.model.ListBlock
import hung.dev.markdown_renderer.model.MarkdownBlock
import hung.dev.markdown_renderer.model.ParagraphBlock
import hung.dev.markdown_renderer.model.QuoteBlock
import hung.dev.markdown_renderer.model.TableAlignment
import hung.dev.markdown_renderer.model.TableBlock
import hung.dev.markdown_renderer.style.MarkdownStyles

@Composable
internal fun RenderBlock(
    block: MarkdownBlock,
    styles: MarkdownStyles,
    onLinkClicked: (String) -> Unit,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit
) {
    when (block) {
        is ParagraphBlock -> RenderInlines(block.inlines, styles, onLinkClicked, imageResolver)

        is HeadingBlock -> {
            val style = when (block.level) {
                1 -> styles.h1
                2 -> styles.h2
                3 -> styles.h3
                4 -> styles.h4
                5 -> styles.h5
                else -> styles.h6
            }
            RenderInlines(
                block.inlines,
                styles,
                onLinkClicked,
                overrideStyle = style,
                imageResolver = imageResolver
            )
        }

        is CodeBlock -> Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(block.code.trimEnd(), style = styles.codeBlock, modifier = Modifier.padding(8.dp))
        }

        is QuoteBlock -> {
            var contentHeight by remember { mutableIntStateOf(0) }

            Row {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(with(LocalDensity.current) { contentHeight.toDp() })
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(styles.quoteBarColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier.onGloballyPositioned { coordinates ->
                        contentHeight = coordinates.size.height
                    }
                ) {
                    block.children.forEach {
                        Spacer(Modifier.height(4.dp))
                        RenderBlock(it, styles, onLinkClicked, imageResolver)
                    }
                }
            }
        }

        is ListBlock -> Column {
            block.items.forEachIndexed { index, item ->
                Row(
                    Modifier.fillMaxWidth()
                ) {
                    val prefix = if (block.ordered) {
                        "${block.start + index}."
                    } else {
                        when (block.depth % 3) {
                            0 -> "•"
                            1 -> "◦"
                            else -> "▪"
                        }
                    }

                    Text(prefix, style = styles.paragraph, modifier = Modifier.width(28.dp))

                    Column {
                        item.children.forEach { child ->
                            if (child is ListBlock) {
                                RenderBlock(
                                    child.copy(depth = block.depth + 1),
                                    styles,
                                    onLinkClicked,
                                    imageResolver
                                )
                            } else {
                                RenderBlock(child, styles, onLinkClicked, imageResolver)
                            }
                        }
                    }
                }
            }
        }

        is TableBlock -> RenderTable(block, styles, onLinkClicked, imageResolver)

        is DividerBlock -> Divider(color = styles.dividerColor)

        is ImageBlock ->
            imageResolver(block.url, block.alt, block.title)
    }
}

@Composable
private fun RenderTable(
    table: TableBlock,
    styles: MarkdownStyles,
    onLinkClicked: (String) -> Unit,
    imageResolver: @Composable (url: String, alt: String?, title: String?) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.padding(4.dp)) {
            SubcomposeLayout { constraints ->
                val columnCount = maxOf(
                    table.headers.size,
                    table.rows.firstOrNull()?.size ?: 0
                )
                if (columnCount == 0) {
                    return@SubcomposeLayout layout(0, 0) {}
                }

                val colMax = IntArray(columnCount)

                fun measureCell(key: String, content: @Composable () -> Unit): Placeable {
                    val meas = subcompose(key, content).first()
                        .measure(
                            constraints.copy(
                                minWidth = 0,
                                maxWidth = constraints.maxWidth
                            )
                        )
                    return meas
                }

                table.headers.forEachIndexed { col, headerCell ->
                    val p = measureCell("h_m_$col") {
                        Box(Modifier.padding(8.dp)) {
                            RenderInlines(
                                inlines = headerCell,
                                styles = styles,
                                onLinkClicked = onLinkClicked,
                                imageResolver = imageResolver,
                                overrideStyle = styles.paragraph.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    colMax[col] = maxOf(colMax[col], p.width)
                }

                table.rows.forEachIndexed { r, row ->
                    row.forEachIndexed { col, cell ->
                        val p = measureCell("r_m_${r}_$col") {
                            Box(Modifier.padding(8.dp)) {
                                RenderInlines(
                                    inlines = cell,
                                    styles = styles,
                                    onLinkClicked = onLinkClicked,
                                    imageResolver = imageResolver
                                )
                            }
                        }
                        colMax[col] = maxOf(colMax[col], p.width)
                    }
                }

                val tableWidthPx = colMax.sum()
                val strokePx = with(this) { 0.5.dp.roundToPx() }
                var headerHeight = 0
                val headerPlaceables = ArrayList<Placeable>(columnCount)

                table.headers.forEachIndexed { col, headerCell ->
                    val contentAlignment =
                        when (table.alignments.getOrNull(col) ?: TableAlignment.LEFT) {
                            TableAlignment.CENTER -> Alignment.Center
                            TableAlignment.RIGHT -> Alignment.CenterEnd
                            else -> Alignment.CenterStart
                        }

                    val pl = subcompose("h_p_$col") {
                        Box(
                            modifier = Modifier
                                .width(with(this) { colMax[col].toDp() })
                                .border(0.5.dp, MaterialTheme.colorScheme.outline)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = contentAlignment
                        ) {
                            Box(Modifier.padding(8.dp)) {
                                RenderInlines(
                                    inlines = headerCell,
                                    styles = styles,
                                    onLinkClicked = onLinkClicked,
                                    imageResolver = imageResolver,
                                    overrideStyle = styles.paragraph.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }.first().measure(
                        constraints.copy(
                            minWidth = colMax[col],
                            maxWidth = colMax[col]
                        )
                    )
                    headerHeight = maxOf(headerHeight, pl.height)
                    headerPlaceables += pl
                }

                val rowHeights = IntArray(table.rows.size)
                val rowPlaceMatrix: MutableList<List<Placeable>> = ArrayList(table.rows.size)

                table.rows.forEachIndexed { r, row ->
                    val rowPls = ArrayList<Placeable>(columnCount)
                    var maxH = 0
                    repeat(columnCount) { col ->
                        val cellInlines = row.getOrNull(col) ?: emptyList()
                        val contentAlignment =
                            when (table.alignments.getOrNull(col) ?: TableAlignment.LEFT) {
                                TableAlignment.CENTER -> Alignment.Center
                                TableAlignment.RIGHT -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        val pl = subcompose("r_p_${r}_$col") {
                            Box(
                                modifier = Modifier
                                    .width(with(this) { colMax[col].toDp() })
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                contentAlignment = contentAlignment
                            ) {
                                Box(Modifier.padding(8.dp)) {
                                    RenderInlines(
                                        inlines = cellInlines,
                                        styles = styles,
                                        onLinkClicked = onLinkClicked,
                                        imageResolver = imageResolver
                                    )
                                }
                            }
                        }.first().measure(
                            constraints.copy(
                                minWidth = colMax[col],
                                maxWidth = colMax[col]
                            )
                        )
                        maxH = maxOf(maxH, pl.height)
                        rowPls += pl
                    }
                    rowHeights[r] = maxH
                    rowPlaceMatrix += rowPls
                }

                val contentHeight =
                    headerHeight + rowHeights.sum() +
                            0

                layout(tableWidthPx, contentHeight) {
                    var y = 0
                    var x = 0
                    headerPlaceables.forEachIndexed { col, pl ->
                        pl.place(x, y)
                        x += colMax[col]
                    }
                    y += headerHeight

                    table.rows.indices.forEach { r ->
                        x = 0
                        rowPlaceMatrix[r].forEachIndexed { col, pl ->
                            pl.place(x, y)
                            x += colMax[col]
                        }
                        y += rowHeights[r]
                    }
                }
            }
        }
    }
}