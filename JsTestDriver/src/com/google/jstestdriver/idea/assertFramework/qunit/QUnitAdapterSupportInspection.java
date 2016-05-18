package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.assertFramework.qunit.jsSrc.QUnitAdapterSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractAddAdapterSupportInspection;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QUnitAdapterSupportInspection extends AbstractAddAdapterSupportInspection {

  public QUnitAdapterSupportInspection() {
    super(
      "QUnit",
      () -> {
        String[] relativePaths = new String[]{"equiv.js", "QUnitAdapter.js"};
        return VfsUtils.findVirtualFilesByResourceNames(QUnitAdapterSrcMarker.class, relativePaths);
      },
      "https://github.com/exnor/QUnit-to-JsTestDriver-adapter"
    );
  }

  @Override
  protected boolean isSuitableElement(@NotNull JSFile jsFile, @NotNull JSCallExpression callExpression) {
    QUnitFileStructure structure = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    String name = structure.getNameByPsiElement(callExpression);
    return name != null;
  }

}
