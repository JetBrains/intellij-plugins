package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.jstestdriver.idea.assertFramework.jasmine.jsSrc.JasmineAdapterSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class JasmineAdapterSupportProvider extends AbstractAddAdapterSupportInspection {
  @Override
  public String getAssertFrameworkName() {
    return "Jasmine";
  }

  @Override
  public List<VirtualFile> getAdapterSourceFiles() {
    String[] relativePaths = new String[] {"jasmine.js", "JasmineAdapter.js"};
    return VfsUtils.findVirtualFilesByResourceNames(JasmineAdapterSrcMarker.class, relativePaths);
  }

  @Override
  protected boolean isNeededSymbol(String methodName, JSExpression[] arguments) {
    return isJasmineSymbol(methodName, arguments);
  }

  private static boolean isJasmineSymbol(String referencedName, JSExpression[] arguments) {
    if (arguments.length == 0) {
      return false;
    }
    if ("describe".equals(referencedName) && JsPsiUtils.isStringElement(arguments[0])) {
      if (arguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(arguments[1])) {
        return true;
      }
    }
    if ("it".equals(referencedName) && JsPsiUtils.isStringElement(arguments[0])) {
      if (arguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(arguments[1])) {
        return true;
      }
    }
    return false;
  }
}
