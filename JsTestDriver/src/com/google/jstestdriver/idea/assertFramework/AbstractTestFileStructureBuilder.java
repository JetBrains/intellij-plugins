package com.google.jstestdriver.idea.assertFramework;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTestFileStructureBuilder<T extends AbstractTestFileStructure> {

  private final Key<CachedValue<T>> myTestFileStructureRegistryKey = Key.create(
    this.getClass().getName()
  );

  @NotNull
  public abstract T buildTestFileStructure(@NotNull JSFile jsFile);

  @NotNull
  public T fetchCachedTestFileStructure(@NotNull final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(
      jsFile,
      myTestFileStructureRegistryKey,
      new CachedValueProvider<T>() {
        @Override
        public Result<T> compute() {
          T testFileStructure = buildTestFileStructure(jsFile);
          return Result.create(testFileStructure, jsFile);
        }
      },
      false
    );
  }

}
