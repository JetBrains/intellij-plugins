package com.intellij.protobuf.ide

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.lang.PbLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

@Service
internal class PbCompositeModificationTracker(val project: Project) : ModificationTracker {
  companion object {
    @JvmStatic
    fun byElement(psiElement: PsiElement): PbCompositeModificationTracker {
      return psiElement.project.service<PbCompositeModificationTracker>()
    }
  }

  private val relatedTrackers: List<ModificationTracker>
    get() = listOf(
      PsiManager.getInstance(project).modificationTracker.forLanguage(PbLanguage.INSTANCE),
      PbProjectSettings.getModificationTracker(project)
    )

  override fun getModificationCount(): Long {
    return relatedTrackers.sumOf { it.modificationCount }
  }
}