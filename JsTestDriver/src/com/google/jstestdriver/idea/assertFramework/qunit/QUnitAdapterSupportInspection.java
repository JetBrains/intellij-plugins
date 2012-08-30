package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.inject.Provider;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.assertFramework.qunit.jsSrc.QUnitAdapterSrcMarker;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QUnitAdapterSupportInspection extends AbstractAddAdapterSupportInspection {

  public QUnitAdapterSupportInspection() {
    super(
      "QUnit",
      new Provider<List<VirtualFile>>() {
        @Override
        public List<VirtualFile> get() {
          String[] relativePaths = new String[]{"equiv.js", "QUnitAdapter.js"};
          return VfsUtils.findVirtualFilesByResourceNames(QUnitAdapterSrcMarker.class, relativePaths);
        }
      },
      "https://github.com/exnor/QUnit-to-JsTestDriver-adapter"
    );
  }

  @Override
  protected boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] arguments) {
    if (arguments.length == 0) {
      return false;
    }
    if (QUnitFileStructureBuilder.MODULE_NAME.equals(methodName)) {
      if (arguments.length == 1) {
        return JsPsiUtils.isStringElement(arguments[0]);
      }
      else if (arguments.length == 2) {
        return isStringAndFunction(arguments);
      }
    }
    else if (QUnitFileStructureBuilder.TEST_NAME.equals(methodName) ||
             QUnitFileStructureBuilder.ASYNC_TEST_NAME.equals(methodName)) {
      if (arguments.length == 2) {
        return isStringAndFunction(arguments);
      }
      else if (arguments.length == 3) {
        return JsPsiUtils.isStringElement(arguments[0]) &&
               JsPsiUtils.isNumberElement(arguments[1]) &&
               JsPsiUtils.isFunctionExpressionElement(arguments[2]);
      }
    }
    return false;
  }

  private static boolean isStringAndFunction(@NotNull JSExpression[] args) {
    return JsPsiUtils.isStringElement(args[0]) && JsPsiUtils.isFunctionExpressionElement(args[1]);
  }

}
