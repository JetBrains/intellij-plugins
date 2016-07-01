package org.intellij.plugins.postcss.fileStructure;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.structure.CssStructureViewElement;
import com.intellij.psi.css.impl.structure.CssStructureViewElementsProvider;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssNest;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

import static com.intellij.util.containers.ContainerUtilRt.newArrayList;

public class PostCssStructureViewElementsProvider extends CssStructureViewElementsProvider {
  private static final Class[] POST_CSS_SUITABLE_CLASSES = new Class[]{PostCssNest.class};

  @Override
  public boolean isMyContext(PsiElement element) {
    return PostCssLanguage.INSTANCE.is(CssPsiUtil.getStylesheetLanguage(element));
  }

  @NotNull
  @Override
  public Class[] getSuitableClasses() {
    return POST_CSS_SUITABLE_CLASSES;
  }

  @Override
  public boolean hasInnerStructure(@NotNull PsiElement element) {
    return false;
  }

  @NotNull
  @Override
  public Collection<? extends CssStructureViewElement> createStructureViewElements(@NotNull PsiElement element) {
    if (element.getLanguage() != PostCssLanguage.INSTANCE || shouldSkipElement(element)) {
      return Collections.emptyList();
    }

    if (element instanceof PostCssNest) {
      return newArrayList(CssStructureViewElement.create(element, AllIcons.Css.Atrule, "@nest"));
    }
    return Collections.emptyList();
  }
}