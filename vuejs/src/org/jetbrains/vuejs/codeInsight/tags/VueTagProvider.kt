// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.Language
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.*
import java.util.*

private const val LOCAL_PRIORITY = 100.0
private const val APP_PRIORITY = 90.0
private const val PLUGIN_PRIORITY = 90.0
private const val GLOBAL_PRIORITY = 80.0
private const val UNREGISTERED_PRIORITY = 50.0

val CUSTOM_TOP_LEVEL_TAGS: Map<String, (XmlTag, XmlTextImpl) -> Language?> = mapOf(
  Pair("i18n", { tag, text ->
    tag.getAttributeValue(LANG_ATTRIBUTE_NAME)
      ?.let { lang -> Language.getRegisteredLanguages().find { it.id.equals(lang, true) } }
    ?: if (text.chars.find { !it.isWhitespace() }?.let { it == '{' || it == '[' } != false)
      Language.findLanguageByID("JSON")
    else
      Language.findLanguageByID("yaml")
  })
)

fun resolveComponent(context: VueEntitiesContainer, tagName: String, containingFile: PsiFile): List<VueComponent> {
  val result = mutableListOf<VueComponent>()
  val normalizedTagName = fromAsset(tagName)
  context.acceptEntities(object : VueModelProximityVisitor() {
    override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
      return acceptSameProximity(proximity, fromAsset(name) == normalizedTagName) {
        // Cannot self refer without export declaration with component name
        if ((component.source as? JSImplicitElement)?.context != containingFile) {
          result.add(component)
        }
      }
    }
  }, VueModelVisitor.Proximity.GLOBAL)
  return result
}

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag == null
        || DumbService.isDumb(tag.project)
        || !isVueContext(tag)) return null


    VueModelManager.findEnclosingContainer(tag)
      ?.let { resolveComponent(it, tag.name, tag.containingFile.originalFile) }
      ?.takeIf { it.isNotEmpty() }
      ?.let { return VueElementDescriptor(tag, it) }
    return CUSTOM_TOP_LEVEL_TAGS[tag.name.toLowerCase(Locale.US)]?.let { VueElementDescriptor(tag) }
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, namespacePrefix: String?) {
    elements ?: return
    if (!StringUtil.isEmpty(namespacePrefix) || !isVueContext(tag)) return

    val scriptLanguage = detectVueScriptLanguage(tag.containingFile)

    val nameMapper: (String) -> List<String> = if (VueFileType.INSTANCE == tag.containingFile.fileType)
      { name -> listOf(toAsset(name).capitalize(), fromAsset(name)) }
    else
      { name -> listOf(fromAsset(name)) }

    val containingFile = tag.containingFile.originalFile
    val providedNames = mutableSetOf<String>()
    VueModelManager.findEnclosingContainer(tag)?.acceptEntities(object : VueModelVisitor() {
      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        // Cannot self refer without export declaration with component name
        if ((component.source as? JSImplicitElement)?.context == containingFile) {
          return true
        }
        val moduleName: String? = if (component.parents.size == 1) {
          (component.parents.first() as? VuePlugin)?.moduleName
        }
        else null
        nameMapper(name).forEach {
          if (providedNames.add(it)) {
            elements.add(createVueLookup(component, it,
                                         proximity != Proximity.OUT_OF_SCOPE,
                                         scriptLanguage,
                                         priorityOf(proximity),
                                         moduleName))
          }
        }
        return true
      }
    }, VueModelVisitor.Proximity.OUT_OF_SCOPE)
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

  private fun createVueLookup(component: VueComponent,
                              name: String,
                              shouldNotBeImported: Boolean,
                              scriptLanguage: String?,
                              priority: Double,
                              moduleName: String? = null): LookupElement {
    val element = component.source
    var builder = LookupElementBuilder.create(Pair(component.documentation,
                                                   element?.let(SmartPointerManager::createPointer)),
                                              name)
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

}

