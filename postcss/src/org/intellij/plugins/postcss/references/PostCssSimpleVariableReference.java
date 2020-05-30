package org.intellij.plugins.postcss.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.CssStylesheet;
import com.intellij.psi.css.CssTermList;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.css.reference.CssReference;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PostCssSimpleVariableReference extends PsiReferenceBase<PsiElement> implements CssReference {
  private static final ResolveCache.AbstractResolver<PostCssSimpleVariableReference, PostCssSimpleVariableDeclaration> RESOLVER =
    (reference, incompleteCode) -> {
      String varName = reference.getValue();
      if (varName.isEmpty()) return null;

      final Ref<PostCssSimpleVariableDeclaration> result = Ref.create();
      processSimpleVariableDeclarations(reference.getElement(), element -> {
        if (varName.equals(element.getName())) {
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
    String text = myElement.getText();
    if (text.startsWith("$(")) {
      if (text.endsWith(")")) {
        return TextRange.create(2, myElement.getTextLength() - 1); // interpolation :$(foo)
      }
      else {
        return TextRange.create(2, myElement.getTextLength()); // incomplete interpolation :$(foo
      }
    }
    return TextRange.create(1, myElement.getTextLength()); // skip leading $ in $foo
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

  @Override
  public Object @NotNull [] getVariants() {
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

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    String text = myElement.getText();
    String newText;
    if (text.startsWith("$(")) {
      newText = "$(" + newElementName + ")";
    }
    else {
      newText = "$" + newElementName;
    }
    PsiFile file = PsiFileFactory.getInstance(myElement.getProject()).createFileFromText(PostCssLanguage.INSTANCE, newText);
    PsiElement oldVarToken = myElement.getFirstChild();
    PsiElement newVarToken = PsiTreeUtil.getDeepestFirst(file);
    if (oldVarToken != null &&
        oldVarToken.getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN &&
        newVarToken.getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN) {
      oldVarToken.replace(newVarToken);
    }
    return myElement;
  }

  private static void processSimpleVariableDeclarations(@NotNull PsiElement context,
                                                        @NotNull Processor<PostCssSimpleVariableDeclaration> processor) {
    CssRulesetList rulesetList = PsiTreeUtil.getParentOfType(context, CssRulesetList.class);
    if (rulesetList == null) return;

    if (!processSimpleVarsInRulesetList(rulesetList, processor)) return;

    PsiFile contextFile = context.getContainingFile();
    Set<VirtualFile> otherFiles = CssUtil.getImportedFiles(context.getContainingFile(), context, true);
    for (VirtualFile otherFile : otherFiles) {
      PsiFile otherPsiFile = contextFile.getManager().findFile(otherFile);
      if (otherPsiFile instanceof StylesheetFile && !otherFile.equals(contextFile.getVirtualFile())) {
        CssStylesheet otherStylesheet = ((StylesheetFile)otherPsiFile).getStylesheet();
        CssRulesetList otherRulesetList = otherStylesheet == null ? null : otherStylesheet.getRulesetList();
        if (otherRulesetList != null) {
          if (!processSimpleVarsInRulesetList(otherRulesetList, processor)) return;
        }
      }
    }
  }

  private static boolean processSimpleVarsInRulesetList(@NotNull CssRulesetList rulesetList,
                                                        @NotNull Processor<PostCssSimpleVariableDeclaration> processor) {
    PsiElement child = rulesetList.getLastChild();
    while (child != null) {
      if (child instanceof PostCssSimpleVariableDeclaration) {
        if (!processor.process((PostCssSimpleVariableDeclaration)child)) return false;
      }
      child = child.getPrevSibling();
    }
    return true;
  }
}
