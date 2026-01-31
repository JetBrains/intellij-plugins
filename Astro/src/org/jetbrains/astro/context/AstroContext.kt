// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.context

import com.intellij.javascript.web.hasFilesOfType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.context.PolyContext
import com.intellij.psi.PsiElement
import org.jetbrains.astro.astroFramework
import org.jetbrains.astro.lang.AstroFileType

private const val KIND_ASTRO_PROJECT = "astro-project"

fun isAstroProject(context: PsiElement): Boolean =
  PolyContext.get(KIND_ASTRO_PROJECT, context).let { it == "astro" || it == "true"}

fun isAstroProject(contextFile: VirtualFile, project: Project): Boolean =
  PolyContext.get(KIND_ASTRO_PROJECT, contextFile, project).let { it == "astro" || it == "true"}

fun isAstroFrameworkContext(context: PsiElement): Boolean =
  astroFramework.isInContext(context)

fun isAstroFrameworkContext(contextFile: VirtualFile, project: Project): Boolean =
  astroFramework.isInContext(contextFile, project)

fun hasAstroFiles(project: Project): Boolean =
  hasFilesOfType(project, AstroFileType)
