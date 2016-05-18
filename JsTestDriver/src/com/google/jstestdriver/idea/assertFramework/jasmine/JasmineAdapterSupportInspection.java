package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.jstestdriver.idea.assertFramework.jasmine.jsSrc.JasmineAdapterSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JasmineAdapterSupportInspection extends AbstractAddAdapterSupportInspection {

  public JasmineAdapterSupportInspection() {
    super(
      "Jasmine",
      () -> {
        String[] relativePaths = new String[]{"jasmine-1.1.0.js", "JasmineAdapter-1.1.2.js"};
        return VfsUtils.findVirtualFilesByResourceNames(JasmineAdapterSrcMarker.class, relativePaths);
      },
      "https://github.com/ibolmo/jasmine-jstd-adapter"
    );
  }

  @Override
  protected boolean isSuitableElement(@NotNull JSFile jsFile, @NotNull JSCallExpression callExpression) {
    JasmineFileStructure structure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    return structure.containsCallExpression(callExpression);
  }

}
