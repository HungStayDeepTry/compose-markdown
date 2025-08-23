# MarkdownRenderer

A lightweight Jetpack Compose library for rendering GitHub-flavored Markdown (GFM) on Android. Supports headings, paragraphs, lists, tables, quotes, images, links, code blocks, inline styles, and more.

Built on top of [CommonMark Java](https://github.com/commonmark/commonmark-java) parser.

## 📸 Preview

<img src="https://hungstaydeeptry.github.io/Screenshot_20250822_175237.png" alt="Preview Image" width="300"/>

## 🚀 Example Code

```kotlin
val parser = MarkdownParser()
val blocks = parser.parse(
    """
    # Hello MarkdownRenderer
    
    - **Bold**
    - *Italic*
    - ~~Strike~~
    - [Link](https://github.com/hungstaydeeptry)
    
    | Column 1 | Column 2 |
    |----------|----------|
    | A        | B        |
    
    > Quote block
    
    ```
    Code block
    ```
    """.trimIndent()
)

RenderMarkdown(
    blocks = blocks,
    styles = MarkdownStyles.default(),
    onLinkClicked = { url -> 
        println("Clicked: $url") 
    },
    imageResolver = { url, alt, title -> 
        AsyncImage(
            model = url, 
            contentDescription = alt
        ) 
    }
)
```

## 📦 Installation

Add JitPack to your project:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("com.github.hungstaydeeptry:markdownRenderer:1.0.0")
}
```

## ⚙️ Default Properties

- **Headings** → Styled with Material typography (h1–h6)
- **Paragraph** → Material bodyMedium
- **Code Block** → Monospace font, surface variant background
- **Inline Code** → Rounded surface with monospace text
- **Quote Block** → Left bar + indented content
- **Lists** → Ordered & unordered with nested depth styles
- **Tables** → Equalized column widths with border & alignment
- **Links** → Blue + underlined, clickable via onLinkClicked

## 📚 Dependencies

This library uses the following open-source components:

- [CommonMark Java](https://github.com/commonmark/commonmark-java) - BSD 2-Clause License
- [CommonMark GFM Extensions](https://github.com/commonmark/commonmark-java) - BSD 2-Clause License

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues and pull requests to improve the library.

## 📜 License

This project is licensed under the MIT License.

### Third-party Licenses

- CommonMark Java is licensed under the BSD 2-Clause License. See [CommonMark License](https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt)
