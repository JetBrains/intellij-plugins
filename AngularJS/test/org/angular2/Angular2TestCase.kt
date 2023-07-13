// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.web.WebFrameworkTestCase
import org.angularjs.AngularTestUtil

abstract class Angular2TestCase(override val testCasePath: String) : WebFrameworkTestCase() {

  override val testDataRoot: String
    get() = AngularTestUtil.getBaseTestDataPath()

  override val defaultDependencies: Map<String, String> =
    mapOf("@angular/core" to "*")

  override val defaultExtension: String
    get() = "ts"

}