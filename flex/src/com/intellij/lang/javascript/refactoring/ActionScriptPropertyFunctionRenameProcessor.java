package com.intellij.lang.javascript.refactoring;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ActionScriptPropertyFunctionRenameProcessor extends JSDefaultRenameProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return DialectDetector.isActionScript(element)
           && (element instanceof JSVariable ||
               JSGetterSetterRenameUtil.isGetterSetterFunction(element));
  }

  @Override
  public void prepareRenaming(PsiElement element, final String newName, final Map<PsiElement, String> allRenames) {
    JSClass containingClass = JSUtils.getMemberContainingClass(element);
    if (containingClass != null) {
      allRenames.putAll(JSGetterSetterRenameUtil.getRelatedElementsInClass(element, containingClass, newName));
      if (element instanceof JSVariable){
        String name = ((JSVariable)element).getName();
        JSResolveUtil.processHierarchy(containingClass, aClass -> {
          if (!aClass.isInterface()) return true;
          final JSFunction getter = aClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.GETTER);
          if (getter != null) allRenames.put(getter, newName);
          final JSFunction setter = aClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.SETTER);
          if (setter != null) allRenames.put(setter, newName);
          return true;
        }, true);
      }
    }
  }
}
