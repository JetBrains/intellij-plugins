package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartReferenceImpl extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartReferenceImpl(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();

    DartReference[] dartReferences = PsiTreeUtil.getChildrenOfType(this, DartReference.class);
    if (dartReferences != null && dartReferences.length > 0) {
      TextRange lastReferenceRange = dartReferences[dartReferences.length - 1].getTextRange();
      return new UnfairTextRange(
        lastReferenceRange.getStartOffset() - textRange.getStartOffset(),
        lastReferenceRange.getEndOffset() - textRange.getEndOffset()
      );
    }

    return new UnfairTextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    PsiElement element = this;
    if (getText().indexOf('.') != -1) {
      // libPrefix.name
      final PsiElement lastChild = getLastChild();
      element = lastChild == null ? this : lastChild;
    }
    final DartId identifier = PsiTreeUtil.getChildOfType(element, DartId.class);
    final DartId identifierNew = DartElementGenerator.createIdentifierFromText(getProject(), newElementName);
    if (identifier != null && identifierNew != null) {
      element.getNode().replaceChild(identifier.getNode(), identifierNew.getNode());
    }
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return this;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(this, DartReference.class);
    final boolean chain = references != null && references.length == 2;
    if (chain) {
      return false;
    }
    final PsiElement target = resolve();
    if (element.getParent() instanceof DartClass &&
        target != null &&
        DartComponentType.typeOf(target.getParent()) == DartComponentType.CONSTRUCTOR) {
      return true;
    }
    return target == element;
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
    final List<? extends PsiElement> elements =
      ResolveCache.getInstance(getProject()).resolveWithCaching(this, DartResolver.INSTANCE, true, incompleteCode);
    return DartResolveUtil.toCandidateInfoArray(elements);
  }

  @NotNull
  @Override
  public DartClassResolveResult resolveDartClass() {
    if (this instanceof DartSuperExpression) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(this, DartClass.class);
      return dartClass == null ? DartClassResolveResult.EMPTY : dartClass.getSuperClassResolvedOrObjectClass();
    }

    if (this instanceof DartNewExpression) {
      final DartClassResolveResult result = DartResolveUtil.resolveClassByType(PsiTreeUtil.getChildOfType(this, DartType.class));
      result.specialize(this);
      return result;
    }

    if (this instanceof DartCallExpression) {
      final DartExpression expression = ((DartCallExpression)this).getExpression();
      final DartClassResolveResult leftResult = tryGetLeftResolveResult(expression);
      if (expression instanceof DartReference) {
        final DartClassResolveResult result =
          DartResolveUtil.getDartClassResolveResult(((DartReference)expression).resolve(), leftResult.getSpecialization());
        result.specialize(this);
        return result;
      }
    }

    if (this instanceof DartCascadeReferenceExpression) {
      PsiElement parent = this.getParent();
      if (parent instanceof DartValueExpression) {
        final List<DartExpression> expressionList = ((DartValueExpression)parent).getExpressionList();
        final DartExpression firstExpression = expressionList.isEmpty() ? null : expressionList.get(0);
        if (firstExpression instanceof DartReference) {
          return ((DartReference)firstExpression).resolveDartClass();
        }
      }
    }

    if (this instanceof DartAwaitExpression) {
      final DartExpression expression = ((DartAwaitExpression)this).getExpression();
      if (expression instanceof DartReference) {
        final DartClassResolveResult result = ((DartReference)expression).resolveDartClass();
        final DartClass resolvedClass = result.getDartClass();
        if (resolvedClass != null && "Future".equals(resolvedClass.getName())) {
          final DartClassResolveResult unwrappedFuture = result.getSpecialization().get(resolvedClass, "T");
          return unwrappedFuture == null ? DartClassResolveResult.EMPTY : unwrappedFuture;
        }
        else {
          return result;
        }
      }
    }

    return DartResolveUtil.getDartClassResolveResult(resolve(), tryGetLeftResolveResult(this).getSpecialization());
  }

  @NotNull
  private static DartClassResolveResult tryGetLeftResolveResult(DartExpression expression) {
    final DartReference[] childReferences = PsiTreeUtil.getChildrenOfType(expression, DartReference.class);
    final DartReference leftReference = childReferences != null ? childReferences[0] : null;
    return leftReference != null
           ? leftReference.resolveDartClass()
           : DartClassResolveResult.create(PsiTreeUtil.getParentOfType(expression, DartClass.class));
  }
}
