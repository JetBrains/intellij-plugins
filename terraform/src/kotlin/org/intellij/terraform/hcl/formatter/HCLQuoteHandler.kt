// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes

class HCLQuoteHandler : SimpleTokenSetQuoteHandler(HCLTokenTypes.STRING_LITERALS)
