package org.intellij.plugins.postcss.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.CssTermList;
import com.intellij.psi.css.reference.CssReference;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssSimpleVariableReference extends PsiReferenceBase<PsiElement> implements CssReference {
  private static final ResolveCache.AbstractResolver<PostCssSimpleVariableReference, PostCssSimpleVariableDeclaration> RESOLVER =
    (reference, incompleteCode) -> {
      final String text = StringUtil.trimStart(reference.getValue(), "$");
      if (text.isEmpty()) return null;

      final Ref<PostCssSimpleVariableDeclaration> result = Ref.create();
      processSimpleVariableDeclarations(reference.getElement(), element -> {
        if (text.equals(element.getName())) {
          result.set(element);
          return false;
        }
        return true;
      });

      return result.get();
    };

  public PostCssSimpleVariableReference(@NotNull final PsiElement element) {
    super(element);
  }

  @Override
  protected TextRange calculateDefaultRangeInElement() {
    return TextRange.create(1, myElement.getTextLength()); // skip leading $
  }

  @NotNull
  @Override
  public String getUnresolvedMessagePattern() {
    return "Cannot find variable " + getCanonicalText();
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return ResolveCache.getInstance(myElement.getProject()).resolveWithCaching(this, RESOLVER, false, false);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final SmartList<LookupElement> result = new SmartList<>();
    processSimpleVariableDeclarations(myElement, element -> {
      LookupElementBuilder lookup = LookupElementBuilder.create(element).withIcon(AllIcons.Nodes.Variable);
      CssTermList initializer = element.getInitializer();
      if (initializer != null) {
        lookup = lookup.withTailText(" " + initializer.getText(), true);
      }
      result.add(lookup);
      return true;
    });

    return result.toArray();
  }

  private static void processSimpleVariableDeclarations(@NotNull PsiElement context,
                                                        @NotNull Processor<PostCssSimpleVariableDeclaration> processor) {
    CssRulesetList rulesetList = PsiTreeUtil.getParentOfType(context, CssRulesetList.class);
    if (rulesetList == null) return;

    PsiElement child = rulesetList.getLastChild();
    while (child != null) {
      if (child instanceof PostCssSimpleVariableDeclaration) {
        if (!processor.process((PostCssSimpleVariableDeclaration)child)) return;
      }
      child = child.getPrevSibling();
    }
  }
}
