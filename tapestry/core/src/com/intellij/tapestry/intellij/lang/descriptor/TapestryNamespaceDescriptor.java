package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.SchemaNSDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TapestryNamespaceDescriptor extends SchemaNSDescriptor {
  private XmlFile myFile;
  private XmlElement myElement;

  public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
    return DescriptorUtil.getTmlOrHtmlTagDescriptor(tag);
  }

  @NotNull
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable XmlDocument doc) {
    if (doc == null) return XmlElementDescriptor.EMPTY_ARRAY;
    XmlTag rootTag = doc.getRootTag();
    if (rootTag == null) return XmlElementDescriptor.EMPTY_ARRAY;
    return DescriptorUtil.getTmlSubelementDescriptors(rootTag, this);
  }

  @Nullable
  public XmlFile getDescriptorFile() {
    return myFile;
  }

  public PsiElement getDeclaration() {
    return myElement;
  }

  public String getName(PsiElement context) {
    return getName();
  }

  public String getName() {
    return myFile.getName();
  }

  public void init(PsiElement element) {
    super.init(element);
    myFile = (XmlFile)element.getContainingFile();
    myElement = (XmlElement)element;

    if (element instanceof XmlDocument) {
      myElement = ((XmlDocument)element).getRootTag();
    }
  }

  @NotNull
  public Object[] getDependencies() {
    return TapestryProject.JAVA_STRUCTURE_DEPENDENCY;
  }

  public XmlElementDescriptor[] getSuperRootElementsDescriptors(XmlDocument document) {
    return super.getRootElementsDescriptors(document);
  }
}
