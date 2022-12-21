// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSPrimitiveLiteralType
import com.intellij.psi.util.CachedValueProvider
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2Declaration

abstract class Angular2SourceDeclaration(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceEntity(decorator, implicitElement), Angular2Declaration {

  override val isStandalone: Boolean
    get() = getCachedValue {
      val property = Angular2DecoratorUtil.getProperty(decorator, Angular2DecoratorUtil.STANDALONE_PROP)
      val type = property?.jsType
      val result = type is JSPrimitiveLiteralType<*> && true == type.literal
      CachedValueProvider.Result.create(result, decorator)
    }
}
