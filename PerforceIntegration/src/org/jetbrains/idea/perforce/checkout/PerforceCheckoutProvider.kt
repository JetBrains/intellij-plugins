// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.checkout

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ui.VcsCloneComponent
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogComponentStateListener
import org.jetbrains.idea.perforce.PerforceBundle

internal class PerforceCheckoutProvider : CheckoutProvider {

  override fun getVcsName(): String = PerforceBundle.message("perforce.name.with.mnemonic")

  override fun buildVcsCloneComponent(
    project: Project,
    modalityState: ModalityState,
    dialogStateListener: VcsCloneDialogComponentStateListener
  ): VcsCloneComponent {
    return PerforceCloneDialogComponent(project, dialogStateListener)
  }
}
