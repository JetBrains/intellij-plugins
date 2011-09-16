package com.google.jstestdriver.idea.assertFramework;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructureManager {

  private static TestFileStructureManager ourInstance = new TestFileStructureManager();

  private static final Key<CachedValue<TestFileStructurePack>> TEST_FILE_STRUCTURE_REGISTRY_KEY = Key.create(TestFileStructurePack.class.getName());

  private final List<AbstractTestFileStructureBuilder> myBuilders = Lists.newArrayList(
    JstdTestFileStructureBuilder.getInstance(),
    QUnitFileStructureBuilder.getInstance(),
    JasmineFileStructureBuilder.getInstance()
  );

  @Nullable
  public TestFileStructurePack fetchTestFileStructurePackByJsFile(final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(jsFile, TEST_FILE_STRUCTURE_REGISTRY_KEY, new CachedValueProvider<TestFileStructurePack>() {
          @Override
          public Result<TestFileStructurePack> compute() {
            long startTime = System.currentTimeMillis();
            List<AbstractTestFileStructure> fileStructures = Lists.newArrayList();
            for (AbstractTestFileStructureBuilder builder : myBuilders) {
              AbstractTestFileStructure testFileStructure = builder.buildTestFileStructure(jsFile);
              fileStructures.add(testFileStructure);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Taken time is " + (endTime - startTime) + " ms");
            return Result.create(new TestFileStructurePack(fileStructures), jsFile);
          }
        }, false);
  }

  public static TestFileStructureManager getInstance() {
    return ourInstance;
  }
}
