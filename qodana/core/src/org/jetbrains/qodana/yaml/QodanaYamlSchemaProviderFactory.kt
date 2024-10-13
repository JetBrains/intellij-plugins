package org.jetbrains.qodana.yaml

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_CONFIG_FILES

const val QODANA_YAML_SCHEMA_RESOURCE = "/schemas/qodana-yaml-schema.json"

internal class QodanaYamlSchemaProviderFactory : JsonSchemaProviderFactory {
  override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
    return listOf(QodanaYamlSchemaProvider())
  }

  class QodanaYamlSchemaProvider : JsonSchemaFileProvider {
    override fun getName(): String = QodanaBundle.message("qodana.yaml.configuration")

    override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

    override fun isAvailable(file: VirtualFile): Boolean =
      QODANA_CONFIG_FILES.any { file.name.equals(it, true) }

    override fun getSchemaFile(): VirtualFile? =
      requireNotNull(javaClass.getResource(QODANA_YAML_SCHEMA_RESOURCE))
        .let(VfsUtil::findFileByURL)
  }
}