package org.intellij.plugins.postcss.fileStructure;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.structure.CssStructureViewElement;
import com.intellij.psi.css.impl.structure.CssStructureViewElementsProvider;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssCustomMediaAtRule;
import org.intellij.plugins.postcss.psi.PostCssCustomSelectorAtRule;
import org.intellij.plugins.postcss.psi.PostCssNest;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class PostCssStructureViewElementsProvider extends CssStructureViewElementsProvider {
  private static final Class[] POST_CSS_SUITABLE_CLASSES = new Class[]{PostCssNest.class, PostCssCustomSelectorAtRule.class};

  @Override
  public boolean isMyContext(PsiElement element) {
    return PostCssLanguage.INSTANCE.is(CssPsiUtil.getStylesheetLanguage(element));
  }

  @Override
  public Class @NotNull [] getSuitableClasses() {
    return POST_CSS_SUITABLE_CLASSES;
  }

  @Override
  public boolean hasInnerStructure(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public @NotNull Collection<? extends CssStructureViewElement> createStructureViewElements(@NotNull PsiElement element) {
    if (element.getLanguage() != PostCssLanguage.INSTANCE || shouldSkipElement(element)) {
      return Collections.emptyList();
    }

    if (element instanceof PostCssNest) {
      return Collections.singletonList(CssStructureViewElement.create(element, AllIcons.Nodes.Annotationtype, "@nest"));
    }
    else if (element instanceof PostCssCustomSelectorAtRule) {
      return Collections.singletonList(CssStructureViewElement.create(element, AllIcons.Nodes.Annotationtype, "@custom-selector"));
    }
    else if (element instanceof PostCssCustomMediaAtRule) {
      return Collections.singletonList(CssStructureViewElement.create(element, AllIcons.Nodes.Annotationtype, "@custom-media"));
    }
    return Collections.emptyList();
  }
}