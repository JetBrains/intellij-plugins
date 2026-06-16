package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.highlighting.DomElementProblemDescriptor
import com.jetbrains.qodana.sarif.model.PropertyBag
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.additionalFingerprintData
import java.lang.reflect.Proxy

class DomProblemFingerprintPsiResolverTest : BasePlatformTestCase() {
  fun testDomTagDescriptorStoresXmlSemanticIdentity() {
    val rootTag = parseXml(
      """
        <idea-plugin>
          <extensions defaultExtensionNs="com.intellij">
            <extension point="com.intellij.foo" implementation="a.b.C"/>
          </extensions>
        </idea-plugin>
      """.trimIndent()
    )
    val extensionTag = rootTag.findFirstSubTag("extensions")?.subTags?.single()
                       ?: error("Missing extension tag")

    val descriptor = TestDomDescriptor(domTag(extensionTag))
    val resolvedElement = DomProblemFingerprintPsiResolver.resolve(descriptor) ?: error("Missing PSI element")

    assertSame(extensionTag, resolvedElement)

    val props = PropertyBag()
    PsiFingerprintDataExtractor.extract(resolvedElement).storeIn(props)

    val resolvedIdentity = props.additionalFingerprintData().xmlSemanticIdentity?.identity
    val directIdentity = PsiFingerprintDataExtractor.extract(extensionTag).additionalFingerprintData.xmlSemanticIdentity?.identity

    assertNotNull(resolvedIdentity)
    assertEquals(directIdentity, resolvedIdentity)
  }

  private fun parseXml(xml: String): XmlTag {
    val psiFile = PsiFileFactory.getInstance(project).createFileFromText("plugin.xml", XMLLanguage.INSTANCE, xml) as XmlFile
    return psiFile.rootTag ?: error("Missing root tag")
  }

  private fun domTag(tag: XmlTag): DomElement {
    return proxy(DomElement::class.java, mapOf(
      "getXmlTag" to tag,
      "getXmlElement" to tag,
      "isValid" to true,
    ))
  }

  private class TestDomDescriptor(
    private val domElement: DomElement,
  ) : DomElementProblemDescriptor {
    override fun getDomElement(): DomElement = domElement

    override fun getHighlightSeverity(): HighlightSeverity = HighlightSeverity.WARNING

    override fun getFixes(): Array<out LocalQuickFix>? = LocalQuickFix.EMPTY_ARRAY

    override fun highlightWholeElement() {
    }

    override fun getHighlightType(): ProblemHighlightType? = null

    override fun getDescriptionTemplate(): String = "Problem"
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> proxy(type: Class<T>, answers: Map<String, Any?>): T {
    return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, _ ->
      answers[method.name] ?: defaultValue(method.returnType)
    } as T
  }

  private fun defaultValue(returnType: Class<*>): Any? {
    return when (returnType) {
      Boolean::class.javaPrimitiveType -> false
      Byte::class.javaPrimitiveType -> 0.toByte()
      Char::class.javaPrimitiveType -> 0.toChar()
      Double::class.javaPrimitiveType -> 0.0
      Float::class.javaPrimitiveType -> 0f
      Int::class.javaPrimitiveType -> 0
      Long::class.javaPrimitiveType -> 0L
      Short::class.javaPrimitiveType -> 0.toShort()
      else -> null
    }
  }
}
