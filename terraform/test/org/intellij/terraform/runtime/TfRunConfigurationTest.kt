// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.util.JDOMUtil
import org.intellij.terraform.install.TfToolType
import java.nio.file.Paths

internal class TfRunConfigurationTest : BaseRunConfigurationTest() {

  fun testMigratingRunConfigFrom242() {
    val runManager = RunManager.getInstance(project) as? RunManagerImpl
    if (runManager == null) {
      fail("Could not get instance of RunManagerImpl")
      return
    }

    val oldStateElement = JDOMUtil.load(Paths.get("${getTestDataPath()}/run-config-242.xml"))
    runManager.loadState(oldStateElement)

    val tfConfig = runManager.allSettings.firstOrNull()?.configuration as? TfRunConfiguration
    assertNotNull(tfConfig)
    assertEquals("Plan directory 309488", tfConfig?.name)
    assertEquals("Terraform plan", tfConfig?.factory?.id)

    val configurationType = tfConfig?.type as? TfConfigurationType
    assertNotNull(configurationType)
    assertEquals(TfConfigurationType.TF_RUN_CONFIGURATION_ID, configurationType?.id)
  }


  fun testGlobalOptionsEditor() {
    val toolType = TfToolType.TERRAFORM
    val initFactory = tfRunConfigurationType(toolType).initFactory

    val runManager = RunManager.getInstance(project)
    val settings = runManager.createConfiguration("Test 'global option'", initFactory)

    val configuration = settings.configuration as? TfRunConfiguration
    assertNotNull(configuration)
    assertEmpty(configuration?.globalOptions)
    assertEquals("init", configuration?.programParameters)

    configuration?.globalOptions = " -chdir=/some_dir/some_sub_dir/test.txt   "
    configuration?.programArguments = "   -auto-approve -input=false"
    configuration?.passGlobalOptions = true
    assertEquals("-chdir=/some_dir/some_sub_dir/test.txt init -auto-approve -input=false", configuration?.programParameters)
  }
}