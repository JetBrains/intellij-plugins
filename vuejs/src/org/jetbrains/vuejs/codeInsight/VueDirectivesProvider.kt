package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.DIRECTIVES
import org.jetbrains.vuejs.index.VueGlobalDirectivesIndex
import org.jetbrains.vuejs.index.VueLocalDirectivesIndex
import org.jetbrains.vuejs.index.getForAllKeys
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 10/23/2017.
 */
class VueDirectivesProvider {
  companion object {
    fun getAttributes(descriptor: JSObjectLiteralExpression?, project: Project) : List<VueAttributeDescriptor> {
      val result = mutableListOf<VueAttributeDescriptor>()
      result.addAll(getForAllKeys(createSearchScope(descriptor, project), VueGlobalDirectivesIndex.KEY, null).
        map { createDescriptor(it) })

      val directives = findProperty(descriptor, DIRECTIVES)
      val fileScope = createContainingFileScope(directives)
      if (directives != null && fileScope != null) {
        result.addAll(getForAllKeys(fileScope, VueLocalDirectivesIndex.KEY, null)
          .filter { PsiTreeUtil.isAncestor(directives, it.parent, false) }
          .map { createDescriptor(it) })
      }
      return result
    }

    fun resolveAttribute(descriptor: JSObjectLiteralExpression?, attrName: String, project: Project) : VueAttributeDescriptor? {
      val searchName = toAsset(attrName.substringAfter("v-", ""))
      if (searchName.isEmpty()) return null
      var element = resolve(searchName, createSearchScope(descriptor, project), VueGlobalDirectivesIndex.KEY)?.firstOrNull()

      val directives = findProperty(descriptor, DIRECTIVES)
      val fileScope = createContainingFileScope(directives)
      if (element == null && directives != null && fileScope != null) {
        element = resolve(searchName, fileScope, VueLocalDirectivesIndex.KEY)
          ?.firstOrNull { PsiTreeUtil.isAncestor(directives, it.parent, false) }
      }

      element ?: return null
      return createDescriptor(element)
    }

    private fun createContainingFileScope(directives: JSProperty?): GlobalSearchScope? {
      directives ?: return null
      val file = getContainingXmlFile(directives) ?: return null
      return GlobalSearchScope.fileScope(file.originalFile)
    }

    private fun createSearchScope(descriptor: JSObjectLiteralExpression?, project: Project) =
      descriptor?.resolveScope ?: GlobalSearchScope.projectScope(project)

    private fun createDescriptor(it: JSImplicitElement) = VueAttributeDescriptor("v-" + fromAsset(it.name), it.parent).setIsDirective()
  }
}