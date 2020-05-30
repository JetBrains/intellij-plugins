/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.aws.cloudformation

import com.intellij.internal.statistic.fileTypes.FileTypeStatisticProvider
import com.intellij.json.JsonFileType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiManager
import org.jetbrains.yaml.YAMLFileType

class CloudFormationFileTypeStatisticProvider : FileTypeStatisticProvider {
  private val LOG = logger<CloudFormationFileTypeStatisticProvider>()

  override fun getPluginId(): String = "AWSCloudFormation"

  override fun accept(event: EditorFactoryEvent, fileType: FileType): Boolean {
    if (fileType === YamlCloudFormationFileType.INSTANCE ||
        fileType === JsonCloudFormationFileType.INSTANCE) {
      return true
    }

    return checkYamlOrJson(fileType, event)
  }

  private fun checkYamlOrJson(fileType: FileType,
                              event: EditorFactoryEvent): Boolean {
    if (!(fileType === YAMLFileType.YML || fileType === JsonFileType.INSTANCE)) return false

    val document = event.editor.document
    val file = FileDocumentManager.getInstance().getFile(document) ?: return false
    val project = event.editor.project ?: return false

    return try {
      val psiFile = PsiManager.getInstance(project).findFile(file)
      psiFile != null && CloudFormationPsiUtils.isCloudFormationFile(psiFile)
    }
    catch (t: Throwable) {
      LOG.debug("Unable to detect whether file ${file.path} is CloudFormation file")
      false
    }
  }
}