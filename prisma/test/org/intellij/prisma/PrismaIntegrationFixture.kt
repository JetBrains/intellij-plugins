// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.lang.javascript.modules.JSNodeInterpreterFixture
import com.intellij.lang.javascript.modules.JSNodeInterpreterFixture.Companion.jsNodeInterpreterFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.testFramework.junit5.fixture.TestFixture

/**
 * Creates a [JSNodeInterpreterFixture] with Node.js 22 on a new project, for the test data in [basePath] under the repository root.
 * Declare the result as an instance property, so that each test gets its own project.
 */
internal fun prismaIntegrationFixture(basePath: String = ""): TestFixture<JSNodeInterpreterFixture> =
  jsNodeInterpreterFixture(JSTempDirFixtureOptions.forBasePath(basePath).withNodeVersion(NodeJsAppRule.LATEST_22))
