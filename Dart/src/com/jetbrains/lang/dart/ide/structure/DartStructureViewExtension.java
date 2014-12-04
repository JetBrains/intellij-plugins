package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.structureView.StructureViewExtension;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import org.jetbrains.annotations.Nullable;

public class DartStructureViewExtension implements StructureViewExtension {
  @Override
  public Class<? extends PsiElement> getType() {
    return XmlTag.class;
  }

  @Override
  @Nullable
  public StructureViewTreeElement[] getChildren(final PsiElement parent) {
    final DartEmbeddedContent dartContent = getDartEmbeddedContent((XmlTag)parent);
    return dartContent != null ? new DartStructureViewElement(dartContent).getChildren() : null;
  }

  @Override
  public Object getCurrentEditorElement(final Editor editor, final PsiElement parent) {
    final DartEmbeddedContent dartContent = getDartEmbeddedContent((XmlTag)parent);
    return dartContent != null ? new DartStructureViewModel(parent.getContainingFile(), editor).getCurrentEditorElement() : null;
  }

  private static DartEmbeddedContent getDartEmbeddedContent(final XmlTag xmlTag) {
    return HtmlUtil.isScriptTag(xmlTag) && DartLanguage.DART_MIME_TYPE.equals(xmlTag.getAttributeValue("type"))
           ? PsiTreeUtil.getChildOfType(xmlTag, DartEmbeddedContent.class)
           : null;
  }
}
