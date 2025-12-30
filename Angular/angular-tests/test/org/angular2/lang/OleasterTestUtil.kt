// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Ref
import com.intellij.testFramework.LightPlatformTestCase
import com.mscharhag.oleaster.runner.StaticRunnerSupport

object OleasterTestUtil {
  @JvmStatic
  fun bootstrapLightPlatform() {
    val testCase = Ref<OleasterLightPlatformTestCase>()
    StaticRunnerSupport.before { testCase.set(OleasterLightPlatformTestCase()) }
    StaticRunnerSupport.after { testCase.get().tearDown() }
  }

  @Suppress("JUnitMalformedDeclaration")
  private class OleasterLightPlatformTestCase : LightPlatformTestCase() {
    init {
      setUp()
    }

    override fun shouldContainTempFiles(): Boolean {
      return false
    }

    override fun getName(): String {
      return "testOleaster"
    }

    public override fun tearDown() {
      ApplicationManager.getApplication().invokeAndWait {
        try {
          super.tearDown()
        }
        catch (e: Exception) {
          e.printStackTrace()
          throw RuntimeException(e)
        }
      }
    }
  }
}
