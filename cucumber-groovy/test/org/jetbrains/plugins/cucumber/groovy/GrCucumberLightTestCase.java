// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.LibraryLightProjectDescriptor;
import org.jetbrains.plugins.groovy.RepositoryTestLibrary;
import org.jetbrains.plugins.groovy.util.BaseTest;
import org.jetbrains.plugins.groovy.util.LightProjectTest;

import static org.jetbrains.plugins.groovy.GroovyProjectDescriptors.LIB_GROOVY_2_1;

public abstract class GrCucumberLightTestCase extends LightProjectTest implements BaseTest {
  private static final LightProjectDescriptor DESCRIPTOR = new LibraryLightProjectDescriptor(
    LIB_GROOVY_2_1.plus(new RepositoryTestLibrary("info.cukes:cucumber-core:1.0.14", "info.cukes:cucumber-groovy:1.0.14")));

  @NotNull
  @Override
  public LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }
}
