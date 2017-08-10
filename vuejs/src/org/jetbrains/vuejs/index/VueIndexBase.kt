package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StringStubIndexExtension

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
abstract class VueIndexBase : StringStubIndexExtension<JSImplicitElementProvider>() {
  private val VERSION = 8
  override fun getVersion(): Int {
    return VERSION
  }
}