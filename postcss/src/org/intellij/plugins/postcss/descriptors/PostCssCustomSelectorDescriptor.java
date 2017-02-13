package org.intellij.plugins.postcss.descriptors;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.descriptor.BrowserVersion;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.descriptor.CssNavigableDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PostCssCustomSelectorDescriptor implements CssPseudoSelectorDescriptor, CssNavigableDescriptor {
  @NotNull private final PostCssCustomSelector mySelector;

  public PostCssCustomSelectorDescriptor(@NotNull final PostCssCustomSelector selector) {
    mySelector = selector;
  }

  @Nullable
  @Override
  public PsiElement getElement() {
    return mySelector;
  }

  @Override
  public boolean hasArguments() {
    return false;
  }

  @Override
  public int getColonPrefixLength() {
    return 1;
  }

  @Override
  public boolean isElementRequired() {
    return false;
  }

  @NotNull
  @Override
  public CssVersion getCssVersion() {
    return CssVersion.UNKNOWN;
  }

  @NotNull
  @Override
  public BrowserVersion[] getBrowsers() {
    return BrowserVersion.EMPTY_ARRAY;
  }

  @Override
  public boolean isAllowedInContextType(@NotNull CssContextType contextType) {
    return true;
  }

  @NotNull
  @Override
  public String getId() {
    return "--" + mySelector.getName();
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return ":--" + mySelector.getName();
  }

  @NotNull
  @Override
  public String getDescription() {
    return "";
  }

  @Nullable
  @Override
  public String getDocumentationString(@Nullable PsiElement context) {
    return null;
  }

  @NotNull
  @Override
  public String getElementTypeName() {
    return PostCssBundle.message("custom.selector");
  }

  @Nullable
  @Override
  public String getSpecificationUrl() {
    return null;
  }

  @NotNull
  @Override
  public CssContextType[] getAllowedContextTypes() {
    return CssContextType.EMPTY_ARRAY;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    final ItemPresentation presentation = ObjectUtils.notNull(mySelector.getPresentation());
    return presentation.getIcon(false);
  }
}
