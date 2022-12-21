// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2EntityUtils
import org.angular2.lang.Angular2Bundle

abstract class Angular2SourceEntityBase protected constructor(override val typeScriptClass: TypeScriptClass) : UserDataHolderBase(), Angular2Entity {

  override fun getName(): String = StringUtil.notNullize(typeScriptClass.name, Angular2Bundle.message("angular.description.unnamed"))

  protected val classModificationDependencies: Collection<Any>
    get() = getCachedValue {
      val dependencies = HashSet<Any>()
      JSClassUtils.processClassesInHierarchy(typeScriptClass, true) { aClass, _, _ ->
        dependencies.add(aClass)
        true
      }
      CachedValueProvider.Result.create<Collection<Any>>(dependencies, dependencies)
    }

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  protected fun <T> getCachedValue(provider: CachedValueProvider<T>): T {
    return CachedValuesManager.getManager(typeScriptClass.project).getCachedValue(this, provider)
  }

  /**
   * Since Ivy entities are cached on TypeScriptClass dependencies, we can avoid caching for values depending solely on class contents.
   */
  protected fun <T : Any> getLazyValue(key: Key<T>, provider: () -> T): T {
    val result = getUserData(key)
    return result ?: putUserDataIfAbsent(key, provider())
  }

  /**
   * Since Ivy entities are cached on TypeScriptClass dependencies, we can avoid caching for values depending solely on class contents.
   */
  protected fun <T> getNullableLazyValue(key: Key<T>, provider: () -> T?): T? {
    var result = getUserData(key)
    if (result === NULL_MARK) {
      return null
    }
    if (result == null) {
      result = provider()
      if (result == null) {
        @Suppress("UNCHECKED_CAST", "NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER")
        putUserDataIfAbsent(key, NULL_MARK as T)
      }
      else {
        return putUserDataIfAbsent(key, result)
      }
    }
    return result
  }

  companion object {

    private val NULL_MARK = Any()
  }
}
