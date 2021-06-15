// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.ChangeTracker;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.javascript.JSTargetedInjector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.PsiChangeTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

final class FlashUmlChangeTracker extends ChangeTracker<JSClass, JSNamedElement, JSReferenceExpression> {
  private static class NameFilter<T extends PsiNamedElement> extends PsiFilter<T> {
    private NameFilter(@NotNull Class<T> filter) {
      super(filter);
    }

    @Override
    public boolean areEquivalent(T e1, T e2) {
      return Objects.equals(e1.getName(), e2.getName());
    }
  }

  private static final class MethodFilter extends NameFilter<JSFunction> {
    private final JSFunction.FunctionKind myKind;

    private MethodFilter(JSFunction.FunctionKind kind) {
      super(JSFunction.class);
      myKind = kind;
    }

    @Override
    public boolean accept(JSFunction element) {
      return JSUtils.getMemberContainingClass(element) != null && element.getKind() == myKind;
    }

    @Override
    public Visitor<JSFunction> createVisitor(List<? super JSFunction> elements) {
      return new InjectingVisitor<>(this, elements);
    }
  }

  private static final PsiFilter<JSClass> CLASS_FILTER = new PsiFilter<>(JSClass.class) {
    @Override
    public boolean accept(JSClass element) {
      return element instanceof XmlBackedJSClassImpl || element.getParent() instanceof JSPackageStatement;
    }

    @Override
    public boolean areEquivalent(JSClass e1, JSClass e2) {
      if (e1 instanceof XmlBackedJSClassImpl && e2 instanceof XmlBackedJSClassImpl) {
        return e1.getQualifiedName().equals(e2.getQualifiedName());
      }
      return super.areEquivalent(e1, e2);
    }
  };

  private static final PsiFilter<JSFunction> SIMPLE_METHOD_FILTER = new MethodFilter(JSFunction.FunctionKind.SIMPLE);

  private static final PsiFilter<JSFunction> CONSTRUCTOR_FILTER = new MethodFilter(JSFunction.FunctionKind.CONSTRUCTOR);

  private static final PsiFilter<JSFunction> GETTER_FILTER = new MethodFilter(JSFunction.FunctionKind.GETTER);

  private static final PsiFilter<JSFunction> SETTER_FILTER = new MethodFilter(JSFunction.FunctionKind.SETTER);

  private static final PsiFilter<JSVariable> FIELD_FILTER = new NameFilter<>(JSVariable.class) {
    @Override
    public boolean accept(JSVariable element) {
      return JSUtils.getMemberContainingClass(element) != null;
    }

    @Override
    public Visitor<JSVariable> createVisitor(List<? super JSVariable> elements) {
      return new InjectingVisitor<>(this, elements);
    }
  };

  private static final PsiFilter<JSReferenceExpression> EXTENDS_FILTER = new ReferenceListFilter(true);
  private static final PsiFilter<JSReferenceExpression> IMPLEMENTS_FILTER = new ReferenceListFilter(false);

  private Map<JSClass, FileStatus> myNodeElements;

  public FlashUmlChangeTracker(Project project, @Nullable PsiFile before, @Nullable PsiFile after) {
    super(project, before, after);
  }

  @Override
  public PsiFilter<JSClass>[] getNodeFilters() {
    return new PsiFilter[]{CLASS_FILTER};
  }

  @Override
  public PsiFilter<JSNamedElement>[] getNodeContentFilters() {
    return new PsiFilter[]{SIMPLE_METHOD_FILTER, CONSTRUCTOR_FILTER, FIELD_FILTER, GETTER_FILTER, SETTER_FILTER};
  }

  @Override
  public PsiFilter<JSReferenceExpression>[] getRelationshipFilters() {
    return new PsiFilter[]{EXTENDS_FILTER, IMPLEMENTS_FILTER};
  }

  @Override
  public String getPresentableName(PsiNamedElement e) {
    if (e instanceof JSVariable) {
      return FlashUmlElementManager.getFieldText((JSVariable)e);
    }
    else if (e instanceof JSFunction) {
      return FlashUmlElementManager.getMethodText((JSFunction)e);
    }
    else {
      return super.getPresentableName(e);
    }
  }

  @Override
  public String getType(JSNamedElement jsNamedElement) {
    return FlashUmlElementManager.getPresentableTypeStatic(jsNamedElement);
  }

  @Override
  public Icon getIcon(PsiNamedElement e) {
    return FlashUmlElementManager.getNodeElementIconStatic(e);
  }

  @Override
  public String getQualifiedName(JSClass e, VirtualFile containingFile) {
    return e.getQualifiedName();
  }

