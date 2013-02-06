/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.cucumber.java.steps;

import org.easymock.EasyMock;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.jetbrains.plugins.cucumber.java.steps.ConstantCucumberVersion.cucumber_1_0;
import static org.jetbrains.plugins.cucumber.java.steps.ConstantCucumberVersion.cucumber_1_1;
import static org.junit.Assert.assertThat;

public class AnnotationPackageProviderTest {

  @Test
  public void returnProperPackageForEnglishFeatureFileBeforeVersion_1_1() throws Exception {
    assertThat(annotationPackageFor(cucumber_1_0(), "en"), is("cucumber.annotation.en"));
  }

  @Test
  public void returnProperPackageForEnglishFeatureFileAfterVersion_1_1() throws Exception {
    assertThat(annotationPackageFor(cucumber_1_1(), "en"), is("cucumber.api.java.en"));
  }

  @Test
  public void returnProperPackageForGermanFeatureFileBeforeVersion_1_1() throws Exception {
    assertThat(annotationPackageFor(cucumber_1_0(), "de"), is("cucumber.annotation.de"));
  }

  @Test
  public void returnProperPackageForGermanFeatureFileAfterVersion_1_1() throws Exception {
    assertThat(annotationPackageFor(cucumber_1_1(), "de"), is("cucumber.api.java.de"));
  }

  @Test
  public void escapeConvertDashInLocaleStringToAnUnderscore() throws Exception {
    assertThat(annotationPackageFor(cucumber_1_0(), "en-pirate"), is("cucumber.annotation.en_pirate"));
  }

  private static String annotationPackageFor(CucumberVersionProvider versionProvider, String language) {
    AnnotationPackageProvider provider = new AnnotationPackageProvider(versionProvider);
    return provider.getAnnotationPackageFor(createStepIn(language));
  }

  private static GherkinStep createStepIn(String language) {
    GherkinFile featureFile = EasyMock.createNiceMock(GherkinFile.class);
    expect(featureFile.getLocaleLanguage()).andReturn(language).anyTimes();

    GherkinStep step = EasyMock.createNiceMock(GherkinStep.class);
    expect(step.getContainingFile()).andReturn(featureFile).anyTimes();

    replay(featureFile, step);
    return step;
  }

}