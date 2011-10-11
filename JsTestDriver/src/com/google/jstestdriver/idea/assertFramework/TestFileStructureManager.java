package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * @author Sergey Simonchik
 */
public abstract class TestFileStructureManager {

  private static final Provider EMPTY_PROVIDER = new Provider() {
    @Override
    public TestFileStructurePack fetchTestFileStructurePackByJsFile(JSFile jsFile) {
      return new TestFileStructurePack(Collections.<AbstractTestFileStructure>emptyList());
    }
  };

  @Nullable
  public static TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile) {
    Provider provider = ServiceManager.getService(TestFileStructureProviderImpl.class);
    if (provider == null) {
      provider = EMPTY_PROVIDER;
    }
    return provider.fetchTestFileStructurePackByJsFile(jsFile);
  }

  interface Provider {
    TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile);
  }
}
