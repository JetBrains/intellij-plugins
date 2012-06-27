package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.inject.Provider;
import com.google.jstestdriver.idea.assertFramework.jasmine.jsSrc.JasmineAdapterSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JasmineAdapterSupportInspection extends AbstractAddAdapterSupportInspection {

  public JasmineAdapterSupportInspection() {
    super("Jasmine", new Provider<List<VirtualFile>>() {
      @Override
      public List<VirtualFile> get() {
        String[] relativePaths = new String[] {"jasmine-1.1.0.js", "JasmineAdapter-1.1.2.js"};
        return VfsUtils.findVirtualFilesByResourceNames(JasmineAdapterSrcMarker.class, relativePaths);
      }
    });
  }

  @Override
  protected boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] methodArguments) {
    if (methodArguments.length == 0) {
      return false;
    }
    if ("describe".equals(methodName) && JsPsiUtils.isStringElement(methodArguments[0])) {
      if (methodArguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(methodArguments[1])) {
        return true;
      }
    }
    if ("it".equals(methodName) && JsPsiUtils.isStringElement(methodArguments[0])) {
      if (methodArguments.length == 2 && JsPsiUtils.isFunctionExpressionElement(methodArguments[1])) {
        return true;
      }
    }
    return false;
  }
}
