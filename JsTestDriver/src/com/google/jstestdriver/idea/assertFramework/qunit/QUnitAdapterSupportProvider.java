package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.assertFramework.qunit.jsSrc.QUnitAdapterSrcMarker;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class QUnitAdapterSupportProvider extends AbstractAddAdapterSupportInspection {

  @Override
  public String getAssertFrameworkName() {
    return "QUnit";
  }

  @Override
  public List<VirtualFile> getAdapterSourceFiles() {
    String[] relativePaths = new String[]{"equiv.js", "QUnitAdapter.js"};
    return VfsUtils.findVirtualFilesByResourceNames(QUnitAdapterSrcMarker.class, relativePaths);
  }

  @Override
  protected boolean isNeededSymbol(String methodName, JSExpression[] arguments) {
    return isQUnitSymbol(methodName, arguments);
  }

  private static boolean isQUnitSymbol(String referencedName, JSExpression[] arguments) {
    if (arguments.length == 0) {
      return false;
    }
    if ("module".equals(referencedName) && JsPsiUtils.isStringElement(arguments[0])) {
      if (arguments.length == 1) {
        return true;
      }
      if (arguments.length == 2 && JsPsiUtils.isObjectElement(arguments[1])) {
        return true;
      }
    }
    if ("test".equals(referencedName) && JsPsiUtils.isStringElement(arguments[0])) {
      if (arguments.length == 1) {
        return true;
      }
      if (arguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(arguments[1])) {
        return true;
      }
      if (arguments.length == 3 && JsPsiUtils.isNumberElement(arguments[1]) && JsPsiUtils.isFunctionExpressionElement(arguments[2])) {
        return true;
      }
    }
    return false;
  }
}
