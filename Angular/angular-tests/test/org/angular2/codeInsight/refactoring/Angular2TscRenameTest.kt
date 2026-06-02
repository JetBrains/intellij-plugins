// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_1_4
import org.angular2.Angular2TestModule.NGRX_SIGNALS_20_1_0
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoFork
class Angular2TscRenameTest : Angular2TestCase("refactoring/rename") {

  @Test
  fun testSignalStore() =
    doSymbolRename("signalStore.ts", "newName", ANGULAR_CORE_20_1_4, NGRX_SIGNALS_20_1_0,
                   dir = false, configurators = listOf(Angular2TsConfigFile()))

}
