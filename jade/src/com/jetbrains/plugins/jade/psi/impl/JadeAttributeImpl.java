package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JadeAttributeImpl extends XmlAttributeImpl {

  private @Nullable String myPatchedName = null;

  private static int ourCurrentFakeId = 0;

  private static int getFakeClassId() {
    if (ourCurrentFakeId > 1e9) {
      ourCurrentFakeId = 0;
    }
    return ourCurrentFakeId++;
  }

  private @NotNull String patchName(@NotNull String name) {
    if (!StringUtil.isEmpty(name)) {
      return name;
    }

    final PsiElement lastChild = getLastChild();
    if (lastChild instanceof JadeAttributeValueImpl) {
      final PsiElement element = lastChild.getFirstChild();
      if (element == null) {
        return name;
      }

      if (myPatchedName != null) {
        return myPatchedName;
      }
      final ASTNode node = element.getNode();
      if (node != null) {
        if (node.getElementType() == JadeElementTypes.CLASS) {
          return myPatchedName = HtmlUtil.CLASS_ATTRIBUTE_NAME + getFakeClassId();
        }
        else if (node.getElementType() == JadeTokenTypes.TAG_ID) {
          return myPatchedName = HtmlUtil.ID_ATTRIBUTE_NAME;
        }
      }
    }

    return name;
  }

  @Override
  public @Nullable JadeAttributeValueImpl getValueElement() {
    return PsiTreeUtil.findChildOfType(this, JadeAttributeValueImpl.class);
  }


  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }


  @Override
  public @NotNull String getLocalName() {
    final String result = patchName(super.getLocalName());
    if (myPatchedName != null && myPatchedName.startsWith(HtmlUtil.CLASS_ATTRIBUTE_NAME)) {
      return HtmlUtil.CLASS_ATTRIBUTE_NAME;
    }
    return result;
  }

  @Override
  public @NotNull String getName() {
    return patchName(super.getName());
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }
}
