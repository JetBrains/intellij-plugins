package tanvd.grazi.utils

import com.intellij.util.lang.UrlClassLoader
import java.net.URL

fun UrlClassLoader.addUrls(urls: Collection<URL>) = with(UrlClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)) {
    isAccessible = true
    urls.forEach { invoke(this@addUrls, it) }
}
