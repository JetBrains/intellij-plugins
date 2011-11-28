package com.google.jstestdriver.idea.assertFramework;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructureProviderImpl implements TestFileStructureManager.Provider {

  private static final Logger LOG = Logger.getInstance(TestFileStructureProviderImpl.class);

  private static final Key<CachedValue<TestFileStructurePack>> TEST_FILE_STRUCTURE_REGISTRY_KEY = Key.create(
    TestFileStructurePack.class.getName()
  );

  private final List<AbstractTestFileStructureBuilder> myBuilders = Lists.newArrayList(
    JstdTestFileStructureBuilder.getInstance(),
    QUnitFileStructureBuilder.getInstance(),
    JasmineFileStructureBuilder.getInstance()
  );

  @Nullable
  public TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(jsFile, TEST_FILE_STRUCTURE_REGISTRY_KEY,
                                              new CachedValueProvider<TestFileStructurePack>() {
          @Override
          public Result<TestFileStructurePack> compute() {
            long startTimeNano = System.nanoTime();
            List<AbstractTestFileStructure> fileStructures = Lists.newArrayList();
            for (AbstractTestFileStructureBuilder builder : myBuilders) {
              AbstractTestFileStructure testFileStructure = builder.buildTestFileStructure(jsFile);
              fileStructures.add(testFileStructure);
            }
            long endTimeNano = System.nanoTime();
            String message = String.format("JsTestDriver: Creating TestFileStructurePack for %s takes %.2f ms",
                                           jsFile.getName(),
                                           (endTimeNano - startTimeNano) / 1000000.0);
            LOG.info(message);
            return Result.create(new TestFileStructurePack(fileStructures), jsFile);
          }
        }, false);
  }

}
