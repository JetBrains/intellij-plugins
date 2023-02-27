/*
 * Copyright 2000-2021 JetBrains s.r.o.
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
package org.intellij.terraform.runtime

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.intellij.terraform.config.model.TypeModelProvider.Companion.reloadModel
import org.intellij.terraform.hcl.HCLBundle
import javax.swing.JButton

private const val CONFIGURABLE_ID: String = "reference.settingsdialog.project.terraform"

class TerraformToolConfigurable(private val project: Project) : BoundConfigurable(
  HCLBundle.message("terraform.name"), null
), SearchableConfigurable {

  override fun getId(): String = CONFIGURABLE_ID

  override fun createPanel(): DialogPanel {
    val configuration = TerraformToolProjectSettings.getInstance(project)
    val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)

    return panel {
      row(HCLBundle.message("terraform.settings.executable.path.label")) {
        textFieldWithBrowseButton(
          browseDialogTitle = "",
          fileChooserDescriptor = fileChooserDescriptor,
          fileChosen = { chosenFile ->
            return@textFieldWithBrowseButton chosenFile.path
          }
        ).bindText(configuration::getTerraformPath, configuration::setTerraformPath)
          .align(AlignX.FILL)
      }
      row {
        var button: Cell<JButton>? = null
        button = button(HCLBundle.message("terraform.settings.reload.terraform.metadata.model")) {
          button?.enabled(false)
          val function = {
            try {
              reloadModel(project)
            }
            finally {
              button?.enabled(true)
            }
          }
          ProgressManager.getInstance().runProcessWithProgressSynchronously(function,
                                                                            HCLBundle.message(
                                                                              "terraform.settings.reloading.terraform.model.progress.title"),
                                                                            false, project, createComponent())
        }
      }
    }
  }
}