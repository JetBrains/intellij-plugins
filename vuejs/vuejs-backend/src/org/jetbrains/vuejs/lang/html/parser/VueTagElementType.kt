// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.psi.impl.source.html.HtmlTagElementType
import org.jetbrains.vuejs.lang.html.VueLanguage

open class VueTagElementType(debugName: String) : HtmlTagElementType(debugName, VueLanguage)