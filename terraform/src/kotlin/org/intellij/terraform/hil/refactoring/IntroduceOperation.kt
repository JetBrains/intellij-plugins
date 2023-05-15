// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.refactoring.BaseIntroduceOperation
import org.intellij.terraform.hil.psi.ILExpression

class IntroduceOperation(project: Project, editor: Editor, file: PsiFile, name: String?) : BaseIntroduceOperation<ILExpression>(project, editor, file, name)