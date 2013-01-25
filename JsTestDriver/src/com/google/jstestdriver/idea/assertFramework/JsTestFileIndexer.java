package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.index.JSFileIndexerFactory;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;

/**
 * User: ksafonov
 */
public class JsTestFileIndexer extends JSElementVisitor {

  public static class Factory extends JSFileIndexerFactory {

    @Override
    protected int getVersion() {
      return 3;
    }

    @Override
    public JSElementVisitor createVisitor(final JSNamespace topLevelNs,
                                          final JSSymbolUtil.JavaScriptSymbolProcessorEx indexer,
                                          final PsiFile file) {
      JSFile jsFile = ObjectUtils.tryCast(file, JSFile.class);
      if (jsFile != null) {
        TestFileStructurePack pack = TestFileStructureManager.createTestFileStructurePackByJsFile(jsFile);
        if (pack != null) {
          if (!pack.isEmpty()) {
            indexer.setIsTestFile();
          }
        }
      }
      return null;
    }
  }

}
