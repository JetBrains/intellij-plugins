package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FlexQualifiedNameLocationProvider implements SMTestLocator {
  private static final String PROTOCOL_ID = "flex_qn";

  public static final FlexQualifiedNameLocationProvider INSTANCE = new FlexQualifiedNameLocationProvider();

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
    if (PROTOCOL_ID.equals(protocol)) {
      JSElement element = findElement(path, project);
      if (element != null) {
        return Collections.singletonList(new PsiLocation<PsiElement>(project, element));
      }
    }

    return Collections.emptyList();
  }

  @Nullable
  private static JSElement findElement(String link, Project project) {
    String moduleName = link.substring(0, link.indexOf(":"));
    Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
    link = link.substring(link.indexOf(":") + 1);

    final JavaScriptIndex index = JavaScriptIndex.getInstance(project);
    PsiElement element = ActionScriptClassResolver.findClassByQName(link, index, module);
    if (element instanceof JSClass) {
      return (JSElement)element;
    }

    if (element == null && link.contains(".") && link.endsWith("()")) {
      String qname = link.substring(0, link.lastIndexOf('.'));
      element = ActionScriptClassResolver.findClassByQName(qname, index, module);
      if (element instanceof JSClass) {
        String methodName = link.substring(link.lastIndexOf('.') + 1, link.length() - 2);
        return ((JSClass)element).findFunctionByNameAndKind(methodName, JSFunction.FunctionKind.SIMPLE);
      }
    }
    return null;
  }
}
