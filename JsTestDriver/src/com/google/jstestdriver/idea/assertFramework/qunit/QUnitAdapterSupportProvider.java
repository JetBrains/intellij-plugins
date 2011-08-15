package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.assertFramework.support.AbstractAdapterSupportProvider;
import com.google.jstestdriver.idea.assertFramework.qunit.js_src.QUnitAdapterSrcMarker;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class QUnitAdapterSupportProvider extends AbstractAdapterSupportProvider {

  @Override
  public String getAssertFrameworkName() {
    return "QUnit";
  }

  @Override
  public List<VirtualFile> getAdapterSourceFiles() {
    String[] relativePaths = new String[] {"equiv.js", "QUnitAdapter.js"};
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
    if ("module".equals(referencedName) && isStringElement(arguments[0])) {
      if (arguments.length == 1) {
        return true;
      }
      if (arguments.length == 2 && isObjectElement(arguments[1])) {
        return true;
      }
    }
    if ("test".equals(referencedName) && isStringElement(arguments[0])) {
      if (arguments.length == 1) {
        return true;
      }
      if (arguments.length == 2 && isFunctionElement(arguments[1])) {
        return true;
      }
      if (arguments.length == 3 && isNumberElement(arguments[1]) && isFunctionElement(arguments[2])) {
        return true;
      }
    }
    return false;
  }

  private static boolean isStringElement(JSExpression jsExpression) {
    return JsPsiUtils.extractStringValue(jsExpression) != null;
  }

  private static boolean isObjectElement(JSExpression jsExpression) {
    return JsPsiUtils.extractObjectLiteralExpression(jsExpression) != null;
  }

  private static boolean isFunctionElement(JSExpression jsExpression) {
    return JsPsiUtils.extractFunctionExpression(jsExpression) != null;
  }

  private static boolean isNumberElement(JSExpression jsExpression) {
    return JsPsiUtils.extractNumberLiteral(jsExpression) != null;
  }

}
