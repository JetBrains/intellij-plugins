package tanvd.grazi.utils

import com.intellij.util.lang.UrlClassLoader
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URL

fun ClassLoader.defineClass(name: String, data: ByteArray, offset: Int, size: Int): Class<*> =
        with(ClassLoader::class.java.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Int::class.java, Int::class.java)) {
            isAccessible = true
            invoke(this@defineClass, name, data, offset, size) as Class<*>
        }

fun UrlClassLoader.addUrls(urls: Collection<URL>) = with(UrlClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)) {
    isAccessible = true
    urls.forEach { invoke(this@addUrls, it) }
}

@Suppress("UNCHECKED_CAST")
fun <Type> Class<*>.getStaticField(name: String): Type = this.getDeclaredField(name).apply { isAccessible = true }.get(null) as Type

fun Class<*>.setFinalStaticField(name: String, value: Any?) = with(this.getDeclaredField(name)) {
    isAccessible = true

    val mods = Field::class.java.getDeclaredField("modifiers")
    mods.isAccessible = true
    mods.setInt(this, this.modifiers and Modifier.FINAL.inv())

    this.set(null, value)
}
