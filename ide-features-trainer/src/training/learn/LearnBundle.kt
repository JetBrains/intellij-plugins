package training.learn

import com.intellij.CommonBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*

/**
 * Created by karashevich on 09/09/15.
 */
open class BundlePlace(val bundleAppendix: String)

object LearnBundle {

  private var ourBundle: Reference<ResourceBundle>? = null

  @NonNls
  private const val BUNDLE = "training.learn.LearnBundle"

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return CommonBundle.message(bundle, key, *params)
  }

  fun messageInPlace(@PropertyKey(resourceBundle = BUNDLE) key: String, bundlePlace: BundlePlace, vararg params: Any): String {
    return CommonBundle.message(bundle, key + bundlePlace.bundleAppendix, *params)
  }

  // Cached loading
  private val bundle: ResourceBundle
    get() {
      var bundle = SoftReference.dereference(ourBundle)
      if (bundle == null) {
        bundle = ResourceBundle.getBundle(BUNDLE)
        ourBundle = SoftReference<ResourceBundle>(bundle)
      }
      return bundle!!
    }
}