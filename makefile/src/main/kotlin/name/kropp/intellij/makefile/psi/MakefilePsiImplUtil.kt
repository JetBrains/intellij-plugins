package name.kropp.intellij.makefile.psi

import com.intellij.navigation.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import name.kropp.intellij.makefile.*
import name.kropp.intellij.makefile.psi.MakefileTypes.*
import name.kropp.intellij.makefile.psi.impl.*
import java.util.regex.*

object MakefilePsiImplUtil {
  private val suffixRule = Pattern.compile("^\\.[a-zA-Z]+(\\.[a-zA-Z]+)$")

  private val ASSIGNMENT = TokenSet.create(MakefileTypes.ASSIGN)
  private val LINE = TokenSet.create(MakefileTypes.IDENTIFIER, MakefileTypes.VARIABLE_USAGE)

  @JvmStatic
  fun getTargets(element: MakefileRule): List<MakefileTarget> {
    return element.targetLine.targets.targetList
  }

  @JvmStatic
  fun getTargetName(element: MakefileTargetLine): String? {
    val targetNode = element.node.findChildByType(MakefileTypes.TARGET) ?: return null
    return targetNode.text
  }

  @JvmStatic
  fun getName(element: MakefileTarget): String {
    return element.text
  }

  @JvmStatic
  fun setName(element: MakefileTarget, newName: String): PsiElement {
    val identifierNode = element.node.firstChildNode
    if (identifierNode != null) {
      val target = MakefileElementFactory.createTarget(element.project, newName)
      val newIdentifierNode = target.firstChild.node
      element.node.replaceChild(identifierNode, newIdentifierNode)
    }
    return element
  }

  @JvmStatic
  fun getName(element: MakefileVariable): String {
    return element.text
  }

  @JvmStatic
  fun setName(element: MakefileVariable, newName: String): PsiElement {
    val identifierNode = element.node.firstChildNode
    if (identifierNode != null) {
      val variable = MakefileElementFactory.createVariable(element.project, newName)
      val newIdentifierNode = variable.firstChild.node
      element.node.replaceChild(identifierNode, newIdentifierNode)
    }
    return element
  }

  @JvmStatic
  fun getNameIdentifier(element: MakefileTarget): PsiElement? {
    if (element.isSpecialTarget) return null

    val targetNode = element.node
    return targetNode?.psi
  }

  @JvmStatic
  fun getNameIdentifier(element: MakefileVariable): PsiElement? {
    return element.firstChild
  }

  @JvmStatic
  fun getDocComment(element: MakefileTarget): String? {
    if (element.isSpecialTarget) return null

    val targetLine = element.parent.parent as MakefileTargetLine

    val comments = PsiTreeUtil.findChildrenOfType(targetLine, PsiComment::class.java)
    for (comment in comments) {
      if (comment.tokenType === MakefileTypes.DOC_COMMENT) {
        return comment.text.substring(2)
      }
    }

    return null
  }

  @JvmStatic
  fun getPresentation(element: MakefileTarget): ItemPresentation {
    return MakefileTargetPresentation(element)
  }

  @JvmStatic
  fun isSpecialTarget(element: MakefileTarget): Boolean {
    val name = element.name
    return name != null && (name.matches("^\\.[A-Z_]*".toRegex()) || name == "FORCE" || suffixRule.matcher(name).matches())
  }

  @JvmStatic
  fun isPatternTarget(element: MakefileTarget): Boolean {
    val name = element.name
    return name != null && name.contains("%")
  }

  @JvmStatic
  fun matches(element: MakefileTarget, prerequisite: String): Boolean {
    val name = element.name ?: return false
    if (name.startsWith("%")) {
      return prerequisite.endsWith(name.substring(1))
    }
    if (name.endsWith("%")) {
      return prerequisite.startsWith(name.substring(0, name.length - 1))
    }
    val matcher = suffixRule.matcher(name)
    return if (matcher.matches()) {
      prerequisite.endsWith(matcher.group(1))
    } else name == prerequisite
  }

  @JvmStatic
  fun getAssignment(element: MakefileVariableAssignment): PsiElement? {
    val node = element.node.findChildByType(ASSIGNMENT) ?: return null
    return node.psi
  }

  @JvmStatic
  fun getValue(element: MakefileVariableAssignment): String? {
    val value = element.variableValue ?: return ""
    val nodes = value.node.getChildren(TokenSet.ANY)
    return nodes.joinToString("\n") { it.text }
  }

  @JvmStatic
  fun getAssignment(element: MakefileDefine): PsiElement? {
    val node = element.node.findChildByType(ASSIGNMENT) ?: return null
    return node.psi
  }

  @JvmStatic
  fun getValue(element: MakefileDefine): String? {
    val nodes = element.node.getChildren(TokenSet.ANY).asSequence()
    return nodes.dropWhile { it.elementType != EOL }.filter { it.elementType != KEYWORD_ENDEF }.joinToString("\n") { it.text }
  }

  @JvmStatic
  fun isEmpty(element: MakefileRecipe): Boolean {
    return element.commandList.isEmpty() && element.conditionalList.isEmpty()
  }

  @JvmStatic
  fun updateText(prerequisite: MakefilePrerequisite, newText: String): MakefilePrerequisiteImpl {
    val replacement = MakefileElementFactory.createPrerequisite(prerequisite.project, newText)
    return prerequisite.replace(replacement) as MakefilePrerequisiteImpl
  }

  @JvmStatic
  fun isPhonyTarget(prerequisite: MakefilePrerequisite): Boolean {
    val file = prerequisite.containingFile
    if (file is MakefileFile) {
      for (rule in file.phonyRules) {
        val prerequisites = rule.targetLine.prerequisites
        if (prerequisites != null) {
          val normalPrerequisites = prerequisites.normalPrerequisites
          for (goal in normalPrerequisites.prerequisiteList) {
            if (goal.text == prerequisite.text) {
              return true
            }
          }
        }
      }

    }
    return false
  }
}