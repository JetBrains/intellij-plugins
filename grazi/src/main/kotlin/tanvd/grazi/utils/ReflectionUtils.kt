// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.utils

import com.intellij.util.lang.UrlClassLoader
import java.net.URL

fun UrlClassLoader.addUrls(urls: Collection<URL>) = with(UrlClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)) {
    isAccessible = true
    urls.forEach { invoke(this@addUrls, it) }
}
