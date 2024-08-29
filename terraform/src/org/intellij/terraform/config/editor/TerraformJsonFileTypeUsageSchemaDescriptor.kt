// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.editor

import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageSchemaDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TerraformJsonFileTypeUsageSchemaDescriptor : FileTypeUsageSchemaDescriptor {
  override fun describes(project: Project, file: VirtualFile): Boolean {
    return file.name.endsWith(".tf.json")
  }
}

class OpenTofuJsonFileTypeUsageSchemaDescriptor : FileTypeUsageSchemaDescriptor {
  override fun describes(project: Project, file: VirtualFile): Boolean {
    return file.name.endsWith(".tofu.json")
  }
}