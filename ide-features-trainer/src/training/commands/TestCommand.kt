/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import org.jetbrains.annotations.TestOnly

import java.util.concurrent.ExecutionException

class TestCommand : Command(Command.CommandType.TEST) {

  @TestOnly
  @Throws(InterruptedException::class, ExecutionException::class, BadCommandException::class)
  override fun execute(executionList: ExecutionList) {
    startNextCommand(executionList)
  }
}
