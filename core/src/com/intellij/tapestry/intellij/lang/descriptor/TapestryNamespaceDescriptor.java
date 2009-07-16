package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jul 1, 2009
 *         Time: 3:43:26 PM
 */
public class TapestryNamespaceDescriptor implements XmlNSDescriptor {
  public static final TapestryNamespaceDescriptor INSTANCE = new TapestryNamespaceDescriptor();
  private XmlFile myFile;
  private XmlElement myElement;

  private TapestryNamespaceDescriptor() {
  }

  public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
    return DescriptorUtil.getTmlOrHtmlTagDescriptor(tag);
  }

  @NotNull
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable XmlDocument doc) {
    if (doc == null) return XmlElementDescriptor.EMPTY_ARRAY;
    XmlTag rootTag = doc.getRootTag();
    if (rootTag == null) return XmlElementDescriptor.EMPTY_ARRAY;
    return DescriptorUtil.getTmlSubelementDescriptors(rootTag);
  }

  @Nullable
  public XmlFile getDescriptorFile() {
    return myFile;
  }

  public boolean isHierarhyEnabled() {
    return false;
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
    myFile = (XmlFile)element.getContainingFile();
    myElement = (XmlElement)element;

    if (element instanceof XmlDocument) {
      myElement = ((XmlDocument)element).getRootTag();
    }
  }

  public Object[] getDependences() {
    return TapestryProject.JAVA_STRUCTURE_DEPENDENCY;
  }
}
