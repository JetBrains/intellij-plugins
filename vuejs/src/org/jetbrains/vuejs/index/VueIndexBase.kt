// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
abstract class VueIndexBase(private val key: StubIndexKey<String, JSImplicitElementProvider>,
                            jsKey: String) : StringStubIndexExtension<JSImplicitElementProvider>() {
  private val VERSION = 25

  init {
    // this is called on index==application component initialization
    JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(jsKey)
  }

  companion object {
    fun createJSKey(key: StubIndexKey<String, JSImplicitElementProvider>) =
      key.name.split(".").joinToString("") { it.subSequence(0, 1) }
  }

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = key

  override fun getVersion(): Int {
    return VERSION
  }
}