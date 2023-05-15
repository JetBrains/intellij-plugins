package org.intellij.prisma.ide.formatter

import com.intellij.formatting.Alignment
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.intellij.prisma.lang.psi.PrismaBlock
import org.intellij.prisma.lang.psi.PrismaEnumValueDeclaration
import org.intellij.prisma.lang.psi.PrismaFieldDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValue

class PrismaChildAlignmentProvider(private val map: Map<PsiElement, PrismaChildAlignment>) {
  fun findByElement(element: PsiElement): PrismaChildAlignment? {
    return map[element]
  }

  companion object {
    private val EMPTY = PrismaChildAlignmentProvider(emptyMap())

    fun forElement(element: PsiElement): PrismaChildAlignmentProvider {
      if (element !is PrismaBlock) {
        return EMPTY
      }

      val builder = PrismaChildAlignmentBuilder()
      var prev: PsiElement? = null

      for (child in element.children) {
        if (shouldStartNewGroup(prev, child)) {
          builder.finishGroup()
        }

        when (child) {
          is PrismaFieldDeclaration -> {
            builder.addToGroup(child)
            builder.alignByType()
            if (child.fieldAttributeList.isNotEmpty()) {
              builder.alignByAttribute()
            }
          }

          is PrismaEnumValueDeclaration -> {
            builder.addToGroup(child)
            if (child.fieldAttributeList.isNotEmpty()) {
              builder.alignByAttribute()
            }
          }

          is PrismaKeyValue -> {
            builder.addToGroup(child)
            builder.alignByKeyValue()
          }

          else -> builder.finishGroup()
        }

        prev = child
      }

      return builder.build()
    }

    private fun shouldStartNewGroup(prev: PsiElement?, next: PsiElement): Boolean {
      for (element in generateSequence(prev) { it.nextSibling }) {
        if (element == next) {
          return false
        }
        if (element is PsiWhiteSpace && StringUtil.countNewLines(element.text) > 1) {
          return true
        }
      }

      return true
    }
  }
}

private class PrismaChildAlignmentBuilder {
  private var typeAlignment: Alignment? = null
  private var attributeAlignment: Alignment? = null
  private var keyValueAlignment: Alignment? = null

  private val alignments = mutableMapOf<PsiElement, PrismaChildAlignment>()
  private val group = mutableListOf<PsiElement>()

  fun alignByType() {
    if (typeAlignment == null) {
      typeAlignment = createAlignment()
    }
  }

  fun alignByKeyValue() {
    if (keyValueAlignment == null) {
      keyValueAlignment = createAlignment()
    }
  }

  fun alignByAttribute() {
    if (attributeAlignment == null) {
      attributeAlignment = createAlignment()
    }
  }

  private fun createAlignment() = Alignment.createAlignment(true)

  fun addToGroup(element: PsiElement) {
    group.add(element)
  }

  fun finishGroup() {
    val childAlignment = PrismaChildAlignment(typeAlignment, attributeAlignment, keyValueAlignment)
    for (element in group) {
      alignments[element] = childAlignment
    }
    reset()
  }

  fun reset() {
    group.clear()

    typeAlignment = null
    attributeAlignment = null
    keyValueAlignment = null
  }

  fun build(): PrismaChildAlignmentProvider {
    finishGroup()
    return PrismaChildAlignmentProvider(alignments)
  }
}