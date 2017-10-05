package org.jetbrains.vuejs

import com.intellij.CommonBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*

/**
 * @author Irina.Chernushina on 10/4/2017.
 */
class VueBundle {
  companion object {
    private const val BUNDLE = "messages.VueBundle"

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
      val bundle = getBundle() ?: return ""
      return CommonBundle.message(bundle, key, *params)
    }

    private var ourBundle: Reference<ResourceBundle>? = null
    @NonNls

    private fun getBundle(): ResourceBundle? {
      var bundle = com.intellij.reference.SoftReference.dereference(ourBundle)
      if (bundle == null) {
        bundle = ResourceBundle.getBundle(BUNDLE)
        ourBundle = SoftReference(bundle)
      }
      return bundle
    }
  }
}