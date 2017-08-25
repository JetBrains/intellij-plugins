package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.changeSignature.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.changeSignature.CallerChooserBase;
import com.intellij.refactoring.changeSignature.MemberNodeBase;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Consumer;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionScriptCreateConstructorFix extends CreateJSFunctionIntentionAction {

  @NotNull private final JSClass myClass;
  private final JSReferenceExpression myRefExpr;
  private final JSCallExpression myNode;

  private ActionScriptCreateConstructorFix(@NotNull JSClass clazz, JSReferenceExpression refExpr, JSCallExpression node) {
    super(clazz.getName(), true);
    myClass = clazz;
    myRefExpr = refExpr;
    myNode = node;
  }

  @Nullable
  public static ActionScriptCreateConstructorFix createIfApplicable(final JSCallExpression node) {
    final JSClass clazz;
    final JSReferenceExpression reference;
    if (node instanceof JSNewExpression) {
      JSExpression methodExpression = node.getMethodExpression();
      if (!(methodExpression instanceof JSReferenceExpression)) {
        return null;
      }
      PsiElement resolved = ((JSReferenceExpression)methodExpression).resolve();
      if (!(resolved instanceof JSClass) || resolved instanceof XmlBackedJSClass || ((JSClass)resolved).isInterface()) {
        return null;
      }
      clazz = (JSClass)resolved;
      reference = (JSReferenceExpression)methodExpression;
    }
    else {
      JSExpression methodExpression = node.getMethodExpression();
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
      reference = (JSReferenceExpression)clazz.findNameIdentifier().getPsi();
    }

    return new ActionScriptCreateConstructorFix(clazz, reference, node);
  }

  @NotNull
  @Override
  protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
    ASTNode lbrace = myClass.getNode().findChildByType(JSTokenTypes.LBRACE);
    return Pair.create(myRefExpr, lbrace.getPsi());
  }

  @Override
  protected void applyFix(final Project project, final PsiElement psiElement, PsiFile file, Editor editor) {
    final AtomicInteger count = new AtomicInteger();
    ReferencesSearch.search(myClass, myClass.getUseScope()).forEach(
      psiReference -> !isClassInstantiation(psiReference) || count.incrementAndGet() < 2);

    int usages = count.get();
    if (usages < 2) {
      usages += JSInheritanceUtil.findSuperConstructorCalls(myClass).size();
    }

    if (usages < 2) {
      final Collection<String> toImport = new ArrayList<>();
      for (JSExpression argument : myNode.getArguments()) {
        String type = JSResolveUtil.getQualifiedExpressionType(argument, argument.getContainingFile());
        if (StringUtil.isNotEmpty(type) && ImportUtils.needsImport(myClass, StringUtil.getPackageName(type))) {
          toImport.add(type);
        }
      }

      WriteAction.run(() -> {
        if (!toImport.isEmpty()) {
          FormatFixer formatFixer = ImportUtils.insertImportStatements(myClass, toImport);
          if (formatFixer != null) {
            formatFixer.fixFormat();
          }
        }
        super.applyFix(project, psiElement, myClass.getContainingFile(),
                       getEditor(myClass.getProject(), myClass.getContainingFile()));
      });
    }
    else {
      String text = "function " + myClass.getName() + "(){}";
      JSFunction fakeFunction = (JSFunction)JSChangeUtil.createStatementFromText(project, text, JavaScriptSupportLoader.ECMA_SCRIPT_L4)
        .getPsi();

      new ChangeSignatureFix(fakeFunction, myNode.getArgumentList()) {
        @Override
        protected Pair<Boolean, List<JSParameterInfo>> handleCall(@NotNull JSFunction function, JSExpression[] arguments, boolean dummy) {
          List<JSParameterInfo> parameterInfos = super.handleCall(function, arguments, dummy).second;
          return Pair.create(true, parameterInfos); // always show dialog
        }

        @Override
        protected JSChangeSignatureDialog createDialog(PsiElement context, final List<JSParameterInfo> paramInfos) {
          JSMethodDescriptor descriptor = new JSMethodDescriptor(myFunction.getElement(), true) {
            @Override
            public List<JSParameterInfo> getParameters() {
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
                                 myClass.getName(),
                                 "",
                                 paramInfos.toArray(new JSParameterInfo[paramInfos.size()]), Collections.emptySet());
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
                               PsiFile file,
                               PsiElement anchorParent) {
    if (constructorShouldBePublic()) {
      template.addTextSegment("public ");
    }

    writeFunctionAndName(template, myClass.getName(), file, null, referenceExpression);
    template.addTextSegment("(");
    addParameters(template, myNode.getArgumentList(), myNode, file);
    template.addTextSegment("){");
    addBody(template, referenceExpression, file);
    template.addTextSegment("}");
  }

  private boolean constructorShouldBePublic() {
    JSClass contextClass;
    return myClass.getAttributeList().getAccessType() == JSAttributeList.AccessType.PUBLIC ||
           (contextClass = JSResolveUtil.getClassOfContext(myNode)) != null &&
           JSPsiImplUtils.differentPackageName(JSResolveUtil.getPackageName(myClass), JSResolveUtil.getPackageName(contextClass));
  }

  @NotNull
  @Override
  public String getName() {
    return JSBundle.message("actionscript.create.constructor.intention.name", myClass.getName());
  }

  private class MyDialog extends JSChangeSignatureDialog {
    public MyDialog(JSMethodDescriptor descriptor, PsiElement context) {
      super(descriptor, context);
      setTitle(JSBundle.message("create.constructor.dialog.title"));
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
                             JSAttributeList.AccessType.valueOf(getVisibility()), myClass.getName(), "",
                             parameters.toArray(new JSParameterInfo[parameters.size()]),
                             myMethodsToPropagateParameters != null
                             ? myMethodsToPropagateParameters
                             : Collections.emptySet());
    }
  }

  private class MyCallerChooser extends JSCallerChooser {
    public MyCallerChooser(JSFunction method, String title, Tree treeToReuse, Consumer<Set<JSFunction>> callback) {
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
    public MyMethodNode(JSFunction method, HashSet<JSFunction> called, Runnable cancelCallback) {
      super(method, called, myClass.getProject(), cancelCallback);
    }

    @Override
    protected List<JSFunction> computeCallers() {
      final Collection<PsiReference> refs = Collections.synchronizedCollection(new ArrayList<PsiReference>());
      ReferencesSearch.search(myClass, myClass.getUseScope(), true).forEach(psiReference -> {
        if (isClassInstantiation(psiReference)) {
          refs.add(psiReference);
        }
        return true;
      });

      Set<JSFunction> result = new java.util.HashSet<>();
      for (PsiReference reference : refs) {
        addCallExpression((JSNewExpression)reference.getElement().getParent(), result);
      }

      for (JSCallExpression superCall : JSInheritanceUtil.findSuperConstructorCalls(myClass)) {
        addCallExpression(superCall, result);
      }
      return new ArrayList<>(result);
    }
  }

  private class MyProcessor extends JSChangeSignatureProcessor {

    public MyProcessor(JSFunction method,
                       JSAttributeList.AccessType visibility,
                       String methodName,
                       String returnType,
                       JSParameterInfo[] parameters, Set<JSFunction> methodsToPropagateParameters) {
      super(method, visibility, methodName, returnType, parameters, methodsToPropagateParameters, Collections.emptySet());
    }

    @NotNull
    @Override
    protected UsageInfo[] findUsages() {
      final Collection<UsageInfo> declarations = Collections.synchronizedCollection(new HashSet<UsageInfo>());
      final Collection<OtherUsageInfo> usages = Collections.synchronizedCollection(new HashSet<OtherUsageInfo>());

      ReferencesSearch.search(myClass, myClass.getUseScope()).forEach(psiReference -> {
        if (isClassInstantiation(psiReference)) {
          PsiElement element = psiReference.getElement();
          usages.add(new OtherUsageInfo(element, null, myParameters, shouldPropagate(element), 0, 0));
        }
        return true;
      });

      for (JSCallExpression superCall : JSInheritanceUtil.findSuperConstructorCalls(myClass)) {
        usages.add(new OtherUsageInfo(superCall.getMethodExpression(), null, myParameters, shouldPropagate(superCall), 0, 0));
      }

      findPropagationUsages(declarations, usages);
      Collection<UsageInfo> result = new ArrayList<>(declarations);
      result.addAll(usages);
      return result.toArray(new UsageInfo[result.size()]);
    }

    @Override
    protected void performRefactoring(@NotNull UsageInfo[] usageInfos) {
      final Collection<String> toImport = new ArrayList<>();
      for (JSExpression argument : myNode.getArguments()) {
        String type = JSResolveUtil.getQualifiedExpressionType(argument, argument.getContainingFile());
        if (StringUtil.isNotEmpty(type) && ImportUtils.needsImport(myClass, StringUtil.getPackageName(type))) {
          toImport.add(type);
        }
      }

      StringBuilder newConstuctorText = new StringBuilder();
      if (constructorShouldBePublic()) {
        newConstuctorText.append("public ");
      }
      newConstuctorText.append("function ").append(myClass.getName());
      JSChangeSignatureDialog.buildParameterListText(Arrays.asList(myParameters), newConstuctorText, true, DialectDetector.dialectOfElement(myClass));
      newConstuctorText.append("{}");
      JSFunction constructorPrototype = (JSFunction)JSChangeUtil.createStatementFromText(myProject, newConstuctorText.toString(),
                                                                                 JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
      PsiElement newConstuctor = myClass.add(constructorPrototype); // TODO anchor
      FormatFixer.create(newConstuctor, FormatFixer.Mode.Reformat).fixFormat();
      if (!toImport.isEmpty()) {
        FormatFixer formatFixer = ImportUtils.insertImportStatements(myClass, toImport);
        if (formatFixer != null) {
          formatFixer.fixFormat();
        }
        List<FormatFixer> fixers = ECMAScriptImportOptimizer.executeNoFormat(myClass.getContainingFile());
        FormatFixer.fixAll(fixers);
      }
      super.performRefactoring(usageInfos);
    }

    @Override
    protected String getCommandName() {
      return getName();
    }
  }

}
