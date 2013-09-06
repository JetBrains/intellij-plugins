package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.DartLibraryStatement;
import com.jetbrains.lang.dart.psi.DartQualifiedComponentName;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartLibraryReferenceImpl extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartLibraryReferenceImpl(ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final DartQualifiedComponentName identifierNew = DartElementGenerator.createQIdentifierFromText(getProject(), newElementName);
    if (identifierNew != null) {
      getNode().replaceAllChildrenToChildrenOf(identifierNew.getNode());
    }
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return this;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return resolve() == element;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);

    return resolveResults.length == 0 ||
           resolveResults.length > 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return tryResolveLibraries();
  }

  @NotNull
  @Override
  public DartClassResolveResult resolveDartClass() {
    return DartClassResolveResult.EMPTY;
  }

  private ResolveResult[] tryResolveLibraries() {
    final String libraryName = DartResolveUtil.normalizeLibraryName(StringUtil.unquoteString(getText()));
    final List<VirtualFile> virtualFiles = DartLibraryIndex.findLibraryClass(this, libraryName);
    final List<PsiElementResolveResult> result = new ArrayList<PsiElementResolveResult>();
    for (VirtualFile virtualFile : virtualFiles) {
      final PsiFile psiFile = getManager().findFile(virtualFile);
      for (PsiElement root : DartResolveUtil.findDartRoots(psiFile)) {
        DartLibraryStatement lib = PsiTreeUtil.getChildOfType(root, DartLibraryStatement.class);
        if (lib == null) {
          continue;
        }
        DartQualifiedComponentName componentName = lib.getQualifiedComponentName();
        if (componentName != null) {
          result.add(new PsiElementResolveResult(componentName));
        }
      }
    }
    return result.toArray(new ResolveResult[result.size()]);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    // handled by DartLibraryNameCompletionContributor
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}
