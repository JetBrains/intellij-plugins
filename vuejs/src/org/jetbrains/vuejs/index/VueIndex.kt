package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor

val INDICES:MutableMap<StubIndexKey<String, JSImplicitElementProvider>, String> = mutableMapOf()

fun getAllKeys(scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): Collection<JSImplicitElement> {
  val keys = StubIndex.getInstance().getAllKeys(key, scope.project!!)
  return keys.mapNotNull {
    resolve(it, scope, key)
  }
}

fun resolve(name:String, scope:GlobalSearchScope, key:StubIndexKey<String, JSImplicitElementProvider>): JSImplicitElement? {
  var result:JSImplicitElement? = null
  StubIndex.getInstance().processElements(key, name, scope.project!!, scope, JSImplicitElementProvider::class.java, Processor {
    val element = (it.indexingData?.implicitElements ?: emptyList()).firstOrNull { it.userString == INDICES[key] }
    result = element
    return@Processor element != null
  })
  return result
}