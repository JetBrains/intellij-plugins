// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run

import com.intellij.execution.application.JvmMainMethodRunConfigurationOptions
import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.annotations.Nls

open class CucumberJavaConfigurationOptions: JvmMainMethodRunConfigurationOptions() {
  @get:OptionTag("GLUE")
  open var glue: String? by string()

  @get:OptionTag("FILE_PATH")
  open var filePath: String? by string()

  @get:OptionTag("NAME_FILTER")
  open var nameFilter: String? by string()

  @get:OptionTag("SUGGESTED_NAME")
  @get:Nls
  open var suggestedName: String? by string()

  @get:OptionTag("CUCUMBER_CORE_VERSION")
  open var cucumberCoreVersion: String? by string()
}