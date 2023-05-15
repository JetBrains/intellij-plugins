package com.intellij.lang.javascript.validation;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil.SearchedMemberType.Fields;

/**
 * @author Maxim.Mossienko
 */
public abstract class ActionScriptImplementedMethodProcessor extends JSCollectMembersToImplementProcessor {

  public static Collection<JSFunction> collectFunctionsToImplement(@NotNull JSClass clazz) {
    final JSFunctionCollector alreadyUsedMethods = new JSFunctionCollector();
    ActionScriptImplementedMethodProcessor processor = new ActionScriptImplementedMethodProcessor(clazz) {

      @Override
      protected void addNonImplementedFunction(final JSFunction function) {
        if (alreadyUsedMethods.hasFunctionWithTheSameKind(function.getName(), function.getKind())) return;
        alreadyUsedMethods.add(function);
      }
    };

    JSResolveUtil.processInterfaceMembers(clazz, processor);
    return alreadyUsedMethods.getFunctions();
  }

  @NotNull
  protected final JSClass myJsClass;

  public ActionScriptImplementedMethodProcessor(@NotNull final JSClass jsClass) {
    super(null, false);
    myJsClass = jsClass;
  }


  @NotNull
  @Override
  protected ResultSink createResultSink() {
    return new CompletionResultSink(place, null);
  }

  @Override
  protected void processMembers(@NotNull List<? extends PsiElement> results) {
    JSFunctionCollector functionsCollector = null;

    for (PsiElement _function : results) {
      if (!(_function instanceof JSFunction function)) {
        continue;
      }
      final String name = function.getName();
      if (name == null) continue;

      if (functionsCollector == null) {
        functionsCollector = collectVisibleFunctions();
      }

      JSFunction o = functionsCollector.findFunctionWithTheSameKind(name, function.getKind());

      if (o == null) {
        if (JSPsiImplUtils.isGetterOrSetter(function)) {
          JSVariable var = (JSVariable)JSInheritanceUtil.findMember(name, myJsClass, Fields, null, true);
          if (var != null && ActionScriptResolveUtil.fieldIsImplicitAccessorMethod(function, var)) {
            continue;
          }
        }
        addNonImplementedFunction(function);
      }
      else {
        addImplementedFunction(function, o);
      }
    }
  }

  @NotNull
  private JSFunctionCollector collectVisibleFunctions() {
    return JSFunctionCollector.collectAllVisibleClassFunctions(myJsClass, null, jsFunction -> {
      final JSAttributeList attributeList = jsFunction.getAttributeList();
      if (!JSInheritanceUtil.canHaveSuperMember(attributeList)) {
        return false;
      }
      PsiElement parentClass = JSResolveUtil.findParent(jsFunction);
      if (attributeList.getAccessType() != JSAttributeList.AccessType.PUBLIC && myJsClass != parentClass) {
        return false;
      }
      return true;
    });

  }

  protected void addImplementedFunction(final JSFunction interfaceFunction, final JSFunction implementationFunction) {
  }

  protected abstract void addNonImplementedFunction(final JSFunction function);
}
