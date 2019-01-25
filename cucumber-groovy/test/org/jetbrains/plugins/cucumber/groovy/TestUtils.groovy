// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.plugins.groovy.LibraryLightProjectDescriptor
import org.jetbrains.plugins.groovy.RepositoryTestLibrary

import static org.jetbrains.plugins.groovy.GroovyProjectDescriptors.LIB_GROOVY_2_1

/**
 * @author Max Medvedev
 */
class TestUtils {
  public static final LightProjectDescriptor DESCRIPTOR = new LibraryLightProjectDescriptor(
    LIB_GROOVY_2_1 + new RepositoryTestLibrary('info.cukes:cucumber-core:1.0.14', 'info.cukes:cucumber-groovy:1.0.14')
  )

  private TestUtils() {}

  static String getAbsoluteTestDataPath() {
    return "$absolutePluginPath/testData/"
  }

  private static String getAbsolutePluginPath() {
    return FileUtil.toSystemIndependentName(new File(PathManager.homePath, "contrib/cucumber-groovy/").path)
  }
}
