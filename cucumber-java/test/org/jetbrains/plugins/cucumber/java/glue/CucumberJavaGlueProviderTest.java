// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.glue;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.CollectConsumer;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.run.CucumberJavaAllFeaturesInFolderGlueProvider;
import org.jetbrains.plugins.cucumber.java.run.CucumberJavaFeatureGlueProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.Arrays;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil.RELATED_TEST_DATA_PATH;

public class CucumberJavaGlueProviderTest extends CucumberCodeInsightTestCase {
  public void testFeatureGlueCalculation() {
    init();

    GherkinFile featureFile = (GherkinFile)myFixture.getFile();
    CollectConsumer<String> consumer = new CollectConsumer<>();
    new CucumberJavaFeatureGlueProvider(featureFile).calculateGlue(consumer);

    assertContainsElements(consumer.getResult(), Arrays.asList("cucumber.examples.java.calculator", "test.cucumber.hooks", "test.cucumber.types"));
  }

  public void testAllFeaturesInFolderGlueCalculation() {
    init();

    PsiDirectory featuresFolder = myFixture.getFile().getParent();
    CollectConsumer<String> consumer = new CollectConsumer<>();
    new CucumberJavaAllFeaturesInFolderGlueProvider(featuresFolder).calculateGlue(consumer);

    assertContainsElements(consumer.getResult(), Arrays.asList("cucumber.examples.java.calculator", "info.cucumber", "test.cucumber.hooks", "test.cucumber.types"));
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + RELATED_TEST_DATA_PATH;
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber5ProjectDescriptor();
  }

  private void init() {
    myFixture.copyDirectoryToProject("glue", "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("test.feature"));
  }
}
