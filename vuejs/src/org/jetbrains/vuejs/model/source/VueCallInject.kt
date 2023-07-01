// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueInject


class VueCallInject(override val name: String, override val source: PsiElement) : VueInject