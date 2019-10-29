// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy

import com.intellij.testFramework.LightProjectDescriptor
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.plugins.groovy.LibraryLightProjectDescriptor
import org.jetbrains.plugins.groovy.RepositoryTestLibrary
import org.jetbrains.plugins.groovy.util.BaseTest
import org.jetbrains.plugins.groovy.util.LightProjectTest

import static org.jetbrains.plugins.groovy.GroovyProjectDescriptors.LIB_GROOVY_2_1

@CompileStatic
abstract class GrCucumberLightTestCase extends LightProjectTest implements BaseTest {

  private static final LightProjectDescriptor DESCRIPTOR = new LibraryLightProjectDescriptor(
    LIB_GROOVY_2_1 + new RepositoryTestLibrary('info.cukes:cucumber-core:1.0.14', 'info.cukes:cucumber-groovy:1.0.14')
  )

  @NotNull
  @Override
  LightProjectDescriptor getProjectDescriptor() {
    DESCRIPTOR
  }
}
