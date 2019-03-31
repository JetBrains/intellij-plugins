package com.intellij.aws.cloudformation

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.remote.JsonSchemaCatalogExclusion

class CloudFormationJsonCatalogExclusion : JsonSchemaCatalogExclusion {
  override fun isExcluded(file: VirtualFile) =
      file.fileType === YamlCloudFormationFileType.INSTANCE ||
          file.fileType === JsonCloudFormationFileType.INSTANCE
}