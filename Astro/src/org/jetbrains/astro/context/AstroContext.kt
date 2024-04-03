// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.context

import com.intellij.javascript.web.hasFilesOfType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.context.WebSymbolsContext
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileType

private const val KIND_ASTRO_PROJECT = "astro-project"
private const val CONTEXT_ASTRO = "astro"

fun isAstroProject(context: PsiElement): Boolean =
  WebSymbolsContext.get(KIND_ASTRO_PROJECT, context) == CONTEXT_ASTRO

fun isAstroProject(contextFile: VirtualFile, project: Project): Boolean =
  WebSymbolsContext.get(KIND_ASTRO_PROJECT, contextFile, project) == CONTEXT_ASTRO

fun isAstroFrameworkContext(context: PsiElement): Boolean =
  WebSymbolsContext.get(WebSymbolsContext.KIND_FRAMEWORK, context) == AstroFramework.ID

fun isAstroFrameworkContext(contextFile: VirtualFile, project: Project): Boolean =
  WebSymbolsContext.get(WebSymbolsContext.KIND_FRAMEWORK, contextFile, project) == AstroFramework.ID

fun hasAstroFiles(project: Project): Boolean =
  hasFilesOfType(project, AstroFileType)
