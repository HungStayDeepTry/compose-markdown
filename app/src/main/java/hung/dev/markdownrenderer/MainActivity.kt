package hung.dev.markdownrenderer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hung.dev.markdown_renderer.Markdown
import hung.dev.markdown_renderer.style.MarkdownStyles
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val markdownText = """
                        # Markdown Renderer Test
                        
                        This is **bold text**, *italic text*, ~~strike~~, và `inline code`.
                        * Item 1
                        * Item 2
                        * Item 2a
                        * Item 2b
                            * Item 3a
                            * Item 3b
                                * Item 3ba
                                    * Item 3baa

                        [OpenAI](https://openai.com) 
                        >> UIAUIA
                        >
                        > Hung 
                        > 
                        You may be using [Markdown Live Preview](https://markdownlivepreview.com/)
                        ![alt text](https://upload.wikimedia.org/wikipedia/commons/b/b6/Image_created_with_a_mobile_phone.png) xc `sdfsdfs` dsadasd
                       
                        This is `inline code` in paragraph. 
                        
                        
                        
                        ```kotlin
                        fun main() {
                            println("Hello, Markdown!")
                        }
                        ```
                        ## Tables

                        | Left columns  | Right columns |
                        | ------------- |:-------------:|
                        | **left foo**      | right foo     |
                        | This is an image ![UIA_wtm](https://ih1.redbubble.net/image.5763372730.4107/st,small,507x507-pad,600x600,f8f8f8.jpg)    | right bar     |
                        | left baz      | right baz     |
                    """.trimIndent()

                    Markdown(
                        text = markdownText,
                        styles = MarkdownStyles.default(),
                        modifier = Modifier.padding(start = 8.dp),
                        imageResolver = { url, alt, title ->
                            AsyncImage(
                                model = url,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val markdownText = """
                # Markdown Renderer Preview

                #### This is **bold text**, *italic text*, ~~strike~~, và `inline code`.

                1. Item 1
                2. **Item 2**
                3. Item 3
                
                * Item 1
                * Item 2
                * Item 2a
                * Item 2b
                    * Item 3a
                    * Item 3b
                        * Item 3ba
                            * Item 3baa
                                * i
                                   * Item 3ba
                                        * Item 3baa
                                            * i
                                                * Item 3b
                                                    * Item 3ba
                                                        * Item 3baa
                                                            * i
                                                               * Item 3ba
                                                                    * Item 3baa
                                                                        * i

                > This is a quote 12312313212312312312312312312123123123123123
                >> OT@
                [OpenAI](https://openai.com)

                ![Alt text](https://picsum.photos/200/300)
            """.trimIndent()

            Markdown(
                text = markdownText,
                styles = MarkdownStyles.default(),
                onLinkClicked = { url ->
                    println("Clicked link: $url")
                },
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}
