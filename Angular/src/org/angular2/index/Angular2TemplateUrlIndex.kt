// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

class Angular2TemplateUrlIndex : Angular2IndexBase<JSImplicitElementProvider>() {

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = Angular2TemplateUrlIndexKey

}

@JvmField
val Angular2TemplateUrlIndexKey: StubIndexKey<String, JSImplicitElementProvider> =
  StubIndexKey.createIndexKey<String, JSImplicitElementProvider>("angular2.templateUrl.index")