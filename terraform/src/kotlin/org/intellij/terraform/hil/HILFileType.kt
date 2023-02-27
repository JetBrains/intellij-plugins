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
package org.intellij.terraform.hil

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.Icons
import javax.swing.Icon

object HILFileType : LanguageFileType(HILLanguage) {
  private const val DEFAULT_EXTENSION: String = "hil"

  override fun getIcon(): Icon {
    return Icons.FileTypes.HIL
  }

  override fun getDefaultExtension(): String {
    return DEFAULT_EXTENSION
  }

  override fun getDescription(): String {
    return HCLBundle.message("HILFileType.description")
  }

  override fun getName(): String {
    return "HIL"
  }

  override fun toString() = name
}
