package org.angular2.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

class Angular2CustomCssPropertyInJsIndex : Angular2IndexBase<JSImplicitElementProvider>() {

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = Angular2CustomCssPropertyInJsIndexKey

}

@JvmField
val Angular2CustomCssPropertyInJsIndexKey: StubIndexKey<String, JSImplicitElementProvider> =
  StubIndexKey.createIndexKey<String, JSImplicitElementProvider>("angular2.js.css.custom-property.index")