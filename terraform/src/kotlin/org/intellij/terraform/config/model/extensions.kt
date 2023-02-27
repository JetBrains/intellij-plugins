/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.model

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.testFramework.LightVirtualFile
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.TypeCachedValueProvider
import org.intellij.terraform.hil.psi.impl.getHCLHost

fun HCLElement.getTerraformModule(): Module {
  val file = this.containingFile.originalFile
  assert(file is HCLFile)
  return Module.getModule(file)
}

fun PsiElement.getTerraformSearchScope(): GlobalSearchScope {
  val file = this.containingFile.originalFile
  var directory = file.containingDirectory
  if (directory == null) {
    if (this is ILExpression) {
      directory = InjectedLanguageManager.getInstance(project).getTopLevelFile(this)?.containingDirectory
    }
  }
  if (directory == null) {
    // File only in-memory, assume as only file in module
    var vf: VirtualFile? = file.virtualFile
    if (vf is LightVirtualFile) {
      vf = vf.originalFile?:vf
    }
    val parent = vf?.parent ?: return GlobalSearchScope.fileScope(file)
    return GlobalSearchScopes.directoryScope(file.project, parent, false)
  } else {
    return GlobalSearchScopes.directoryScope(directory, false)
  }
}

fun HCLProperty.toProperty(type: PropertyType): Property {
  return Property(type, this.value)
}

fun HCLBlock.getProviderFQName(): String? {
  val tp = this.getNameElementUnquoted(1) ?: return null
  val als = when (val value = this.`object`?.findProperty("alias")?.value) {
    is HCLStringLiteral -> value.value
    is HCLIdentifier -> value.id
    else -> null
  }
  if (als != null) {
    return "$tp.$als"
  } else {
    return tp
  }
}

fun BaseExpression?.getType(): Type? {
  if (this == null) return null
  return TypeCachedValueProvider.getType(this)
}

fun BaseExpression.isInTerraformFile(): Boolean {
  return TerraformPatterns.TerraformFile.accepts(this.getHCLHost()?.containingFile)
}

fun String.ensureHavePrefix(prefix: String) = if (this.startsWith(prefix)) this else (prefix + this)
fun String.ensureHaveSuffix(suffix: String) = if (this.endsWith(suffix)) this else (this + suffix)


fun ObjectNode.isNotEmpty(): Boolean {
  return size() > 0
}

fun ObjectNode.obj(name: String): ObjectNode? {
  return this.get(name) as? ObjectNode
}

fun ObjectNode.array(name: String): ArrayNode? {
  return this.get(name) as? ArrayNode
}

fun ObjectNode.string(name: String): String? {
  return this.get(name)?.textValue()
}

fun ObjectNode.number(name: String): Number? {
  return this.get(name)?.numberValue()
}

fun ObjectNode.boolean(name: String): Boolean? {
  val node = this.get(name) ?: return null
  if (!node.isBoolean) return null
  return node.booleanValue()
}

