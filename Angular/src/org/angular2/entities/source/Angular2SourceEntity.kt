// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import java.util.*

abstract class Angular2SourceEntity(override val decorator: ES6Decorator,
                                    protected val implicitElement: JSImplicitElement)
  : Angular2SourceEntityBase(getClassForDecoratorElement(decorator)!!) {

  override val navigableElement: JSElement
    get() = decorator

  override val sourceElement: JSElement
    // try to find a fresh implicit element
    get() = decorator.indexingData?.implicitElements
              ?.firstOrNull { el -> implicitElement.name == el.name && implicitElement.userString == el.userString }
            ?: implicitElement

  protected fun <T : Angular2SourceEntity> createPointer(constructor: (ES6Decorator, JSImplicitElement) -> T): Pointer<T> {
    val decoratorPtr = decorator.createSmartPointer()
    val implicitElementPtr = implicitElement.createSmartPointer()
    return Pointer {
      val decorator = decoratorPtr.dereference()
      val implicitElement = implicitElementPtr.dereference()
      if (decorator != null && implicitElement != null) constructor(decorator, implicitElement) else null
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val entity = other as Angular2SourceEntity?
    return decorator == entity!!.decorator && typeScriptClass == entity.typeScriptClass
  }

  override fun hashCode(): Int {
    return Objects.hash(decorator, typeScriptClass)
  }
}
