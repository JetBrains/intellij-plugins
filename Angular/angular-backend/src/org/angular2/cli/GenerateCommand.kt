// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

class GenerateCommand {
  var kind: GenerateCommandKind = GenerateCommandKind.WRITE
  var path: String = ""
  var to: String? = null
  var content: String? = null

  override fun toString(): String {
    return "$kind $path"
  }
}

enum class GenerateCommandKind {
  WRITE, DELETE, RENAME,
}