package org.jetbrains.qodana.xml

import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaXmlSemanticIdentityProvider

internal class XmlSemanticIdentityProvider : QodanaXmlSemanticIdentityProvider {
  override fun extractIdentity(element: PsiElement): String? = XmlSemanticIdentityExtractor.extractIdentity(element)
}

internal object XmlSemanticIdentityExtractor {
  private const val VERSION = "v1"
  private val STABLE_SELECTOR_NAMES = listOf(
    "id",
    "name",
    "key",
    "bean",
    "ref",
    "point",
    "implementation",
    "groupId",
    "artifactId",
    "module",
    "serviceInterface",
    "interface",
    "forClass",
  )

  fun extractIdentity(element: PsiElement): String? {
    if (element.containingFile?.language != XMLLanguage.INSTANCE) return null
    val target = findTarget(element) ?: return null
    return "$VERSION|${target.identity()}"
  }

  private fun findTarget(element: PsiElement): XmlTarget? {
    var current: PsiElement? = element
    while (current != null) {
      when (current) {
        is XmlAttributeValue -> return XmlAttributeValueTarget(current)
        is XmlAttribute -> return XmlAttributeTarget(current)
        is XmlText -> return current.parentTag?.let(::XmlTextTarget)
        is XmlTag -> return XmlTagTarget(current)
      }
      current = current.parent
    }
    return null
  }

  private sealed interface XmlTarget {
    val ownerTag: XmlTag
    val kind: String

    fun targetDetails(): List<String> = emptyList()

    fun identity(): String {
      return buildList {
        add(kind)
        add(tagPath(ownerTag))
        addAll(targetDetails())
      }.joinToString("|")
    }
  }

  private data class XmlTagTarget(private val tag: XmlTag) : XmlTarget {
    override val ownerTag: XmlTag = tag
    override val kind: String = "tag"
  }

  private data class XmlAttributeTarget(private val attribute: XmlAttribute) : XmlTarget {
    override val ownerTag: XmlTag = attribute.parent
    override val kind: String = "attribute"

    override fun targetDetails(): List<String> = listOf(attributeSegment(attribute))
  }

  private data class XmlAttributeValueTarget(private val attributeValue: XmlAttributeValue) : XmlTarget {
    private val attribute: XmlAttribute = attributeValue.parent as XmlAttribute

    override val ownerTag: XmlTag = attribute.parent
    override val kind: String = "attributeValue"

    override fun targetDetails(): List<String> {
      return listOf(
        attributeSegment(attribute),
        "value=${normalizeValue(attributeValue.value)}",
      )
    }
  }

  private data class XmlTextTarget(override val ownerTag: XmlTag) : XmlTarget {
    override val kind: String = "text"

    override fun targetDetails(): List<String> {
      val text = ownerTag.value.trimmedText.takeIf { it.isNotBlank() } ?: return emptyList()
      return listOf("value=${normalizeValue(text)}")
    }
  }

  private fun tagPath(tag: XmlTag): String {
    return generateSequence(tag) { it.parentTag }
      .toList()
      .asReversed()
      .joinToString("/") { currentTag -> tagSegment(currentTag) }
  }

  private fun tagSegment(tag: XmlTag): String {
    val selectors = tagSelectors(tag)
    return buildString {
      append(qualifiedName(tag.localName, tag.namespace))
      if (selectors.isNotEmpty()) {
        append('[')
        append(selectors.joinToString(","))
        append(']')
      }
    }
  }

  private fun tagSelectors(tag: XmlTag): List<String> {
    val stableSelectors = STABLE_SELECTOR_NAMES.mapNotNull { selectorName ->
      stableAttributeSelector(tag, selectorName) ?: stableChildSelector(tag, selectorName)
    }
    if (stableSelectors.isNotEmpty()) return stableSelectors

    val attributeFallback = tag.attributes
      .filterNot { it.isNamespaceDeclaration }
      .sortedBy { attributeSortKey(it) }
      .map(::attributeSegment)
    if (attributeFallback.isNotEmpty()) return attributeFallback

    val textFallback = tag.value.trimmedText
      .takeIf { tag.subTags.isEmpty() && it.isNotBlank() }
      ?.let { text -> listOf("text=${normalizeValue(text)}") }
    return textFallback.orEmpty()
  }

  private fun stableAttributeSelector(tag: XmlTag, selectorName: String): String? {
    val attribute = tag.attributes.firstOrNull { !it.isNamespaceDeclaration && it.localName == selectorName } ?: return null
    return attributeSegment(attribute)
  }

  private fun stableChildSelector(tag: XmlTag, selectorName: String): String? {
    val subTag = tag.subTags.firstOrNull { it.localName == selectorName } ?: return null
    val value = subTag.value.trimmedText.takeIf { it.isNotBlank() } ?: return null
    return "child:${qualifiedName(subTag.localName, subTag.namespace)}=${normalizeValue(value)}"
  }

  private fun attributeSegment(attribute: XmlAttribute): String {
    val attributeValue = attribute.displayValue.orEmpty().ifBlank { attribute.value.orEmpty() }
    return "attr:${qualifiedName(attribute.localName, attribute.namespace)}=${normalizeValue(attributeValue)}"
  }

  private fun attributeSortKey(attribute: XmlAttribute): String {
    return buildString {
      append(attribute.namespace)
      append('\u0000')
      append(attribute.localName)
    }
  }

  private fun qualifiedName(localName: String, namespace: String): String {
    return if (namespace.isBlank()) {
      localName
    }
    else {
      "{$namespace}$localName"
    }
  }

  private fun normalizeValue(value: String): String {
    return value.trim()
      .replace(WHITESPACE_REGEX, " ")
      .take(MAX_VALUE_LENGTH)
  }

  private val WHITESPACE_REGEX = Regex("\\s+")
  private const val MAX_VALUE_LENGTH = 200
}
