// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.NullableConsumer
import com.intellij.util.containers.Stack
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.metadata.stubs.Angular2MetadataEntityStub

abstract class Angular2MetadataEntity<Stub : Angular2MetadataEntityStub<*>>(element: Stub)
  : Angular2MetadataClassBase<Stub>(element), Angular2Entity {

  override val decorator: ES6Decorator?
    get() = null


  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  companion object {

    @JvmStatic
    protected fun collectReferencedElements(root: PsiElement,
                                            consumer: NullableConsumer<in PsiElement>,
                                            cacheDependencies: MutableSet<PsiElement>?) {
      val resolveQueue = Stack(root)
      val visited = HashSet<PsiElement>()
      while (!resolveQueue.empty()) {
        ProgressManager.checkCanceled()
        val element = resolveQueue.pop()
        if (element != null && !visited.add(element)) {
          // Protect against cyclic references or visiting same thing several times
          continue
        }
        if (cacheDependencies != null && element != null) {
          cacheDependencies.add(element)
        }
        when (element) {
          is Angular2MetadataArray -> resolveQueue.addAll(listOf(*element.children))
          is Angular2MetadataReference -> resolveQueue.push(element.resolve())
          is Angular2MetadataCall -> resolveQueue.push(element.value)
          is Angular2MetadataSpread -> resolveQueue.push(element.expression)
          else -> consumer.consume(element)
        }
      }
    }
  }
}
