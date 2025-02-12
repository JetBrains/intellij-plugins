// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.DataInputOutputUtilRt
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.util.ThrowableConsumer
import com.intellij.util.gist.GistManager
import com.intellij.util.io.DataExternalizer
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.TypeModel.Companion.getTerraformBlock
import org.intellij.terraform.config.patterns.TfPsiPatterns.RequiredProvidersData
import org.intellij.terraform.config.patterns.TfPsiPatterns.RequiredProvidersSource
import org.intellij.terraform.hcl.psi.HCLProperty
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

@Service
internal class LocalProviderNamesService {

  companion object {
    @JvmStatic
    fun getInstance(): LocalProviderNamesService = ApplicationManager.getApplication().getService(LocalProviderNamesService::class.java)
  }

  val providersNamesGist =
    GistManager.getInstance().newPsiFileGist<Map<String, String>>("TF_PROVIDER_LIST", 1, object : DataExternalizer<Map<String, String>> {
      override fun save(out: DataOutput, value: Map<String, String>) {
        DataInputOutputUtilRt.writeMap(out, value,
                                       ThrowableConsumer { out.writeUTF(it) },
                                       ThrowableConsumer { out.writeUTF(it) })
      }

      override fun read(input: DataInput): Map<String, String> {
        return DataInputOutputUtilRt.readMap(input,
                                             ThrowableComputable<String, IOException> { input.readUTF() },
                                             ThrowableComputable<String, IOException> { input.readUTF() })
      }
    }) { psiFile ->
      val localNames = mutableMapOf<String, String>()
      val terraformRootBlock = getTerraformBlock(psiFile)
      terraformRootBlock?.accept(RequiredProvidersVisitor(localNames))
      localNames
    }

  private class RequiredProvidersVisitor(private val localNames: MutableMap<String, String>) : PsiRecursiveElementVisitor() {
    override fun visitElement(element: PsiElement) {
      if (RequiredProvidersData.accepts(element)) {
        element as HCLProperty
        val localName = element.name
        val source = element.value?.children?.firstOrNull { RequiredProvidersSource.accepts(it) } as? HCLProperty
        source?.value?.name
          ?.let { name -> ProviderType.parseCoordinates(name) }
          ?.run { localNames.put(localName, "${namespace}/${name}") }
      }
      element.acceptChildren(this)
    }
  }

}