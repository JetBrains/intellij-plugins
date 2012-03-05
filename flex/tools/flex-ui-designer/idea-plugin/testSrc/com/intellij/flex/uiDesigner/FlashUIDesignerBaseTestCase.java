package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.ModuleTestCase;
import com.intellij.util.TripleFunction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class FlashUIDesignerBaseTestCase extends ModuleTestCase {
  protected TripleFunction<ModifiableRootModel, VirtualFile, List<String>, Void> moduleInitializer;

  @Override
  protected boolean isRunInWriteAction() {
    return false;
  }

  protected static String getFudHome() {
    return DebugPathManager.getFudHome();
  }

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(),
                             DebugPathManager.getIdeaHome() + "/plugins/JavaScriptLanguage/testData/flex_highlighting/MockGumboSdk", false);
  }

  protected VirtualFile configureByFile(final String filepath) throws Exception {
    return configureByFile(DesignerTests.getVFile(filepath));
  }

  protected VirtualFile configureByFile(final VirtualFile vFile) throws Exception {
    return configureByFiles(null, new VirtualFile[]{vFile}, null)[0];
  }

  protected VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot, VirtualFile[] files, @Nullable VirtualFile[] auxiliaryFiles) throws Exception {
    return DesignerTests.configureByFiles(rawProjectRoot, files, auxiliaryFiles, myModule, moduleInitializer);
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}