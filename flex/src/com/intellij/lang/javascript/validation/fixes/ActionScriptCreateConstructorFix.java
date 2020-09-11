// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.template.Template;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.changeSignature.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.changeSignature.CallerChooserBase;
import com.intellij.refactoring.changeSignature.MemberNodeBase;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ActionScriptCreateConstructorFix extends CreateJSFunctionIntentionAction {

  @NotNull private final SmartPsiElementPointer<JSClass> myClass;
  @NotNull private final SmartPsiElementPointer<JSCallExpression> myNode;
  @NotNull private final String myName;

  private ActionScriptCreateConstructorFix(@NotNull JSClass clazz,
                                           @NotNull JSCallExpression node) {
    super(clazz.getName(), true, false);
    SmartPointerManager manager = SmartPointerManager.getInstance(clazz.getProject());
    myClass = manager.createSmartPsiElementPointer(clazz);
    myNode = manager.createSmartPsiElementPointer(node);
    myName = StringUtil.notNullize(clazz.getName());
  }

  @Override
  protected void appendFunctionBody(Template template, JSReferenceExpression refExpr, PsiElement anchorParent) {
  }

  @Nullable
  public static ActionScriptCreateConstructorFix createIfApplicable(@NotNull JSCallExpression node) {
    final JSClass clazz;
    JSExpression methodExpression = node.getMethodExpression();
    if (node instanceof JSNewExpression) {
      if (!(methodExpression instanceof JSReferenceExpression)) {
        return null;
      }
      PsiElement resolved = ((JSReferenceExpression)methodExpression).resolve();
      if (!(resolved instanceof JSClass) || resolved instanceof XmlBackedJSClass || ((JSClass)resolved).isInterface()) {
        return null;
      }
      clazz = (JSClass)resolved;
    }
    else {
      if (!(methodExpression instanceof JSSuperExpression)) {
        return null;
      }
      JSClass containingClass = JSResolveUtil.getClassOfContext(node);
      if (containingClass == null) {
        return null;
      }
      clazz = containingClass.getSuperClasses()[0];
      if (clazz.isInterface()) {
        return null;
      }
    }

    return new ActionScriptCreateConstructorFix(clazz, node);
  }

  @NotNull
  @Override
  protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
    JSClass element = myClass.getElement();
    if (element == null) return Pair.create(null, null);

    ASTNode lbrace = element.getNode().findChildByType(JSTokenTypes.LBRACE);
    JSCallExpression callExpression = myNode.getElement();
    JSExpression methodExpression = callExpression != null ? callExpression.getMethodExpression() : null;
    return Pair.create(ObjectUtils.tryCast(methodExpression, JSReferenceExpression.class), lbrace.getPsi());
  }

  @Override
  protected void applyFix(final Project project, final PsiElement psiElement, @NotNull PsiFile file, @Nullable Editor editor) {
    final AtomicInteger count = new AtomicInteger();
    JSClass jsClass = myClass.getElement();
    JSCallExpression node = myNode.getElement();
    if (jsClass == null || node == null) return;

    ReferencesSearch.search(jsClass, jsClass.getUseScope()).forEach(
      psiReference -> !isClassInstantiation(psiReference) || count.incrementAndGet() < 2);

    int usages = count.get();
    if (usages < 2) {
      usages += JSInheritanceUtil.findSuperConstructorCalls(jsClass).size();
    }

    if (usages < 2) {
      final Collection<String> toImport = new ArrayList<>();
      for (JSExpression argument : node.getArguments()) {
        String type = ActionScriptResolveUtil.getQualifiedExpressionType(argument, argument.getContainingFile());
        if (StringUtil.isNotEmpty(type) && ImportUtils.needsImport(jsClass, StringUtil.getPackageName(type))) {
          toImport.add(type);
        }
      }
      if (!FileModificationService.getInstance().preparePsiElementForWrite(jsClass)) return;
      final Editor finalEditor = getEditor(jsClass.getProject(), jsClass.getContainingFile());
      WriteAction.run(() -> {
        if (!toImport.isEmpty()) {
          FormatFixer formatFixer = ImportUtils.insertImportStatements(jsClass, toImport);
          if (formatFixer != null) {
            formatFixer.fixFormat();
          }
        }
        super.applyFix(project, psiElement, jsClass.getContainingFile(), finalEditor);
      });
    }
    else {
      String text = "function " + jsClass.getName() + "(){}";
      JSFunction fakeFunction = (JSFunction)JSChangeUtil.createStatementFromText(project, text, JavaScriptSupportLoader.ECMA_SCRIPT_L4)
        .getPsi();

      new JSChangeSignatureFix(fakeFunction, node.getArgumentList()) {
        @Override
        protected Pair<Boolean, List<JSParameterInfo>> handleCall(@NotNull JSFunction function, JSExpression @NotNull [] arguments, boolean dryRun) {
          List<JSParameterInfo> parameterInfos = super.handleCall(function, arguments, dryRun).second;
          return Pair.create(true, parameterInfos); // always show dialog
        }

        @Override
        protected JSChangeSignatureDialog createDialog(PsiElement context, final List<JSParameterInfo> paramInfos) {
          JSMethodDescriptor descriptor = new JSMethodDescriptor(getFunction(), true) {
            @Override
            public @NotNull List<JSParameterInfo> getParameters() {
              return paramInfos;
            }
          };
          return new MyDialog(descriptor, context);
        }

        @Override
        protected JSChangeSignatureProcessor createProcessor(List<JSParameterInfo> paramInfos,
                                                             JSAttributeList attributeList,
                                                             @NotNull JSFunction function) {
          return new MyProcessor(function,
                                 attributeList != null ? attributeList.getAccessType() : JSAttributeList.AccessType.PACKAGE_LOCAL,
                                 jsClass.getName(),
                                 "",
                                 paramInfos.toArray(JSParameterInfo.EMPTY_ARRAY), Collections.emptySet());
        }
      }.invoke(project, editor, file);
    }
  }

  private static boolean isClassInstantiation(PsiReference psiReference) {
    return psiReference instanceof JSReferenceExpression && ((JSReferenceExpression)psiReference).getParent() instanceof JSNewExpression;
  }

  @Override
  protected void buildTemplate(Template template,
                               JSReferenceExpression referenceExpression,
                               boolean staticContext,
                               @NotNull PsiElement anchorParent) {
    if (constructorShouldBePublic()) {
      template.addTextSegment("public ");
    }
    JSClass jsClass = myClass.getElement();
    assert jsClass != null;
    JSCallExpression node = myNode.getElement();
    assert node != null;

    writeFunctionAndName(template, jsClass.getName(), jsClass, jsClass, referenceExpression);
    template.addTextSegment("(");
    addParameters(template, node.getArguments(), node, jsClass);
    template.addTextSegment("){");
    addBody(template, referenceExpression, anchorParent);
    template.addTextSegment("}");
  }

  private boolean constructorShouldBePublic() {
    JSClass contextClass;
    JSClass jsClass = myClass.getElement();
    return jsClass.getAttributeList().getAccessType() == JSAttributeList.AccessType.PUBLIC ||
           (contextClass = JSResolveUtil.getClassOfContext(myNode.getElement())) != null &&
           JSPsiImplUtils.differentPackageName(JSResolveUtil.getPackageName(jsClass), JSResolveUtil.getPackageName(contextClass));
  }

  @NotNull
  @Override
  public String getName() {
    return FlexBundle.message("actionscript.create.constructor.intention.name", myName);
  }

  private class MyDialog extends JSChangeSignatureDialog {
    MyDialog(JSMethodDescriptor descriptor, PsiElement context) {
      super(descriptor, context);
      setTitle(JavaScriptBundle.message("create.constructor.dialog.title"));
    }

    @Override
    protected CallerChooserBase<JSFunction> createCallerChooser(String title,
                                                                Tree treeToReuse,
                                                                Consumer<Set<JSFunction>> callback) {
      return new MyCallerChooser(myMethod.getMethod(), title, treeToReuse, callback);
    }

    @Override
    protected JSChangeSignatureProcessor createRefactoringProcessor() {
      List<JSParameterInfo> parameters = getParameters();
      return new MyProcessor(myMethod.getMethod(),
                             JSAttributeList.AccessType.valueOf(getVisibility()), myClass.getElement().getName(), "",
                             parameters.toArray(JSParameterInfo.EMPTY_ARRAY),
                             myMethodsToPropagateParameters != null
                             ? myMethodsToPropagateParameters
                             : Collections.emptySet());
    }
  }

  private class MyCallerChooser extends JSCallerChooser {
    MyCallerChooser(JSFunction method, @NlsContexts.DialogTitle String title, Tree treeToReuse, Consumer<Set<JSFunction>> callback) {
      super(method, method.getProject(), title, treeToReuse, callback);
    }

    @Override
    protected MemberNodeBase<JSFunction> createTreeNodeFor(JSFunction method,
                                                           HashSet<JSFunction> called,
                                                           Runnable cancelCallback) {
      return new MyMethodNode(method, called, cancelCallback);
    }
  }

  private class MyMethodNode extends JSMethodNode {
    MyMethodNode(JSFunction method, HashSet<JSFunction> called, Runnable cancelCallback) {
      super(method, called, myClass.getProject(), cancelCallback);
    }

    @Override
    protected List<JSFunction> computeCallers() {
      final Collection<PsiReference> refs = Collections.synchronizedCollection(new ArrayList<>());
      JSClass jsClass = myClass.getElement();
      assert jsClass != null;

      ReferencesSearch.search(jsClass, jsClass.getUseScope(), true).forEach(psiReference -> {
        if (isClassInstantiation(psiReference)) {
          refs.add(psiReference);
        }
        return true;
      });

      Set<JSFunction> result = new HashSet<>();
      for (PsiReference reference : refs) {
        addCallExpression((JSNewExpression)reference.getElement().getParent(), result);
      }

      for (JSCallExpression superCall : JSInheritanceUtil.findSuperConstructorCalls(jsClass)) {
        addCallExpression(superCall, result);
      }
      return new ArrayList<>(result);
    }
  }

  private class MyProcessor extends JSChangeSignatureProcessor {

    MyProcessor(JSFunction method,
                       JSAttributeList.AccessType visibility,
                       String methodName,
                       String returnType,
                       JSParameterInfo[] parameters, Set<JSFunction> methodsToPropagateParameters) {
      super(method, visibility, methodName, returnType, parameters, methodsToPropagateParameters, Collections.emptySet());
    }

    @Override
    protected UsageInfo @NotNull [] findUsages() {
      final Collection<UsageInfo> declarations = Collections.synchronizedCollection(new HashSet<>());
      final Collection<OtherUsageInfo> usages = Collections.synchronizedCollection(new HashSet<>());

      JSClass jsClass = myClass.getElement();
      assert jsClass != null;

      ReferencesSearch.search(jsClass, jsClass.getUseScope()).forEach(psiReference -> {
        if (isClassInstantiation(psiReference)) {
          PsiElement element = psiReference.getElement();
          usages.add(new OtherUsageInfo(element, null, myParameters, shouldPropagate(element), 0, 0));
        }
        return true;
      });

      for (JSCallExpression superCall : JSInheritanceUtil.findSuperConstructorCalls(jsClass)) {
        usages.add(new OtherUsageInfo(superCall.getMethodExpression(), null, myParameters, shouldPropagate(superCall), 0, 0));
      }

      findPropagationUsages(declarations, usages);
      Collection<UsageInfo> result = new ArrayList<>(declarations);
      result.addAll(usages);
      return result.toArray(UsageInfo.EMPTY_ARRAY);
    }

    @Override
    protected void performRefactoring(UsageInfo @NotNull [] usageInfos) {
      final Collection<String> toImport = new ArrayList<>();
      JSCallExpression node = myNode.getElement();
      assert node != null;
      JSClass jsClass = myClass.getElement();
      assert jsClass != null;

      for (JSExpression argument : node.getArguments()) {
        String type = ActionScriptResolveUtil.getQualifiedExpressionType(argument, argument.getContainingFile());
        if (StringUtil.isNotEmpty(type) && ImportUtils.needsImport(jsClass, StringUtil.getPackageName(type))) {
          toImport.add(type);
        }
      }

      StringBuilder newConstuctorText = new StringBuilder();
      if (constructorShouldBePublic()) {
        newConstuctorText.append("public ");
      }
      newConstuctorText.append("function ").append(jsClass.getName());
      JSChangeSignatureDialog.buildParameterListText(Arrays.asList(myParameters), newConstuctorText, DialectDetector.dialectOfElement(
        jsClass));
      newConstuctorText.append("{}");
      JSFunction constructorPrototype = (JSFunction)JSChangeUtil.createStatementFromText(myProject, newConstuctorText.toString(),
                                                                                 JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
      PsiElement newConstuctor = jsClass.add(constructorPrototype); // TODO anchor
      FormatFixer.create(newConstuctor, FormatFixer.Mode.Reformat).fixFormat();
      if (!toImport.isEmpty()) {
        FormatFixer formatFixer = ImportUtils.insertImportStatements(jsClass, toImport);
        if (formatFixer != null) {
          formatFixer.fixFormat();
        }
        List<FormatFixer> fixers = ECMAScriptImportOptimizer.executeNoFormat(jsClass.getContainingFile());
        FormatFixer.fixAll(fixers);
      }
      super.performRefactoring(usageInfos);
    }

    @NotNull
    @Override
    protected String getCommandName() {
      return getName();
    }
  }

}
