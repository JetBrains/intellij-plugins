// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("DEPRECATION")

package org.jetbrains.vuejs.language

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.expr._VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.impl._VueJSVForExpression

@Deprecated("Use org.jetbrains.vuejs.lang.expr.VueJSLanguage instead, kept here for compatibility with Aegis Code Check Plugin")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueJSLanguage : _VueJSLanguage()

@Deprecated("Use org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression instead, kept here for compatibility with Aegis Code Check Plugin")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueVForExpression(vueJSElementType: IElementType) : _VueJSVForExpression(vueJSElementType)
