// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.UI.CfmlLookUpItemUtil;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.IconManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlReferenceExpression extends AbstractQualifiedReference<CfmlReferenceExpression>
  implements CfmlReference, CfmlExpression, CfmlTypedElement {
  private static final Logger LOG = Logger.getInstance(CfmlReferenceExpression.class.getName());

  public CfmlReferenceExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected boolean processUnqualifiedVariants(PsiScopeProcessor processor) {
    PsiFile containingFile = getContainingFile();
    if (containingFile instanceof CfmlFile) {
      CfmlComponent componentDefinition = ((CfmlFile)containingFile).getComponentDefinition();

      // process functions
      // functions should be processed !first! as explicitly defined getter overlaps the implicit one
      CfmlFunction[] functions;
      if (componentDefinition != null) {
        functions = componentDefinition.getFunctionsWithSupers(this.getFirstChild() instanceof CfmlSuperComponentReference);
      }
      else {
        PsiFile file = this.getContainingFile();
        if (file instanceof CfmlFile) {
          functions = ((CfmlFile)file).getGlobalFunctions().toArray(CfmlFunction.EMPTY_ARRAY);
        }
        else {
          functions = CfmlFunction.EMPTY_ARRAY;
        }
      }
      for (CfmlFunction function : functions) {
        if (!processor.execute(function, ResolveState.initial())) {
          return false;
        }
      }
      // process properties
      if (componentDefinition != null) {
        CfmlProperty[] propertiesWithSupers =
          componentDefinition.getPropertiesWithSupers(this.getFirstChild() instanceof CfmlSuperComponentReference);
        for (CfmlProperty property : propertiesWithSupers) {
          if (!processor.execute(property, ResolveState.initial())) {
            return false;
          }
        }
      }
    }
    return super.processUnqualifiedVariants(processor);
  }

  @Override
  protected boolean processVariantsInner(PsiScopeProcessor processor) {
    CfmlTypedElement typedOwner = CfmlPsiUtil.getTypedQualifierInner(this);
    PsiType type = null;
    if (typedOwner != null) {
      type = typedOwner.getPsiType();
    }
    else {
      return processUnqualifiedVariants(processor);
    }
    // CfmlReferenceExpression qualifier = CfmlPsiUtil.getQualifierInner(this);
    if (type instanceof PsiClassType) {
      PsiClass psiClass;
      if (type instanceof CfmlFunctionCallExpression.PsiClassStaticType) {
        psiClass = PsiUtil.resolveClassInType(((CfmlFunctionCallExpression.PsiClassStaticType)type).getRawType());
        processor.handleEvent(CfmlVariantsProcessor.CfmlProcessorEvent.START_STATIC, null);
      }
      else {
        psiClass = PsiUtil.resolveClassInType(type);
      }
      processor.handleEvent(CfmlVariantsProcessor.CfmlProcessorEvent.SET_INITIAL_CLASS, psiClass);
      if (psiClass != null && !psiClass.processDeclarations(processor, ResolveState.initial(), null, this)) {
        return false;
      }
    }
    else if (type instanceof CfmlComponentType) {
      Collection<CfmlComponent> components = ((CfmlComponentType)type).resolve();

      for (CfmlComponent component : components) {
        if (!component.processDeclarations(processor, ResolveState.initial(), null, this)) {
          return false;
        }
      }
    }
    if (typedOwner instanceof CfmlFunctionCallExpression) {
      typedOwner = ((CfmlFunctionCallExpression)typedOwner).getReferenceExpression();
    }
    if (typedOwner instanceof CfmlReference) {
      final PsiElement psiElement = ((CfmlReference)typedOwner).resolve();
      return psiElement == null || psiElement.processDeclarations(processor, ResolveState.initial(), null, this);
    }
    return true;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    if (element instanceof CfmlProperty && element.getContainingFile() == getContainingFile()) {
      String name = ((CfmlProperty)element).getName();
      String referenceText = getText();

      if ((StringUtil.toLowerCase(referenceText).startsWith("get") ||
           StringUtil.toLowerCase(referenceText).startsWith("set")) &&
          referenceText.substring(3).equalsIgnoreCase(name)) {
        return true;
      }
      else {
        return false;
      }
    }

    final PsiManager manager = getManager();
    for (final ResolveResult result : multiResolve(false)) {
      final PsiElement target = result.getElement();
      if (manager.areElementsEquivalent(element, target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected ResolveResult @NotNull [] resolveInner() {
    final String referenceName = getReferenceName();
    if (referenceName == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final CfmlVariantsProcessor<ResolveResult> processor = new CfmlVariantsProcessor<>(this, getParent(), referenceName) {
      //Map<String, PsiNamedElement> myVariables = new HashMap<String, PsiNamedElement>();

      @Override
      protected ResolveResult execute(final PsiNamedElement element, final boolean error) {
        return new PsiElementResolveResult(element, false);
      }
    };

    boolean ifContinue = true;

    final PsiElement parent = getParent();
    if (parent instanceof CfmlAssignmentExpression) {
      CfmlAssignmentExpression assignment = (CfmlAssignmentExpression)parent;
      CfmlVariable var = assignment.getAssignedVariable();
      if (var != null && assignment.getAssignedVariableElement() == this) {
        ifContinue = processor.execute(var, ResolveState.initial());
      }
    }

    if (ifContinue) {
      processVariantsInner(processor);
    }
    final ResolveResult[] variantsResults = processor.getVariants(ResolveResult.EMPTY_ARRAY);
    List<ResolveResult> results = new ArrayList<>();
    for (ResolveResult variantsResult : variantsResults) {
      if (variantsResult.getElement() != null) {
        PsiElement parentRef = variantsResult.getElement().getParent();
        if (!(parentRef instanceof CfmlReferenceExpression) ||
            (variantsResult.getElement() instanceof CfmlAssignmentExpression.AssignedVariable &&
             CfmlUtil.hasEqualScope(this, (CfmlReferenceExpression)parentRef))) {
          results.add(variantsResult);
        }
      }
    }
    if (results.isEmpty()) {
      return ResolveResult.EMPTY_ARRAY;
    }
    // resolve to truly declaration if found, otherwise resolve to the nearest assignment
    if (results.size() > 1) {
      ResolveResult result = results.get(results.size() - 1);
      PsiElement element = result.getElement();
      if (element instanceof CfmlVariable && ((CfmlVariable)element).isTrulyDeclaration()) {
        return new ResolveResult[]{result};
      }
    }
    return new ResolveResult[]{results.get(0)};
  }

  @Override
  @NotNull
  protected CfmlReferenceExpression parseReference(String newText) {
    return CfmlPsiUtil.createReferenceExpression(newText, getProject());
  }

  @Override
  protected PsiElement getSeparator() {
    return findChildByType(CfscriptTokenTypes.POINT);
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    if (getScope() != null) {
      return new TextRange(0, getTextLength());
    }
    return super.getRangeInElement();
  }

  private static boolean checkType(@Nullable PsiElement element, IElementType type) {
    if (element == null) {
      return false;
    }
    ASTNode node = element.getNode();
    if (node != null && node.getElementType().equals(type)) {
      return true;
    }
    return false;
  }

  @Nullable
  public PsiElement getScope() {
    PsiElement identifier = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    if (identifier != null) {
      PsiElement prevSubling = identifier.getPrevSibling();
      if (prevSubling != null &&
          checkType(prevSubling, CfscriptTokenTypes.POINT) &&
          checkType(prevSubling.getPrevSibling(), CfscriptTokenTypes.SCOPE_KEYWORD)) {
        return prevSubling.getPrevSibling();
      }
    }
    return null;
  }

  @Override
  protected PsiElement getReferenceNameElement() {
    PsiElement identifier = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    if (identifier == null) {
      return findChildByType(CfmlTokenTypes.STRING_TEXT);
    }
    /*
    if (getScope() != null) {
      return identifier.getParent();
    }
    */
    return identifier;
  }

  @Override
  public Object @NotNull [] getVariants() {
    final CfmlVariantsProcessor<PsiNamedElement> processor = new CfmlVariantsProcessor<>(this, getParent(), null) {
      Set<String> myVariablesNames = new HashSet<>();

      @Override
      protected PsiNamedElement execute(final PsiNamedElement element, final boolean error) {
        if (element instanceof CfmlVariable) {
          if (myVariablesNames.add(element.getName())) {
            return element;
          }
          return null;
        }
        else {
          // only variables can be scoped
          PsiElement scope = getScope();
          if (scope != null && !scope.getText().equalsIgnoreCase("this")) {
            return null;
          }
        }
        return element;
      }
    };
    processVariantsInner(processor);
    PsiNamedElement[] variants = processor.getVariants(PsiNamedElement.EMPTY_ARRAY);
    HashSet<LookupElement> result = new HashSet<>();
    for (PsiNamedElement namedElement : variants) {
      if (namedElement instanceof CfmlProperty) {
        final String capitalizedName = StringUtil.capitalize(StringUtil.notNullize(namedElement.getName()));
        if (((CfmlProperty)namedElement).hasGetter() ||
            (namedElement.getParent() instanceof CfmlComponent &&
             (((CfmlComponent)namedElement.getParent()).hasImplicitAccessors() ||
              ((CfmlComponent)namedElement.getParent()).isPersistent()))) {
          result.add(LookupElementBuilder.create(namedElement, "get" + capitalizedName + "()").withCaseSensitivity(false)
                       .withIcon(IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method)));
        }
        if (((CfmlProperty)namedElement).hasSetter() ||
            (namedElement.getParent() instanceof CfmlComponent &&
             (((CfmlComponent)namedElement.getParent()).hasImplicitAccessors() ||
              ((CfmlComponent)namedElement.getParent()).isPersistent()))) {
          result.add(LookupElementBuilder.create(namedElement, "set" + capitalizedName + "()").withCaseSensitivity(false)
                       .withIcon(IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method)));
        }
      }
    }

    result.addAll(ContainerUtil.map2Set(variants, element -> {
      PsiElement scope = getScope();
      return CfmlLookUpItemUtil.namedElementToLookupItem(element, scope != null ? scope.getText() : null);
    }));
    return result.toArray();
  }

  @Override
  public PsiType getPsiType() {
    if (getParent() instanceof CfmlFunctionCallExpression) {
      final PsiType type = ((CfmlFunctionCallExpression)getParent()).getExternalType();
      if (type != null) {
        return type;
      }
    }
    final PsiElement element = resolve();
    if (element instanceof CfmlVariable) {
      if ((element instanceof CfmlAssignmentExpression.AssignedVariable)) {
        CfmlExpression rightExpr = ((CfmlAssignmentExpression.AssignedVariable)element).getRightHandExpr();
        if (rightExpr == this || (rightExpr instanceof CfmlFunctionCallExpression &&
                                  ((CfmlFunctionCallExpression)rightExpr).getExternalType() == null &&
                                  ((CfmlFunctionCallExpression)rightExpr).getReferenceExpression() == this)) {
          LOG.error("CFML parsing problem. Please report the problem to JetBrains with the file attached.",
                    new Throwable(rightExpr.getText()),
                    new Attachment("problem.cfml", element.getContainingFile().getText()));
          return null;
        }
      }
      return ((CfmlVariable)element).getPsiType();
    }
    if (element instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)element;
      return method.getReturnType();
    }
    if (element instanceof CfmlFunction) {
      return ((CfmlFunction)element).getReturnType();
    }
    return null;
  }

  @Override
  public String getReferenceName() {
    PsiElement identifier = getReferenceNameElement();
    return identifier != null ? identifier.getText() : "";
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);

    PsiElement newElement;

    final PsiElement referenceNameElement = getReferenceNameElement();
    if (referenceNameElement != null) {
      final ASTNode referenceNode = referenceNameElement.getNode();
      if (referenceNode == null) {
        return this;
      }
      if (referenceNode.getElementType() == CfmlTokenTypes.STRING_TEXT) {
        newElement = CfmlPsiUtil.createConstantString(newElementName, getProject());
      }
      else {
        newElement = CfmlPsiUtil.createIdentifier(newElementName, getProject());
      }
      getNode().replaceChild(referenceNode, newElement.getNode());
    }

    return this;
  }

  @Override
  public String toString() {
    return getNode().getElementType().toString();
  }
}
