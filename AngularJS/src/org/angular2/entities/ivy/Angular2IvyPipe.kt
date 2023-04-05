// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.model.Pointer
import com.intellij.psi.util.CachedValueProvider
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.Angular2Pipe
import org.angular2.lang.Angular2Bundle

class Angular2IvyPipe(entityDef: Angular2IvySymbolDef.Pipe) : Angular2IvyDeclaration<Angular2IvySymbolDef.Pipe>(entityDef), Angular2Pipe {

  override fun getName(): String = myEntityDef.name ?: Angular2Bundle.message("angular.description.unnamed")

  override val transformMembers: Collection<JSElement>
    get() = getCachedValue {
      CachedValueProvider.Result.create(
        Angular2EntityUtils.getPipeTransformMembers(typeScriptClass), classModificationDependencies)
    }

  override fun createPointer(): Pointer<Angular2IvyPipe> {
    val source = myEntityDef.createPointer()
    return Pointer {
      source.dereference()?.let { Angular2IvyPipe(it) }
    }
  }
}
