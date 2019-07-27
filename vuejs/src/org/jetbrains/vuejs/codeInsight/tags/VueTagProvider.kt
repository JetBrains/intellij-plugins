// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import icons.VuejsIcons
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.*

private const val LOCAL_PRIORITY = 100.0
private const val APP_PRIORITY = 90.0
private const val PLUGIN_PRIORITY = 90.0
private const val GLOBAL_PRIORITY = 80.0
private const val UNREGISTERED_PRIORITY = 50.0

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null && tag.containingFile.language == VueLanguage.INSTANCE && isVueContext(tag)) {
      val tagName = fromAsset(tag.name)

      val components = mutableListOf<VueComponent>()
      VueModelManager.findEnclosingContainer(tag)?.acceptEntities(object : VueModelProximityVisitor() {
        override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
          return acceptSameProximity(proximity, fromAsset(name) == tagName) {
            components.add(component)
          }
        }
      }, VueModelVisitor.Proximity.GLOBAL)

      if (components.isNotEmpty()) return VueElementDescriptor(tag, components)

      if (VUE_FRAMEWORK_COMPONENTS.contains(tagName)) {
        return VueElementDescriptor(tag)
      }
    }
    return null
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, namespacePrefix: String?) {
    elements ?: return
    if (!StringUtil.isEmpty(namespacePrefix) || !isVueContext(tag)) return

    val scriptLanguage = detectVueScriptLanguage(tag.containingFile)

    val nameMapper: (String) -> List<String> = if (VueFileType.INSTANCE == tag.containingFile.fileType)
      { name -> listOf(toAsset(name).capitalize(), fromAsset(name)) }
    else
      { name -> listOf(fromAsset(name)) }

    val providedNames = mutableSetOf<String>()
    VueModelManager.findEnclosingContainer(tag)?.acceptEntities(object : VueModelVisitor() {
      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        val moduleName: String? = if (component.parents.size == 1) {
          (component.parents.first() as? VuePlugin)?.moduleName
        }
        else null
        nameMapper(name).forEach {
          if (providedNames.add(it)) {
            elements.add(createVueLookup(component.source, it,
                                         proximity != Proximity.OUT_OF_SCOPE,
                                         scriptLanguage,
                                         priorityOf(proximity),
                                         moduleName))
          }
        }
        return true
      }
    }, VueModelVisitor.Proximity.OUT_OF_SCOPE)

    elements.addAll(VUE_FRAMEWORK_COMPONENTS.map {
      LookupElementBuilder.create(it).withIcon(VuejsIcons.Vue).withTypeText("vue", true)
    })
  }

  private fun priorityOf(proximity: VueModelVisitor.Proximity): Double {
    return when (proximity) {
      VueModelVisitor.Proximity.OUT_OF_SCOPE -> UNREGISTERED_PRIORITY
      VueModelVisitor.Proximity.GLOBAL -> GLOBAL_PRIORITY
      VueModelVisitor.Proximity.APP -> APP_PRIORITY
      VueModelVisitor.Proximity.PLUGIN -> PLUGIN_PRIORITY
      VueModelVisitor.Proximity.LOCAL -> LOCAL_PRIORITY
    }
  }

  private fun createVueLookup(element: PsiElement?,
                              name: String,
                              shouldNotBeImported: Boolean,
                              scriptLanguage: String?,
                              priority: Double,
                              moduleName: String? = null): LookupElement {
    var builder = (if (element != null) LookupElementBuilder.create(element, name) else LookupElementBuilder.create(name))
      .withIcon(VuejsIcons.Vue)
    if (priority == LOCAL_PRIORITY) {
      builder = builder.bold()
    }
    if (moduleName != null) {
      builder = builder.withTypeText(moduleName, true)
    }
    if (!shouldNotBeImported && element != null) {
      val settings = JSApplicationSettings.getInstance()
      if ((scriptLanguage != null && "ts" == scriptLanguage)
          || (DialectDetector.isTypeScript(element)
              && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
        if (settings.hasTSImportCompletionEffective(element.project)) {
          builder = builder.withInsertHandler(VueInsertHandler.INSTANCE)
        }
      }
      else {
        if (settings.isUseJavaScriptAutoImport) {
          builder = builder.withInsertHandler(VueInsertHandler.INSTANCE)
        }
      }
    }
    return PrioritizedLookupElement.withPriority(builder, priority)
  }

  companion object {
    private val VUE_FRAMEWORK_COMPONENTS = setOf(
      "component",
      "slot"
    )
  }
}

