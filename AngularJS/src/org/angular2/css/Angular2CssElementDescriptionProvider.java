// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import com.intellij.util.ArrayUtilRt;
import com.intellij.xml.XmlElementDescriptor;
import org.angular2.codeInsight.refs.Angular2SelectorReferencesProvider;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Angular2CssElementDescriptionProvider extends CssElementDescriptorProvider {
  @NonNls private static final String NG_DEEP = "ng-deep";
  private static final Set<CssPseudoSelectorDescriptorStub> PSEUDO_SELECTORS =
    Collections.singleton(new CssPseudoSelectorDescriptorStub(NG_DEEP, true));

  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    return context != null && Angular2LangUtil.isAngular2Context(context);
  }

  @Override
  public boolean isPossibleSelector(final @NotNull String selector, @NotNull PsiElement context) {
    return !Angular2EntitiesProvider.findElementDirectivesCandidates(context.getProject(), selector).isEmpty();
  }

  @Override
  public @NotNull Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name,
                                                                                                  @Nullable PsiElement context) {
    if (context != null && NG_DEEP.equals(name) && Angular2LangUtil.isAngular2Context(context)) {
      return PSEUDO_SELECTORS;
    }
    return Collections.emptySet();
  }

  @Override
  public @NotNull Collection<? extends CssPseudoSelectorDescriptor> getAllPseudoSelectorDescriptors(@Nullable PsiElement context) {
    return context != null && Angular2LangUtil.isAngular2Context(context) ? PSEUDO_SELECTORS : Collections.emptySet();
  }

  @Override
  public String @NotNull [] getSimpleSelectors(@NotNull PsiElement context) {
    return ArrayUtilRt.toStringArray(Angular2EntitiesProvider.getAllElementDirectives(context.getProject())
                                       .keySet());
  }

  @Override
  public PsiElement @NotNull [] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    XmlElementDescriptor descriptor = Angular2SelectorReferencesProvider.getElementDescriptor(
      selector.getElementName(), selector.getContainingFile());
    if (descriptor != null) {
      return new PsiElement[]{descriptor.getDeclaration()};
    }
    String elementName = selector.getElementName();
    return Angular2EntitiesProvider.findElementDirectivesCandidates(
      selector.getProject(), elementName)
      .stream()
      .map(dir -> dir.getSelector().getSimpleSelectorsWithPsi())
      .flatMap(Collection::stream)
      .map(sel -> sel.getElement())
      .filter(Objects::nonNull)
      .filter(el -> elementName.equals(el.getName()))
      .toArray(PsiElement[]::new);
  }
}
