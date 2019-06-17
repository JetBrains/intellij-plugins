// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.*
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import icons.VuejsIcons
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributesProvider
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponentDetailsProvider.Companion.getBoundName

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
      (object : VueModelProximityVisitor() {
        override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
          return visitSameProximity(proximity) {
            if (fromAsset(name) == tagName) {
              components.add(component)
              false
            }
            else {
              true
            }
          }
        }
      }).visitAllContextScope(tag, VueModelVisitor.Proximity.GLOBAL)

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
    (object : VueModelVisitor() {
      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        nameMapper(name).forEach {
          if (providedNames.add(it))
            elements.add(createVueLookup(component.source, it,
                                         proximity != Proximity.OUT_OF_SCOPE,
                                         scriptLanguage,
                                         priorityOf(proximity)))
        }
        return true
      }
    }).visitAllContextScope(tag, VueModelVisitor.Proximity.OUT_OF_SCOPE)

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
                              priority: Double): LookupElement {
    var builder = (if (element != null) LookupElementBuilder.create(element, name) else LookupElementBuilder.create(name))
      .withIcon(VuejsIcons.Vue)
    if (priority == LOCAL_PRIORITY) {
      builder = builder.bold()
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
      "keep-alive",
      "slot",
      "transition",
      "transition-group"
    )
  }
}

class VueElementDescriptor(private val tag: XmlTag, private val sources: Collection<Any> = emptyList()) : XmlElementDescriptor {
  companion object {
    // it is better to use default attributes method since it is guaranteed to do not call any extension providers
    fun getDefaultHtmlAttributes(context: XmlTag?): Array<out XmlAttributeDescriptor> =
      ((HtmlNSDescriptorImpl.guessTagForCommonAttributes(context) as? HtmlElementDescriptorImpl)
         ?.getDefaultAttributeDescriptors(context) ?: emptyArray())
  }

  override fun getDeclaration(): JSImplicitElement {
    return StreamEx.of(sources)
      .map {
        if (it is VueComponent)
          it.source?.let { source ->
            VueModelManager.getComponentImplicitElement(source)
            ?: JSImplicitElementImpl(tag.name, source)
          }
        else
          it
      }
      .select(JSImplicitElement::class.java)
      .findFirst()
      .orElseGet { JSImplicitElementImpl(tag.name, tag) }
  }

  override fun getName(context: PsiElement?): String = (context as? XmlTag)?.name ?: name
  override fun getName(): String = fromAsset(declaration.name)
  override fun init(element: PsiElement?) {}
  override fun getQualifiedName(): String = name
  override fun getDefaultName(): String = name

  override fun getElementsDescriptors(context: XmlTag): Array<XmlElementDescriptor> {
    return XmlDescriptorUtil.getElementsDescriptors(context)
  }

  override fun getElementDescriptor(childTag: XmlTag, contextTag: XmlTag): XmlElementDescriptor? {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
  }

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    val result = mutableListOf<XmlAttributeDescriptor>()
    val defaultHtmlAttributes = getDefaultHtmlAttributes(context)
    result.addAll(defaultHtmlAttributes)
    result.addAll(VueAttributesProvider.getDefaultVueAttributes())
    result.addAll(getProps())
    return result.toTypedArray()
  }

  fun getPsiSources(): List<PsiElement> {
    return sources.mapNotNull {
      when (it) {
        is VueComponent -> it.source
        is PsiElement -> it
        else -> null
      }
    }.ifEmpty { listOf(JSImplicitElementImpl(tag.name, tag)) }
  }

  fun getProps(): List<XmlAttributeDescriptor> {
    return StreamEx.of(sources)
      .select(VueContainer::class.java)
      .flatCollection {
        val result = mutableListOf<XmlAttributeDescriptor>()
        it.acceptSelfScope(object : VueModelVisitor() {
          override fun visitInputProperty(prop: VueInputProperty): Boolean {
            result.add(VueAttributeDescriptor(fromAsset(prop.name), prop.source))
            return true
          }
        })
        result
      }
      .toList()
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    attributeName ?: return null
    if (VueAttributesProvider.DEFAULT.contains(attributeName)) {
      return VueAttributeDescriptor(attributeName)
    }
    val extractedName = getBoundName(attributeName)
    val normalizedName = fromAsset(extractedName ?: attributeName)

    StreamEx.of(sources)
      .select(VueContainer::class.java)
      .map {
        var result: XmlAttributeDescriptor? = null
        it.acceptSelfScope(object : VueModelVisitor() {
          override fun visitInputProperty(prop: VueInputProperty): Boolean {
            if (normalizedName == fromAsset(prop.name)) {
              result = VueAttributeDescriptor(attributeName, prop.source)
              return false
            }
            return super.visitInputProperty(prop)
          }
        })
        result
      }
      .nonNull()
      .findFirst()
      .orElse(null)
      ?.let { return it }

    return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(extractedName ?: attributeName, context)
           // relax attributes check: https://vuejs.org/v2/guide/components.html#Non-Prop-Attributes
           // vue allows any non-declared as props attributes to be passed to a component
           ?: VueAttributeDescriptor(attributeName, isNonProp = extractedName == null)
  }

  override fun getAttributeDescriptor(attribute: XmlAttribute?): XmlAttributeDescriptor? = getAttributeDescriptor(attribute?.name,
                                                                                                                  attribute?.parent)

  override fun getNSDescriptor(): XmlNSDescriptor? = null
  override fun getTopGroup(): XmlElementsGroup? = null
  override fun getContentType(): Int = CONTENT_TYPE_ANY
  override fun getDefaultValue(): String? = null
}