  @Override
  public Map<JSClass, FileStatus> getNodeElements() {
    if (myNodeElements == null) {
      myNodeElements = new HashMap<>();

      Pair<PsiElement, PsiElement> beforeAndAfter = adjustBeforeAfter();
      for (PsiFilter<JSClass> filter : getNodeFilters()) {
        myNodeElements.putAll(PsiChangeTracker.getElementsChanged(beforeAndAfter.second, beforeAndAfter.first, filter));
      }
    }
    return myNodeElements;
  }

  private Pair<PsiElement, PsiElement> adjustBeforeAfter() {
    PsiElement before = getBefore();
    PsiElement after = getAfter();
    if (after != null && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)after) && before == null) {
      after = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)getAfter());
    }
    else if (before != null && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)before) && after == null) {
      before = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)getBefore());
    }
    else if (before != null &&
             JavaScriptSupportLoader.isFlexMxmFile((PsiFile)before) &&
             after != null &&
             JavaScriptSupportLoader.isFlexMxmFile((PsiFile)after)) {
      before = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)before);
      after = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)after);
    }
    return Pair.create(before, after);
  }

  @Override
  public RelationshipInfo[] getRelationships() {
    final List<RelationshipInfo> result = new ArrayList<>();
    Pair<PsiElement, PsiElement> beforeAndAfter = adjustBeforeAfter();
    for (PsiFilter<JSReferenceExpression> filter : getRelationshipFilters()) {
      final Map<JSReferenceExpression, FileStatus> map =
        PsiChangeTracker.getElementsChanged(beforeAndAfter.second, beforeAndAfter.first, filter);

      for (JSReferenceExpression expression : map.keySet()) {
        JSClass sourceClass = PsiTreeUtil.getParentOfType(expression, JSClass.class);
        if (sourceClass == null) {
          continue;
        }
        if (InjectedLanguageManager.getInstance(sourceClass.getProject()).getInjectionHost(sourceClass) != null) {
          sourceClass = JSResolveUtil.getXmlBackedClass((JSFile)sourceClass.getContainingFile());
        }

        JSReferenceList refList = PsiTreeUtil.getParentOfType(expression, JSReferenceList.class);
        assert refList != null;
        final JSExpression[] references = refList.getExpressions();
        final JSClass[] referencedClasses = refList.getReferencedClasses();
        JSClass targetClass = null;
        for (int i = 0; i < references.length; i++) {
          if (references[i] == expression) {
            targetClass = i < referencedClasses.length ? referencedClasses[i] : null;
            break;
          }
        }
        if (targetClass == null) {
          continue;
        }

        EdgeType edgeType = filter == IMPLEMENTS_FILTER ? EdgeType.IMPLEMENTS : EdgeType.EXTENDS;
        result.add(new RelationshipInfo(sourceClass.getQualifiedName(), targetClass.getQualifiedName(), edgeType, map.get(expression)));
      }
    }
    return result.toArray(RelationshipInfo.EMPTY);
  }

  @Override
  public PsiNamedElement findElementByFQN(Project project, String fqn) {
    Object o = FlashUmlVfsResolver.resolveElementByFqnStatic(fqn, project);
    return o instanceof JSClass ? (PsiNamedElement)o : null;
  }

  private static class ReferenceListFilter extends PsiFilter<JSReferenceExpression> {
    private final boolean myExtends;

    ReferenceListFilter(boolean isExtends) {
      super(JSReferenceExpression.class);
      myExtends = isExtends;
    }

    @Override
    public boolean accept(JSReferenceExpression element) {
      final PsiElement parent = element.getParent();
      return parent instanceof JSReferenceListMember &&
             parent.getParent().getNode().findChildByType(myExtends ? JSTokenTypes.EXTENDS_KEYWORD : JSTokenTypes.IMPLEMENTS_KEYWORD) != null;
    }

    @Override
    public Visitor<JSReferenceExpression> createVisitor(List<? super JSReferenceExpression> elements) {
      return new InjectingVisitor<>(this, elements);
    }
  }

  private static class InjectingVisitor<T extends PsiElement> extends PsiFilter.Visitor<T> {
    InjectingVisitor(PsiFilter<? super T> filter, List<? super T> elements) {
      super(filter, elements);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
      super.visitElement(element);
      if (element instanceof XmlText || element instanceof XmlAttributeValue) {
        final XmlTag parentTag = PsiTreeUtil.getParentOfType(element, XmlTag.class); // actually we need just any tag here
        for (MultiHostInjector injector : MultiHostInjector.MULTIHOST_INJECTOR_EP_NAME.getExtensions(element.getProject())) {
          if (injector instanceof JSTargetedInjector) {
            injector.getLanguagesToInject(new XmlBackedJSClassImpl.InjectedScriptsVisitor.MyRegistrar(parentTag, new JSResolveUtil.JSInjectedFilesVisitor() {
                @Override
                protected void process(JSFile file) {
                  file.acceptChildren(InjectingVisitor.this);
                }
              }), element);
          }
        }
      }
    }
  }
}
