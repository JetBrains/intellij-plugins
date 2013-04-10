package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructureManager {

  private static final Logger LOG = Logger.getInstance(TestFileStructureManager.class);

  private static final Key<CachedValue<TestFileStructurePack>> TEST_FILE_STRUCTURE_REGISTRY_KEY = Key.create(
    TestFileStructurePack.class.getName()
  );

  private static final List<AbstractTestFileStructureBuilder<? extends AbstractTestFileStructure>> myBuilders = ContainerUtil.newArrayList(
    JstdTestFileStructureBuilder.getInstance(),
    QUnitFileStructureBuilder.getInstance(),
    JasmineFileStructureBuilder.getInstance()
  );

  private TestFileStructureManager() {}

  @Nullable
  public static TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(
      jsFile,
      TEST_FILE_STRUCTURE_REGISTRY_KEY,
      new CachedValueProvider<TestFileStructurePack>() {
        @Override
        public Result<TestFileStructurePack> compute() {
          TestFileStructurePack pack = createTestFileStructurePack(jsFile);
          return Result.create(pack, jsFile);
        }
      },
      false
    );
  }

  @NotNull
  private static TestFileStructurePack createTestFileStructurePack(@NotNull JSFile jsFile) {
    long startTimeNano = System.nanoTime();
    List<AbstractTestFileStructure> fileStructures = new ArrayList<AbstractTestFileStructure>(myBuilders.size());
    for (AbstractTestFileStructureBuilder<? extends AbstractTestFileStructure> builder : myBuilders) {
      AbstractTestFileStructure testFileStructure = builder.fetchCachedTestFileStructure(jsFile);
      fileStructures.add(testFileStructure);
    }
    long durationNano = System.nanoTime() - startTimeNano;
    if (durationNano > 50 * 1000000) {
      // more than 50 ms
      String message = String.format("JsTestDriver: Creating TestFileStructurePack for %s takes %.2f ms",
                                     jsFile.getName(),
                                     durationNano / 1000000.0);
      LOG.info(message);
    }
    return new TestFileStructurePack(fileStructures);
  }

}
