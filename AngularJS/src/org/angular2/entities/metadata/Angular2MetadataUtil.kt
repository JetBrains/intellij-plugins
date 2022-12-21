// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.util.Ref
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.ObjectUtils.tryCast
import org.angular2.entities.Angular2DirectiveProperties
import org.angular2.entities.Angular2EntitiesProvider.isDeclaredClass
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase
import org.angular2.entities.metadata.psi.Angular2MetadataEntity
import org.angular2.entities.metadata.psi.Angular2MetadataFunction
import org.angular2.index.Angular2MetadataClassNameIndex
import org.angular2.index.Angular2MetadataFunctionIndex

object Angular2MetadataUtil {
  fun findMetadataFunction(function: JSFunction): Angular2MetadataFunction? {
    val parent = function.context as? TypeScriptClass
    if (function.name == null || parent == null) {
      return null
    }
    val result = Ref<Angular2MetadataFunction>()
    StubIndex.getInstance().processElements(
      Angular2MetadataFunctionIndex.KEY, function.name!!, function.project,
      GlobalSearchScope.allScope(function.project),
      Angular2MetadataFunction::class.java) { f ->
      if (f.isValid) {
        if (parent == (f.context as? Angular2MetadataClassBase<*>)?.typeScriptClass) {
          result.set(f)
          return@processElements false
        }
      }
      true
    }
    return result.get()
  }

  fun getMetadataEntity(typeScriptClass: TypeScriptClass): Angular2MetadataEntity<*>? {
    return getMetadataClass(typeScriptClass, Angular2MetadataEntity::class.java)
  }

  private fun <T : Angular2MetadataClassBase<*>> getMetadataClass(typeScriptClass: TypeScriptClass, clazz: Class<T>): T? {
    val className = typeScriptClass.name
    if (className == null
        //check classes only from d.ts files
        || !isDeclaredClass(typeScriptClass)) {
      return null
    }
    val result = Ref<T>()
    StubIndex.getInstance().processElements(
      Angular2MetadataClassNameIndex.KEY, className, typeScriptClass.project,
      GlobalSearchScope.allScope(typeScriptClass.project), Angular2MetadataClassBase::class.java
    ) { e ->
      val casted = tryCast(e, clazz)
      if (casted != null && casted.isValid && casted.typeScriptClass === typeScriptClass) {
        result.set(casted)
        return@processElements false
      }
      true
    }
    return result.get()
  }

  fun getMetadataClassDirectiveProperties(typeScriptClass: TypeScriptClass): Angular2DirectiveProperties? {
    return getMetadataClass(typeScriptClass, Angular2MetadataClassBase::class.java)?.bindings
  }
}
