// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  Angular2BananaFixTest::class,
  Angular2CodeCompletionTest::class,
  Angular2EntitiesRenameTest::class,
  Angular2HtmlCodeCompletionTest::class,
)
class Angular2ReSharperTestSuite
