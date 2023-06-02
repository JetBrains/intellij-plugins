package com.intellij.dts

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

class DtsBundle : DynamicBundle(BUNDLE) {
    companion object {
        const val BUNDLE = "messages.DtsBundle"

        private val INSTANCE = DtsBundle()

        @JvmStatic
        fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
            return INSTANCE.getMessage(key, *params)
        }
    }
}