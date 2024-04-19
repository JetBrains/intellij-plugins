// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.psi.HCLElement

/**
 *  occurrences could be HCLElement or ILElement or part of some string
 */
class IntroduceOperation(project: Project, editor: Editor, file: PsiFile, name: String?) : BaseIntroduceOperation<HCLElement>(project, editor, file, name)