// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.codeInsight;

import com.intellij.lang.css.CssDialectMappings;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.xml.util.HtmlUtil;
import one.util.streamex.StreamEx;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ArrayUtilRt.EMPTY_STRING_ARRAY;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angularjs.index.AngularJSDirectivesSupport.findTagDirectives;

final class AngularJSCssElementDescriptionProvider extends CssElementDescriptorProvider {
  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    if (context == null || !context.isValid()) return false;
    final PsiFile file = context.getContainingFile();
    if (file == null) return false;
    final Project project = context.getProject();
    if (HtmlUtil.hasHtml(file)) return AngularIndexUtil.hasAngularJS(project);
    final VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
    return !CssDialectMappings.getInstance(project).hasCustomDialect(virtualFile) && AngularIndexUtil.hasAngularJS(project);
  }

  @Override
  public boolean isPossibleSelector(final @NotNull String selector, @NotNull PsiElement context) {
    return DirectiveUtil.getTagDirective(DirectiveUtil.normalizeAttributeName(selector), context.getProject()) != null;
  }

  @Override
  public String @NotNull [] getSimpleSelectors(@NotNull PsiElement context) {
    return StreamEx.of(findTagDirectives(context.getProject(), null))
      .map(directive -> doIfNotNull(directive.getName(), DirectiveUtil::getAttributeName))
      .nonNull()
      .toArray(EMPTY_STRING_ARRAY);
  }

  @Override
  public PsiElement @NotNull [] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    final JSImplicitElement directive =
      DirectiveUtil.getTagDirective(DirectiveUtil.normalizeAttributeName(selector.getElementName()), selector.getProject());
    return directive != null ? new PsiElement[]{directive} : PsiElement.EMPTY_ARRAY;
  }
}
