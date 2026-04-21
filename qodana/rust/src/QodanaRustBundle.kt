package org.intellij.qodana.rust

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE: @NonNls String = "messages.QodanaRustBundle"

object QodanaRustBundle {
    private val INSTANCE = DynamicBundle(QodanaRustBundle::class.java, BUNDLE)

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String = INSTANCE.getMessage(key, *params)
}
