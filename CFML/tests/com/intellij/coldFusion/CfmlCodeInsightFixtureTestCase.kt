// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class CfmlCodeInsightFixtureTestCase : BasePlatformTestCase() {
  final override fun getTestDataPath() = CfmlTestUtil.BASE_TEST_DATA_PATH + basePath
}
