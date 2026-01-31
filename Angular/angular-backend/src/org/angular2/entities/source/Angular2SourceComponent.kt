// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider.Result.create
import org.angular2.Angular2DecoratorUtil.STYLES_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URLS_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URL_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.entities.Angular2ClassBasedComponent
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2ModuleResolver
import org.angular2.entities.source.Angular2SourceUtil.getReferencedFile

class Angular2SourceComponent(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceDirective(decorator, implicitElement), Angular2ClassBasedComponent {

  private var moduleResolverField: Angular2ModuleResolver<ES6Decorator>? = null
  private val moduleResolver: Angular2ModuleResolver<ES6Decorator>
    get() = moduleResolverField
            ?: Angular2ModuleResolver({ decorator }, Angular2SourceModule.symbolCollector)
              .also { moduleResolverField = it }

  override val imports: Set<Angular2Entity>
    get() = if (isStandalone)
      moduleResolver.imports
    else
      emptySet()

  override val forwardRefImports: Set<Angular2Entity>
    get() = if (isStandalone)
      moduleResolver.forwardRefImports
    else
      emptySet()

  override val isScopeFullyResolved: Boolean
    get() = if (isStandalone)
      moduleResolver.isScopeFullyResolved
    else
      true

  override val templateFile: PsiFile?
    get() = getCachedValue {
      create(findAngularComponentTemplate(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator)
    }

  override val cssFiles: List<PsiFile>
    get() = getCachedValue { create(findCssFiles(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator) }

  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() = getCachedValue {
      create(Angular2SourceUtil.getNgContentSelectors(templateFile),
             VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, decorator)
    }

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<out Angular2Component> {
    return createPointer { decorator, implicitElement ->
      Angular2SourceComponent(decorator, implicitElement)
    }
  }

  private fun getDecoratorProperty(name: String): JSProperty? {
    return getProperty(decorator, name)
  }

  private fun findAngularComponentTemplate(): PsiFile? {
    val file = getReferencedFile(getDecoratorProperty(TEMPLATE_URL_PROP), true)
    return file ?: getReferencedFile(getDecoratorProperty(TEMPLATE_PROP), false)
  }

  private fun findCssFiles(): List<PsiFile> {
    return Angular2SourceUtil.findCssFiles(getDecoratorProperty(STYLE_URLS_PROP), true)
      .plus(Angular2SourceUtil.findCssFiles(getDecoratorProperty(STYLES_PROP), false))
      .plus(listOfNotNull(getReferencedFile(getDecoratorProperty(STYLES_PROP), false),
                          getReferencedFile(getDecoratorProperty(STYLE_URL_PROP), true)))
      .toList()
  }

}
