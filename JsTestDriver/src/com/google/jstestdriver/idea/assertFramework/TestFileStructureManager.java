package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Sergey Simonchik
 */
public abstract class TestFileStructureManager {

  private static final Key<CachedValue<TestFileStructurePack>> TEST_FILE_STRUCTURE_REGISTRY_KEY = Key.create(
    TestFileStructurePack.class.getName()
  );

  @Nullable
  public static TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile) {
    final Provider provider = ServiceManager.getService(TestFileStructureProviderImpl.class);
    if (provider == null) {
      return null;
    }
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(
      jsFile,
      TEST_FILE_STRUCTURE_REGISTRY_KEY,
      new CachedValueProvider<TestFileStructurePack>() {
        @Override
        public Result<TestFileStructurePack> compute() {
          TestFileStructurePack pack = provider.createTestFileStructurePack(jsFile);
          return Result.create(pack, jsFile);
        }
      },
      false
    );
  }

  @Nullable
  public static TestFileStructurePack createTestFileStructurePackByJsFile(final JSFile jsFile, final Collection<String> symbolNames) {
    final Provider provider = ServiceManager.getService(TestFileStructureProviderImpl.class);
    if (provider == null) {
      return null;
    }
    return provider.createTestFileStructurePack(jsFile);
  }

  interface Provider {
    TestFileStructurePack createTestFileStructurePack(@NotNull JSFile jsFile);
  }

}
