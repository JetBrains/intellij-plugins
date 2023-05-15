// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.formatter

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import org.intellij.terraform.hil.HILTokenTypes

class HILQuoteHandler : SimpleTokenSetQuoteHandler(HILTokenTypes.STRING_LITERALS)
