// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.tree.IElementType
import org.intellij.terraform.template.TerraformTemplateLanguage

class TerraformTemplateTokenType(debugName: String) : IElementType(debugName, TerraformTemplateLanguage)