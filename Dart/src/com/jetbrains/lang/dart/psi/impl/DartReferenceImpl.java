package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.DartLookupElement;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartReferenceImpl extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartReferenceImpl(ASTNode node) {
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

    DartReference[] dartReferences = PsiTreeUtil.getChildrenOfType(this, DartReference.class);
    if (dartReferences != null && dartReferences.length > 0) {
      TextRange lastReferenceRange = dartReferences[dartReferences.length - 1].getTextRange();
      return new TextRange(
        lastReferenceRange.getStartOffset() - textRange.getStartOffset(),
        lastReferenceRange.getEndOffset() - textRange.getEndOffset()
      );
    }

    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
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
  public boolean isReferenceTo(PsiElement element) {
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
      return DartResolveUtil.resolveClassByType(dartClass == null ? null : dartClass.getSuperClass());
    }
    if (this instanceof DartNewExpression || this instanceof DartConstConstructorExpression) {
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
      DartReference[] children = PsiTreeUtil.getChildrenOfType(this, DartReference.class);
      if (children != null && children.length == 2) {
        return children[0].resolveDartClass();
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

  @NotNull
  @Override
  public Object[] getVariants() {
    final Set<DartComponentName> suggestedVariants = new THashSet<DartComponentName>();
    DartClass dartClass = null;
    // if do not contain references
    if (DartResolveUtil.aloneOrFirstInChain(this)) {
      final PsiElement context = this;
      final PsiScopeProcessor processor = new ComponentNameScopeProcessor(suggestedVariants);

      DartResolveUtil.treeWalkUpAndTopLevelDeclarations(context, processor);

      dartClass = PsiTreeUtil.getParentOfType(this, DartClass.class);
    }

    final DartReference leftReference = DartResolveUtil.getLeftReference(this);
    if (leftReference != null) {
      final DartClassResolveResult classResolveResult = leftReference.resolveDartClass();
      dartClass = classResolveResult.getDartClass();
      // prefix
      if (PsiTreeUtil.getParentOfType(leftReference.resolve(), DartImportStatement.class, DartExportStatement.class) != null) {
        final VirtualFile virtualFile = DartResolveUtil.getFileByPrefix(getContainingFile(), leftReference.getText());
        DartResolveUtil.processTopLevelDeclarations(this, new ComponentNameScopeProcessor(suggestedVariants), virtualFile, null);
      }
    }

    if (dartClass != null) {
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(dartClass.getFields()));
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(dartClass.getMethods()));
      suggestedVariants.addAll(DartResolveUtil.getComponentNames(ContainerUtil.filter(
        dartClass.getConstructors(),
        new Condition<DartComponent>() {
          @Override
          public boolean value(DartComponent component) {
            return component instanceof DartNamedConstructorDeclaration || component instanceof DartFactoryConstructorDeclaration;
          }
        })
      ));
    }

    final boolean typeInNew = getParent() instanceof DartType && getParent().getParent() instanceof DartNewExpression;

    if (typeInNew) {
      final Set<DartComponentName> constructors = new THashSet<DartComponentName>();
      for (DartComponentName componentName : suggestedVariants) {
        final PsiElement parent = componentName.getParent();
        if (!(parent instanceof DartClass)) continue;
        constructors.addAll(DartResolveUtil.getComponentNames(ContainerUtil.filter(
          ((DartClass)parent).getConstructors(),
          new Condition<DartComponent>() {
            @Override
            public boolean value(DartComponent component) {
              boolean namedOrFactory = component instanceof DartNamedConstructorDeclaration ||
                                       component instanceof DartFactoryConstructorDeclaration;
              return namedOrFactory && component.isPublic();
            }
          })
        ));
      }
      suggestedVariants.addAll(constructors);
    }

    return DartLookupElement.convert(suggestedVariants, typeInNew).toArray();
  }
}
