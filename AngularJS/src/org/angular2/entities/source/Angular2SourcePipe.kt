// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.psi.util.CachedValueProvider
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.Angular2Pipe

class Angular2SourcePipe(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceDeclaration(decorator, implicitElement), Angular2Pipe {

  override fun getName(): String = implicitElement.name

  override val transformMembers: Collection<JSElement>
    get() = getCachedValue {
      CachedValueProvider.Result.create(
        Angular2EntityUtils.getPipeTransformMembers(typeScriptClass), classModificationDependencies)
    }

  override fun createPointer(): Pointer<out Angular2Pipe> {
    return createPointer { decorator, implicitElement ->
      Angular2SourcePipe(decorator, implicitElement)
    }
  }
}
