// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.testFramework.LightPlatformTestCase
import junit.framework.TestCase

class BlueprintListTest : LightPlatformTestCase() {
  fun testList() {
    val output = DEFAULT_OUTPUT
    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = listOf("class", "component", "module", "service")
    val existingBlueprints = blueprints.filter { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(requiredBlueprints.size, existingBlueprints.size)
    TestCase.assertEquals(listOf("--flat", "--inline-template", "--inline-style", "--prefix", "--spec", "--view-encapsulation",
                                 "--change-detection", "--skip-import", "--module", "--export", "--app"),
                          existingBlueprints[1].options.map { "--" + it.name })

    val blacklistedBlueprints = listOf("aliases:")
    val nonBlueprints = blueprints.filter { blacklistedBlueprints.contains(it.name) }
    TestCase.assertEquals(0, nonBlueprints.size)
  }

  fun testNewList() {
    val output = "Available schematics:\n" +
                 "    application\n" +
                 "    class\n" +
                 "    component\n" +
                 "    directive\n" +
                 "    enum\n" +
                 "    guard\n" +
                 "    interface\n" +
                 "    module\n" +
                 "    pipe\n" +
                 "    service\n"
    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = listOf("class", "component", "module", "service")
    val existingBlueprints = blueprints.filter { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(requiredBlueprints.size, existingBlueprints.size)
  }

}
