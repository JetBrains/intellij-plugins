/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.intellij.terraform.hil.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.Variable
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.psi.impl.getHCLHost

fun getTerraformModule(element: BaseExpression): Module? {
  val host = element.getHCLHost() ?: return null
  val module = host.getTerraformModule()
  return module
}

fun getLocalDefinedVariables(element: BaseExpression): List<Variable> {
  return getTerraformModule(element)?.getAllVariables() ?: emptyList()
}
fun getLocalDefinedLocals(element: BaseExpression): List<String> {
  return getTerraformModule(element)?.getAllLocals()?.map { it.first } ?: emptyList()
}

fun getProvisionerOrConnectionResource(position: BaseExpression): HCLBlock? {
  // For now 'self' allowed only for provisioners and connections inside resources
  return position.getHCLHost()?.let { getProvisionerResource(it) ?: getConnectionResource(it)}
}

fun getProvisionerResource(host: HCLElement): HCLBlock? {
  val provisioner = PsiTreeUtil.getParentOfType(host, HCLBlock::class.java) ?: return null
  if (provisioner.getNameElementUnquoted(0) == "connection") return getProvisionerResource(provisioner)
  if (provisioner.getNameElementUnquoted(0) != "provisioner") return null
  val resource = PsiTreeUtil.getParentOfType(provisioner, HCLBlock::class.java, true) ?: return null
  if (resource.getNameElementUnquoted(0) != "resource") return null
  return resource
}

fun getConnectionResource(host: HCLElement): HCLBlock? {
  val provisioner = PsiTreeUtil.getParentOfType(host, HCLBlock::class.java) ?: return null
  if (provisioner.getNameElementUnquoted(0) != "connection") return null
  val resource = PsiTreeUtil.getParentOfType(provisioner, HCLBlock::class.java, true) ?: return null
  if (resource.getNameElementUnquoted(0) != "resource") return null
  return resource
}

fun getResource(position: BaseExpression): HCLBlock? {
  val host = position.getHCLHost() ?: return null

  // For now 'self' allowed only for provisioners inside resources

  val resource = PsiTreeUtil.getParentOfType(host, HCLBlock::class.java, true) ?: return null
  if (resource.getNameElementUnquoted(0) != "resource") return null
  return resource
}

fun getDataSource(position: BaseExpression): HCLBlock? {
  val host = position.getHCLHost() ?: return null

  val dataSource = PsiTreeUtil.getParentOfType(host, HCLBlock::class.java, true) ?: return null
  if (dataSource.getNameElementUnquoted(0) != "data") return null
  return dataSource
}

fun getContainingResourceOrDataSource(element: HCLElement?): HCLBlock? {
  if (element == null) return null
  return PsiTreeUtil.findFirstParent(element, true) {
    it is HCLBlock &&
        (TerraformPatterns.DataSourceRootBlock.accepts(it) || TerraformPatterns.ResourceRootBlock.accepts(it))
  } as? HCLBlock
}

fun getContainingResourceOrDataSourceOrModule(element: HCLElement?): HCLBlock? {
  if (element == null) return null
  return PsiTreeUtil.findFirstParent(element, true) {
    it is HCLBlock &&
        (TerraformPatterns.DataSourceRootBlock.accepts(it) || TerraformPatterns.ResourceRootBlock.accepts(it)|| TerraformPatterns.ModuleRootBlock.accepts(it))
  } as? HCLBlock
}

fun <T : PsiElement> PsiElement.getNthChild(n: Int, clazz: Class<T>): T? {
  var child: PsiElement? = this.firstChild
  var i: Int = 0
  while (child != null) {
    if (clazz.isInstance(child)) {
      i++
      @Suppress("UNCHECKED_CAST")
      if (i == n) return child as T?
    }
    child = child.nextSibling
  }
  return null
}