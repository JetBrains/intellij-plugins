// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeNameImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagScriptImpl;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlPsiUtil {
  @NotNull
  public static Collection<String> findBetween(@NotNull String source, @NotNull String startMarker, @NotNull String endMarker) {
    int fromIndex = 0;
    Collection<String> collection = new LinkedList<>();
    while (fromIndex < source.length() && fromIndex >= 0) {
      int start = source.indexOf(startMarker, fromIndex);
      if (start < 0) {
        break;
      }
      start += startMarker.length();
      final int end = source.indexOf(endMarker, start);
      if (end < start) {
        break;
      }
      collection.add(source.substring(start, end));
      fromIndex = end + endMarker.length();
    }
    return collection;
  }

  @Nullable
  public static TextRange findRange(@NotNull String source, @NotNull String startMarker, @NotNull String endMarker) {
    int start = source.indexOf(startMarker);
    if (start < 0) {
      return null;
    }
    start += startMarker.length();
    final int end = source.indexOf(endMarker, start);
    if (end < start) {
      return null;
    }
    return new TextRange(start, end);
  }

  private final static Set<String> OUR_TRANSPARENT_FUNCTIONS = new HashSet<>();

  static {
    OUR_TRANSPARENT_FUNCTIONS.add("cfsilent");
    OUR_TRANSPARENT_FUNCTIONS.add(CfmlTagScriptImpl.TAG_NAME);
    OUR_TRANSPARENT_FUNCTIONS.add("cfprocessingdirective");
    OUR_TRANSPARENT_FUNCTIONS.add("cfsavecontent");
    OUR_TRANSPARENT_FUNCTIONS.add("cflock");
  }

  public static boolean processDeclarations(@NotNull final PsiScopeProcessor processor,
                                            @NotNull final ResolveState state,
                                            @Nullable final PsiElement lastParent,
                                            @NotNull final PsiElement currentElement) {
    PsiElement element = (lastParent == null ? currentElement.getLastChild() : lastParent.getPrevSibling());
    do {
      if (element instanceof PsiNamedElement && !(element instanceof CfmlFunction)) { // functions are processed separately
        if (!processor.execute(element, state)) {
          return false;
        }
      }
      else if (element instanceof CfmlTag) {
        if (!(element instanceof CfmlFunction)) { // functions are processed separately
          final PsiElement psiElement = ((CfmlTag)element).getDeclarativeElement();
          if (psiElement != null && !processor.execute(psiElement, state)) {
            return false;
          }
          if (OUR_TRANSPARENT_FUNCTIONS.contains(((CfmlTag)element).getTagName())) {
            if (!processDeclarations(processor, state, null, element)) {
              return false;
            }
          }
        }
      }
      else if (element instanceof CfmlAssignmentExpression assignmentExpression) {
        CfmlVariable assignedVariable = assignmentExpression.getAssignedVariable();
        if (assignedVariable != null &&
            lastParent != assignmentExpression.getRightHandExpr() &&
            !processor.execute(assignedVariable, state)) {
          return false;
        }
      }
      if (element == null) {
        return true;
      }
      element = element.getPrevSibling();
    }
    while (element != null);
    return true;
  }

  @Nullable
  public static CfmlTypedElement getTypedQualifierInner(PsiElement element) {
    if (element == null) {
      return null;
    }
    PsiElement child = element.getFirstChild();
    while (child != null) {
      if (child instanceof CfmlTypedElement) {
        return (CfmlTypedElement)child;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  @Nullable
  public static CfmlReference getQualifierInner(PsiElement element) {
    if (element == null) {
      return null;
    }
    PsiElement child = element.getFirstChild();
    while (child != null) {
      if (child instanceof CfmlReferenceExpression) {
        return (CfmlReferenceExpression)child;
      }
      if (child instanceof CfmlFunctionCallExpression) {
        return ((CfmlFunctionCallExpression)child).getReferenceExpression();
      }
      child = child.getNextSibling();
    }
    return null;
  }

  @Nullable
  public static PsiType getTypeByName(String typeName, Project project) {
    return JavaPsiFacade.getInstance(project).getElementFactory().createTypeByFQClassName(typeName, GlobalSearchScope.allScope(project));
  }

  public static CfmlFile createDummyFile(Project project, String text) {
    final String fileName = "dummy." + CfmlFileType.INSTANCE.getDefaultExtension();
    return (CfmlFile)PsiFileFactory.getInstance(project).createFileFromText(fileName, CfmlLanguage.INSTANCE, text);
  }

  @NotNull
  public static CfmlReferenceExpression createReferenceExpression(final String text, final Project project) {
    final CfmlFile dummyFile = createDummyFile(project, "<cfset " + text + " = 0>");
    final PsiElement tag = dummyFile.getFirstChild();
    assert tag != null;
    final CfmlAssignmentExpression assignment = PsiTreeUtil.getChildOfType(tag, CfmlAssignmentExpression.class);
    assert assignment != null;
    final CfmlReferenceExpression expression = PsiTreeUtil.getChildOfType(assignment, CfmlReferenceExpression.class);
    assert expression != null;
    return expression;
  }

  @NotNull
  public static PsiElement createIdentifier(final String text, final Project project) {
    final CfmlReferenceExpression reference = createReferenceExpression(text, project);
    final PsiElement identifier = reference.getFirstChild();
    assert identifier != null;
    final ASTNode identifierNode = identifier.getNode();
    assert identifierNode != null;
    assert identifierNode.getElementType() == CfscriptTokenTypes.IDENTIFIER;
    return identifier;
  }

  @NotNull
  public static PsiElement createConstantString(final String text, final Project project) {
    final CfmlFile dummyFile = createDummyFile(project, "<cffunction name=\"" + text + "\"></cffunction>");
    final PsiElement tag = dummyFile.getFirstChild();
    assert tag != null;
    final CfmlAttributeNameImpl namedAttribute = PsiTreeUtil.getChildOfType(tag, CfmlAttributeNameImpl.class);
    assert namedAttribute != null;
    final PsiElement element = namedAttribute.getValueElement();
    assert element != null;
    return element;
  }

  @Nullable
  public static String getPureAttributeValue(CfmlTag tag, String attributeName) {
    final CfmlAttributeImpl[] attributes = PsiTreeUtil.getChildrenOfType(tag, CfmlAttributeImpl.class);
    if (attributes == null) {
      return null;
    }
    for (CfmlAttributeImpl attribute : attributes) {
      if (attributeName.equals(attribute.getAttributeName())) {
        return attribute.getPureAttributeValue();
      }
    }
    return null;
  }

  public static boolean isFunctionDefinition(Object element) {
    return element instanceof CfmlFunction ||
           (element instanceof CfmlNamedAttributeImpl && ((CfmlNamedAttributeImpl)element).getParent() instanceof CfmlFunction);
  }

  public static CfmlFunction getFunctionDefinition(Object element) {
    if (element instanceof CfmlFunction) {
      return (CfmlFunction)element;
    }
    if (element instanceof CfmlNamedAttributeImpl && ((CfmlNamedAttributeImpl)element).getParent() instanceof CfmlFunction) {
      return ((CfmlFunction)((CfmlNamedAttributeImpl)element).getParent());
    }
    return null;
  }

  @Nullable
  public static PsiElement getAttributeValueElement(PsiElement element, @NotNull String attributeName) {
    final CfmlAttributeImpl[] attributes = PsiTreeUtil.getChildrenOfType(element, CfmlAttributeImpl.class);
    if (attributes == null) {
      return null;
    }
    for (CfmlAttributeImpl attribute : attributes) {
      if (attributeName.equals(attribute.getAttributeName())) {
        return attribute.getValueElement();
      }
    }
    return null;
  }

  public static PsiReference @NotNull [] getComponentReferencesFromAttributes(PsiElement element) {
    final PsiElement rEx = getAttributeValueElement(element, "extends");
    final PsiElement rImpl = getAttributeValueElement(element, "implements");

    ASTNode rExNode = rEx != null ? rEx.getNode() : null;
    ASTNode rImplNode = rImpl != null ? rImpl.getNode() : null;
    if (rExNode != null) {
      return rImplNode == null ? new PsiReference[]{new CfmlComponentReference(rExNode, element)} :
             new PsiReference[]{new CfmlComponentReference(rExNode, element), new CfmlComponentReference(rImplNode, element)};
    }
    if (rImplNode != null) {
      String implList = rImplNode.getText();
      if (!implList.contains(",")) {
        return new PsiReference[]{new CfmlComponentReference(rImplNode, element)};
      }/* else {
       // TODO: to parse list of components
      }*/
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  private static ASTNode getSuperComponentNode(PsiElement element) {
    final PsiElement rEx = getAttributeValueElement(element, "extends");

    if (rEx != null) {
      return rEx.getNode();
    }

    return null;
  }

  @NotNull
  public static String getSuperComponentName(PsiElement element) {
    ASTNode superComponentNode = getSuperComponentNode(element);
    if (superComponentNode != null) {
      return superComponentNode.getText();
    }
    return "";
  }

  @Nullable
  public static CfmlComponentReference getSuperComponentReference(PsiElement element) {
    ASTNode node = getSuperComponentNode(element);
    if (node != null) {
      return new CfmlComponentReference(node, element);
    }
    return null;
  }

  @Nullable
  public static CfmlComponent getSuperComponent(PsiElement element) {
    CfmlComponentReference referenceToSuperComponent = getSuperComponentReference(element);
    if (referenceToSuperComponent != null) {
      PsiElement resolve = referenceToSuperComponent.resolve();
      if (resolve instanceof CfmlComponent) {
        return (CfmlComponent)resolve;
      }
    }
    return null;
  }

  private static <Result extends PsiNamedElement> Result[] componentHierarchyGatherer(CfmlComponent component,
                                                                                      Function<? super CfmlComponent, Result[]> gatherer,
                                                                                      Result[] EMPTY_ARRAY, boolean isSuperPriority) {
    CfmlComponent currentComponent = isSuperPriority ? component.getSuper() : component;
    Set<String> names = new HashSet<>();
    List<Result> result = new LinkedList<>();
    while (currentComponent != null) {
      for (Result candidate : gatherer.apply(currentComponent)) {
        if (names.add(candidate.getName())) {
          result.add(candidate);
        }
      }
      currentComponent = currentComponent.getSuper();
    }
    if (isSuperPriority) {
      currentComponent = component;
      for (Result candidate : gatherer.apply(currentComponent)) {
        if (names.add(candidate.getName())) {
          result.add(candidate);
        }
      }
    }
    return result.toArray(EMPTY_ARRAY);
  }

  public static CfmlFunction @NotNull [] getFunctionsWithSupers(CfmlComponent component, boolean isSuperPriority) {
    return componentHierarchyGatherer(component, component1 -> component1.getFunctions(), CfmlFunction.EMPTY_ARRAY, isSuperPriority);
  }

  public static CfmlProperty @NotNull [] getPropertiesWithSupers(CfmlComponent component, boolean isSuperPriority) {
    return componentHierarchyGatherer(component, component1 -> component1.getProperties(), CfmlProperty.EMPTY_ARRAY, isSuperPriority);
  }

  public static boolean processGlobalVariablesForComponent(CfmlComponent component,
                                                           final PsiScopeProcessor processor,
                                                           final ResolveState state,
                                                           final PsiElement lastParent) {
    boolean res = true;
    try {
      component.accept(new CfmlRecursiveElementVisitor() {
        public void visitCfmlAssignmentExpression(CfmlAssignmentExpression expression) {
          if (expression.getFirstChild().getNode().getElementType() != CfscriptTokenTypes.VAR_KEYWORD &&
              expression.getParent() != lastParent) {
            if (expression.getAssignedVariable() != null && !processor.execute(expression.getAssignedVariable(), state)) {
              throw Stop.DONE;
            }
          }
        }

        @Override
        public void visitCfmlComponent(CfmlComponent component) {
          super.visitCfmlComponent(component);
        }

        @Override
        public void visitCfmlFunction(CfmlFunction function) {
          if (function != lastParent) {  // skip function we are inside
            super.visitCfmlFunction(function);
          }
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
          if (element != lastParent && element instanceof CfmlAssignmentExpression) {
            visitCfmlAssignmentExpression((CfmlAssignmentExpression)element);
          }
          else {
            super.visitElement(element);
          }
        }
      });
    }
    catch (CfmlRecursiveElementVisitor.Stop e) {
      res = false;
    }
    return res;
  }
}
