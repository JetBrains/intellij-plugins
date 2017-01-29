package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object CloudFormationParser {
  private val PARSED_KEY = Key.create<CloudFormationParsedFile>("CFN_PARSED_FILE")

  fun parse(psiFile: PsiFile): CloudFormationParsedFile {
    val cached = psiFile.getUserData(PARSED_KEY)
    if (cached != null && cached.fileModificationStamp == psiFile.modificationStamp) {
      return cached
    }

    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a CloudFormation file" }

    val parsed = when (psiFile) {
      is JsonFile -> JsonCloudFormationParser.parse(psiFile)
      is YAMLFile -> YamlCloudFormationParser.parse(psiFile)
      else -> error("Unsupported PSI file type: " + psiFile.javaClass.name)
    }
    assertAllNodesAreMapped(parsed)

    psiFile.putUserData(PARSED_KEY, parsed)

    return parsed
  }

  private fun assertAllNodesAreMapped(parsed: CloudFormationParsedFile) {
    fun isGoodField(field: Field): Boolean =
      field.name.indexOf('$') == -1 && !Modifier.isTransient(field.modifiers) && !Modifier.isStatic(field.modifiers)

    val seen = mutableSetOf<Any>()

    fun processInstance(obj: Any, parent: Any) {
      if (seen.contains(obj)) return
      seen.add(obj)

      if (obj is CfnNode) {
        try {
          parsed.getPsiElement(obj)
        } catch (t: Throwable) {
          error("Node $obj under $parent is not mapped")
        }
      }

      if (obj is Collection<*>) {
        obj.forEach { processInstance(it!!, parent) }
        return
      }

      val fields = obj.javaClass.declaredFields
      AccessibleObject.setAccessible(fields, true)

      fields
          .filter { isGoodField(it) }
          .mapNotNull { it.get(obj) }
          .forEach { processInstance(it, obj) }
    }

    processInstance(parsed.root, parsed.root)
  }
}