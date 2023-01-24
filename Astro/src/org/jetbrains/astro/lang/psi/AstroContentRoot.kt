// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.javaee.ExternalResourceManager
import com.intellij.javaee.ExternalResourceManagerEx
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.impl.PsiCachedValueImpl
import com.intellij.psi.impl.meta.MetaRegistry
import com.intellij.psi.meta.PsiMetaData
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlProlog
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlNSDescriptor
import com.intellij.xml.impl.XmlNsDescriptorUtil
import org.jetbrains.astro.lang.AstroLanguage
import java.util.concurrent.ConcurrentHashMap

class AstroContentRoot : JSEmbeddedContentImpl, XmlDocument {
  constructor(node: ASTNode) : super(node)

  constructor(stub: JSEmbeddedContentStub, type: IStubElementType<*, *>) : super(stub, type)

  override fun getLanguage(): Language {
    return AstroLanguage.INSTANCE
  }

  // Copied over from XmlDocument
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is XmlElementVisitor) {
      visitor.visitXmlDocument(this)
    }
    else {
      visitor.visitElement(this)
    }
  }

  override fun getMetaData(): PsiMetaData? =
    MetaRegistry.getMeta(this)

  override fun getProlog(): XmlProlog? =
    childrenOfType<XmlProlog>().firstOrNull()

  override fun getRootTag(): XmlTag? =
    childrenOfType<XmlTag>().firstOrNull()

  override fun getRootTagNSDescriptor(): XmlNSDescriptor? {
    val rootTag = rootTag
    return rootTag?.getNSDescriptor(rootTag.namespace, false)
  }

  private val defaultDescriptorsCacheStrict = ConcurrentHashMap<String, CachedValue<XmlNSDescriptor?>>()
  private val defaultDescriptorsCacheNotStrict = ConcurrentHashMap<String, CachedValue<XmlNSDescriptor?>>()

  @Volatile
  private var myExtResourcesModCount: Long = -1

  override fun clearCaches() {
    defaultDescriptorsCacheStrict.clear()
    defaultDescriptorsCacheNotStrict.clear()
    super.clearCaches()
  }

  override fun getDefaultNSDescriptor(namespace: String, strict: Boolean): XmlNSDescriptor? {
    val curExtResourcesModCount = ExternalResourceManagerEx.getInstanceEx().getModificationCount(project)
    if (myExtResourcesModCount != curExtResourcesModCount) {
      defaultDescriptorsCacheNotStrict.clear()
      defaultDescriptorsCacheStrict.clear()
      myExtResourcesModCount = curExtResourcesModCount
    }
    val defaultDescriptorsCache = if (strict) defaultDescriptorsCacheStrict else defaultDescriptorsCacheNotStrict
    var cachedValue = defaultDescriptorsCache[namespace]
    if (cachedValue == null) {
      defaultDescriptorsCache[namespace] = PsiCachedValueImpl(manager, CachedValueProvider {
        val defaultNSDescriptorInner = XmlNsDescriptorUtil.getDefaultNSDescriptor(this, namespace, strict)
        if (XmlNsDescriptorUtil.isGeneratedFromDtd(this, defaultNSDescriptorInner)) {
          CachedValueProvider.Result(defaultNSDescriptorInner, this,
                                     ExternalResourceManager.getInstance())
        }
        else
          CachedValueProvider.Result(defaultNSDescriptorInner,
                                     defaultNSDescriptorInner?.dependencies ?: ExternalResourceManager.getInstance())
      }).also { cachedValue = it }
    }
    return cachedValue!!.value
  }

  override fun clone(): PsiElement {
    val cacheStrict = HashMap<String, CachedValue<XmlNSDescriptor?>>(defaultDescriptorsCacheStrict)
    val cacheNotStrict = HashMap<String, CachedValue<XmlNSDescriptor?>>(defaultDescriptorsCacheNotStrict)
    val copy = super.clone() as AstroContentRoot
    updateSelfDependentDtdDescriptors(copy, cacheStrict, cacheNotStrict)
    return copy
  }

  override fun copy(): PsiElement {
    val cacheStrict = HashMap<String, CachedValue<XmlNSDescriptor?>>(defaultDescriptorsCacheStrict)
    val cacheNotStrict = HashMap<String, CachedValue<XmlNSDescriptor?>>(defaultDescriptorsCacheNotStrict)
    val copy = super.copy() as AstroContentRoot
    updateSelfDependentDtdDescriptors(copy, cacheStrict, cacheNotStrict)
    return copy
  }

  private fun updateSelfDependentDtdDescriptors(copy: AstroContentRoot,
                                                cacheStrict: HashMap<String, CachedValue<XmlNSDescriptor?>>,
                                                cacheNotStrict: HashMap<String, CachedValue<XmlNSDescriptor?>>) {
    copy.defaultDescriptorsCacheNotStrict.clear()
    copy.defaultDescriptorsCacheStrict.clear()
    for ((key, value) in cacheStrict) {
      if (value.hasUpToDateValue()) {
        val nsDescriptor = value.value
        if (!XmlNsDescriptorUtil.isGeneratedFromDtd(this, nsDescriptor)) copy.defaultDescriptorsCacheStrict[key] = value
      }
    }
    for ((key, value) in cacheNotStrict) {
      if (value.hasUpToDateValue()) {
        val nsDescriptor = value.value
        if (!XmlNsDescriptorUtil.isGeneratedFromDtd(this, nsDescriptor)) copy.defaultDescriptorsCacheNotStrict[key] = value
      }
    }
  }


}