package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.inject.Provider;
import com.google.jstestdriver.idea.assertFramework.jasmine.jsSrc.JasmineAdapterSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
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
  protected boolean isSuitableElement(@NotNull JSFile jsFile, @NotNull JSCallExpression callExpression) {
    JasmineFileStructure structure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    return structure.containsCallExpression(callExpression);
  }

}
