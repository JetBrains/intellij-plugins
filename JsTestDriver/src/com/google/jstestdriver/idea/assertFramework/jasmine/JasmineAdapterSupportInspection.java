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
    super(
      "Jasmine",
      new Provider<List<VirtualFile>>() {
        @Override
        public List<VirtualFile> get() {
          String[] relativePaths = new String[]{"jasmine-1.1.0.js", "JasmineAdapter-1.1.2.js"};
          return VfsUtils.findVirtualFilesByResourceNames(JasmineAdapterSrcMarker.class, relativePaths);
        }
      },
      "https://github.com/ibolmo/jasmine-jstd-adapter"
    );
  }

  @Override
  protected boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] arguments) {
    if (arguments.length != 2) {
      return false;
    }
    if (JasmineFileStructureBuilder.DESCRIBE_NAME.equals(methodName)) {
      return isStringAndFunction(arguments);
    }
    else if (JasmineFileStructureBuilder.IT_NAME.equals(methodName)) {
      return isStringAndFunction(arguments);
    }
    return false;
  }

  private static boolean isStringAndFunction(@NotNull JSExpression[] args) {
    return JsPsiUtils.isStringElement(args[0]) && JsPsiUtils.isFunctionExpressionElement(args[1]);
  }
}
