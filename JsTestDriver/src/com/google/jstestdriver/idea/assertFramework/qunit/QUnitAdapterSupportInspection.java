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
    super("QUnit", new Provider<List<VirtualFile>>() {
      @Override
      public List<VirtualFile> get() {
        String[] relativePaths = new String[]{"equiv.js", "QUnitAdapter.js"};
        return VfsUtils.findVirtualFilesByResourceNames(QUnitAdapterSrcMarker.class, relativePaths);
      }
    });
  }

  @Override
  protected boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] methodArguments) {
    if (methodArguments.length == 0) {
      return false;
    }
    if ("module".equals(methodName) && JsPsiUtils.isStringElement(methodArguments[0])) {
      if (methodArguments.length == 1) {
        return true;
      }
      if (methodArguments.length == 2 && JsPsiUtils.isObjectElement(methodArguments[1])) {
        return true;
      }
    }
    if ("test".equals(methodName) && JsPsiUtils.isStringElement(methodArguments[0])) {
      if (methodArguments.length == 1) {
        return true;
      }
      if (methodArguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(methodArguments[1])) {
        return true;
      }
      if (methodArguments.length == 3 && JsPsiUtils.isNumberElement(methodArguments[1]) && JsPsiUtils.isFunctionExpressionElement(methodArguments[2])) {
        return true;
      }
    }
    return false;
  }

}
