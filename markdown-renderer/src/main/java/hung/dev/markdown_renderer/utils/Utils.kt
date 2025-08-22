package hung.dev.markdown_renderer.utils

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent

fun Context.openInCustomTab(url: String) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(this, android.net.Uri.parse(url))
}