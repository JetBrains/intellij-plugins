package org.intellij.plugins.postcss.descriptors;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.descriptor.BrowserVersion;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.descriptor.CssNavigableDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class PostCssCustomSelectorDescriptor implements CssPseudoSelectorDescriptor, CssNavigableDescriptor {
  private final @NotNull PostCssCustomSelector mySelector;

  public PostCssCustomSelectorDescriptor(final @NotNull PostCssCustomSelector selector) {
    mySelector = selector;
  }

  @Override
  public @Nullable PsiElement getElement() {
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

  @Override
  public @NotNull CssVersion getCssVersion() {
    return CssVersion.UNKNOWN;
  }

  @Override
  public BrowserVersion @NotNull [] getBrowsers() {
    return BrowserVersion.EMPTY_ARRAY;
  }

  @Override
  public boolean isAllowedInContextType(@NotNull CssContextType contextType) {
    return true;
  }

  @Override
  public @NotNull String getId() {
    return "--" + mySelector.getName();
  }

  @Override
  public @NotNull String getPresentableName() {
    return ":--" + mySelector.getName();
  }

  @Override
  public @NotNull String getDescription() {
    return "";
  }

  @Override
  public @Nullable String getDocumentationString(@Nullable PsiElement context) {
    return null;
  }

  @Override
  public @NotNull String getElementTypeName() {
    return PostCssBundle.message("custom.selector.label");
  }

  @Override
  public @Nullable String getSpecificationUrl() {
    return null;
  }

  @Override
  public CssContextType @NotNull [] getAllowedContextTypes() {
    return CssContextType.EMPTY_ARRAY;
  }

  @Override
  public @Nullable Icon getIcon() {
    final ItemPresentation presentation = Objects.requireNonNull(mySelector.getPresentation());
    return presentation.getIcon(false);
  }
}
