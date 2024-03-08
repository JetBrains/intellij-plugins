// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation.psi

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementGenerator

@Service(Service.Level.PROJECT)
internal class FakeHCLElementPsiFactory(val project: Project) {

  private val psiElementsCache = Caffeine.newBuilder()
    .softValues()
    .maximumSize(100)
    .executor(AppExecutorUtil.getAppExecutorService())
    .build<Pair<String, String>, HCLBlock> { (name, type) ->
      generateBlock(name, type)
    }

  fun createFakeHCLBlock(blockName: String, blockType: String): HCLBlock = psiElementsCache.get(Pair(blockName, blockType))

  private fun generateBlock(blockName: String, blockType: String): HCLBlock =
    HCLElementGenerator(project).createBlock(blockName, blockType)

}
