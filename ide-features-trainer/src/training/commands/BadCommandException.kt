/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

class BadCommandException : Exception {

  constructor(command: Command) : super("exception in command " + command.commandType.toString())
  constructor(s: String) : super(s)
}
