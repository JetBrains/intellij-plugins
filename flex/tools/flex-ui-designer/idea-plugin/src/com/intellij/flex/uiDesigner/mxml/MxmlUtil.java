package com.intellij.flex.uiDesigner.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.*;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MxmlUtil {
  static THashSet<String> anchorStyles = new THashSet<String>();
  static {
    anchorStyles.add("baseline");
    anchorStyles.add("bottom");
    anchorStyles.add("horizontalCenter");
    anchorStyles.add("baseline");
    anchorStyles.add("baseline");
  }

  static Document getDocument(@NotNull PsiElement element) {
    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    assert virtualFile != null;
    return FileDocumentManager.getInstance().getDocument(virtualFile);
  }

  static int getLineNumber(XmlTag tag) {
    return getDocument(tag).getLineNumber(tag.getTextOffset()) + 1;
  }

  // about id http://opensource.adobe.com/wiki/display/flexsdk/id+property+in+MXML+2009
  static boolean isIdLanguageIdAttribute(XmlAttribute attribute) {
    String ns = attribute.getNamespace();
    return ns.length() == 0 || ns.equals(JavaScriptSupportLoader.MXML_URI3);
  }

  static boolean isComponentLanguageTag(XmlTag tag) {
      return tag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) && tag.getLocalName().equals("Component");
    }

  static boolean containsOnlyWhitespace(XmlTagChild child) {
    PsiElement firstChild = child.getFirstChild();
    return firstChild == child.getLastChild() && (firstChild == null || firstChild instanceof PsiWhiteSpace);
  }

  @Nullable
  public static XmlElement getInjectedHost(XmlTag tag) {
    // support <tag>{v}...</tag> or <tag>__PsiWhiteSpace__{v}...</tag>
    // <tag><span>ssss</span> {v}...</tag> is not supported
    for (XmlTagChild child : tag.getValue().getChildren()) {
      if (child instanceof XmlText) {
        return child;
      }
      else if (!(child instanceof PsiWhiteSpace)) {
        return null;
      }
    }

    return null;
  }
}
