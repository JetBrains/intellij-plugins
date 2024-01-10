// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.types.TypeScriptTypeOfJSTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Entity
import org.angular2.entities.source.Angular2SourceEntityBase
import java.util.*

abstract class Angular2IvyEntity<T : Angular2IvySymbolDef.Entity> protected constructor(protected val myEntityDef: T)
  : Angular2SourceEntityBase(PsiTreeUtil.getContextOfType(myEntityDef.field, TypeScriptClass::class.java)!!) {

  protected val field: TypeScriptField
    get() = myEntityDef.field

  override val decorator: ES6Decorator?
    get() = null

  override val sourceElement: PsiElement
    get() = this.field

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val entity = other as Angular2IvyEntity<*>?
    return field == entity!!.field && typeScriptClass == entity.typeScriptClass
  }

  override fun hashCode(): Int {
    return Objects.hash(field, typeScriptClass)
  }

  companion object {
    @JvmStatic
    protected fun <T : Angular2Entity> resolveTypeofTypeToEntity(typeOfType: TypeScriptTypeofType,
                                                                 symbolClazz: Class<T>,
                                                                 dependencies: MutableSet<Any>): T? {
      val reference = typeOfType.referenceText
      if (reference == null) {
        return null
      }
      val resolvedTypes = TypeScriptTypeOfJSTypeImpl.getTypeOfResultElements(typeOfType, reference)
      resolvedTypes.forEach { type -> dependencies.add(type.containingFile) }
      return resolvedTypes
        .map { el -> Angular2EntitiesProvider.getEntity(el) }
        .filterIsInstance(symbolClazz)
        .firstOrNull()
    }
  }
}
