package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdTestFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructureProviderImpl implements TestFileStructureManager.Provider {

  private static final Logger LOG = Logger.getInstance(TestFileStructureProviderImpl.class);

  private final List<AbstractTestFileStructureBuilder<? extends AbstractTestFileStructure>> myBuilders = ContainerUtilRt.newArrayList(
    JstdTestFileStructureBuilder.getInstance(),
    QUnitFileStructureBuilder.getInstance(),
    JasmineFileStructureBuilder.getInstance()
  );

  @NotNull
  public TestFileStructurePack createTestFileStructurePack(@NotNull JSFile jsFile) {
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
