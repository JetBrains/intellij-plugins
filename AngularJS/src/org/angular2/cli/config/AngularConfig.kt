// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.vfs.VirtualFile

interface AngularConfig {

  val projects: List<AngularProject>

  val defaultProject: AngularProject?

  val file: VirtualFile

  fun getProject(context: VirtualFile): AngularProject?

}
