// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.Variable
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hil.psi.impl.getHCLHost

fun getTerraformModule(element: BaseExpression): Module? {
  val host = element.getHCLHost() ?: return null
  return host.getTerraformModule()
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
    (TfPsiPatterns.DataSourceRootBlock.accepts(it) || TfPsiPatterns.ResourceRootBlock.accepts(it))
  } as? HCLBlock
}

fun getContainingResourceOrDataSourceOrModule(element: HCLElement?): HCLBlock? {
  if (element == null) return null
  return PsiTreeUtil.findFirstParent(element, true) {
    it is HCLBlock &&
    (TfPsiPatterns.DataSourceRootBlock.accepts(it) || TfPsiPatterns.ResourceRootBlock.accepts(it) || TfPsiPatterns.ModuleRootBlock.accepts(it))
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