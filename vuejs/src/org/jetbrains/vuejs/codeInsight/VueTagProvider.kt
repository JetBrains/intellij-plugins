package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDocumentImpl
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ArrayUtil
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import com.intellij.xml.XmlTagNameProvider
import icons.VuejsIcons
import org.jetbrains.vuejs.VueFileType

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null) {
      val file = FilenameIndex.getFilesByName(tag.project, toAsset(tag.name) + ".vue", tag.resolveScope).firstOrNull() ?:
                 FilenameIndex.getFilesByName(tag.project, toAsset(tag.name).capitalize() + ".vue", tag.resolveScope).firstOrNull()
      if (file != null) {
        return VueElementDescriptor(file)
      }
    }
    return null
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
    elements?.addAll(FileTypeIndex.getFiles(VueFileType.INSTANCE, tag.resolveScope).map { createVueLookup(it) })
  }

  private fun createVueLookup(file: VirtualFile) =
    LookupElementBuilder.create(fromAsset(file.nameWithoutExtension)).
      withInsertHandler(XmlTagInsertHandler.INSTANCE).
      withIcon(VuejsIcons.Vue)
}


class VueElementDescriptor(val file: PsiFile) : XmlElementDescriptor {
  override fun getDeclaration() = file
  override fun getName(context: PsiElement?):String = name
  override fun getName() = fromAsset(file.virtualFile.nameWithoutExtension)
  override fun init(element: PsiElement?) {}
  override fun getQualifiedName() = name
  override fun getDefaultName() = name

  override fun getElementsDescriptors(context: XmlTag?): Array<out XmlElementDescriptor> {
    val xmlDocument = PsiTreeUtil.getParentOfType(context, XmlDocumentImpl::class.java) ?: return XmlElementDescriptor.EMPTY_ARRAY
    return xmlDocument.rootTagNSDescriptor.getRootElementsDescriptors(xmlDocument)
  }

  override fun getElementDescriptor(childTag: XmlTag?, contextTag: XmlTag?): XmlElementDescriptor? {
    val parent = contextTag?.parentTag ?: return null
    val descriptor = parent.getNSDescriptor(childTag?.namespace, true)
    return descriptor?.getElementDescriptor(childTag!!)
  }

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)!!
  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?) = getAttributesDescriptors(context).find { it.name == attributeName } ?:
                                                                                  VueAttributesProvider.vueAttributeDescriptor(attributeName)
  override fun getAttributeDescriptor(attribute: XmlAttribute?) = getAttributeDescriptor(attribute?.name, attribute?.parent)

  override fun getNSDescriptor() = null
  override fun getTopGroup() = null
  override fun getContentType() = CONTENT_TYPE_ANY
  override fun getDefaultValue() = null
  override fun getDependences(): Array<out Any> = ArrayUtil.EMPTY_OBJECT_ARRAY!!
}
