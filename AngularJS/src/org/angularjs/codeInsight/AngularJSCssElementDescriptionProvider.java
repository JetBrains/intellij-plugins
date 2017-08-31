package org.angularjs.codeInsight;

import com.intellij.lang.css.CssDialect;
import com.intellij.lang.css.CssDialectMappings;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.util.HtmlUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AngularJSCssElementDescriptionProvider extends CssElementDescriptorProvider {
  private static final String NG_DEEP = "ng-deep";
  private static final Set<CssPseudoSelectorDescriptorStub> PSEUDO_SELECTORS =
    Collections.singleton(new CssPseudoSelectorDescriptorStub(NG_DEEP, true));

  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    if (context == null || !context.isValid()) return false;
    final PsiFile file = context.getContainingFile();
    if (file == null) return false;
    final Project project = context.getProject();
    if (HtmlUtil.hasHtml(file)) return AngularIndexUtil.hasAngularJS(project);
    final VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
    final CssDialect mapping = CssDialectMappings.getInstance(project).getMapping(virtualFile);
    return (mapping == null || mapping == CssDialect.CLASSIC) && AngularIndexUtil.hasAngularJS(project);
  }

  @Override
  public boolean isPossibleSelector(@NotNull final String selector, @NotNull PsiElement context) {
    return DirectiveUtil.getTagDirective(DirectiveUtil.normalizeAttributeName(selector), context.getProject()) != null;
  }

  @NotNull
  @Override
  public Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name,
                                                                                         @Nullable PsiElement context) {
    if (context == null || !NG_DEEP.equals(name) || !AngularIndexUtil.hasAngularJS2(context.getProject())) {
      return Collections.emptySet();
    }
    return PSEUDO_SELECTORS;
  }

  @NotNull
  @Override
  public Collection<? extends CssPseudoSelectorDescriptor> getAllPseudoSelectorDescriptors(@Nullable PsiElement context) {
    return context != null && AngularIndexUtil.hasAngularJS2(context.getProject()) ? PSEUDO_SELECTORS : Collections.emptySet();
  }

  @NotNull
  @Override
  public String[] getSimpleSelectors(@NotNull PsiElement context) {
    final List<String> result = new LinkedList<>();
    DirectiveUtil.processTagDirectives(context.getProject(), proxy -> {
      result.add(proxy.getName());
      return true;
    });
    return ArrayUtil.toStringArray(result);
  }

  @NotNull
  @Override
  public PsiElement[] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    final JSImplicitElement directive =
      DirectiveUtil.getTagDirective(DirectiveUtil.normalizeAttributeName(selector.getElementName()), selector.getProject());
    return directive != null ? new PsiElement[]{directive} : PsiElement.EMPTY_ARRAY;
  }
}
