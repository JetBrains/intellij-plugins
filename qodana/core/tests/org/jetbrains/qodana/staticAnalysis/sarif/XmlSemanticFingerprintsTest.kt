package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV2
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.additionalFingerprintData
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiFingerprintDataExtractor
import org.junit.Assert.assertNotEquals

class XmlSemanticFingerprintsTest : BasePlatformTestCase() {
  fun testXmlAttributeValueFingerprintIgnoresFileRename() {
    val before = fingerprintState("META-INF/plugin-old.xml", """
      <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
          <extension point="com.intellij.foo" implementation="a.b.C"/>
        </extensions>
      </idea-plugin>
    """.trimIndent()) { rootTag ->
      extensionTag(rootTag).getAttribute("implementation")?.valueElement ?: error("Missing implementation attribute value")
    }
    val after = fingerprintState("META-INF/plugin-new.xml", """
      <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
          <extension point="com.intellij.foo" implementation="a.b.C"/>
        </extensions>
      </idea-plugin>
    """.trimIndent()) { rootTag ->
      extensionTag(rootTag).getAttribute("implementation")?.valueElement ?: error("Missing implementation attribute value")
    }

    assertMatchingFingerprints(before, after)
  }

  fun testXmlTagFingerprintIgnoresFileMove() {
    val before = fingerprintState("old/META-INF/plugin.xml", """
      <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
          <extension point="com.intellij.foo" implementation="a.b.C"/>
        </extensions>
      </idea-plugin>
    """.trimIndent(), ::extensionTag)
    val after = fingerprintState("new/location/META-INF/plugin.xml", """
      <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
          <extension point="com.intellij.foo" implementation="a.b.C"/>
        </extensions>
      </idea-plugin>
    """.trimIndent(), ::extensionTag)

    assertMatchingFingerprints(before, after)
  }

  fun testXmlTextFingerprintIgnoresDependencyReorder() {
    val before = fingerprintState("pom.xml", """
      <project>
        <dependencies>
          <dependency>
            <groupId>sample.one</groupId>
            <artifactId>first</artifactId>
          </dependency>
          <dependency>
            <groupId>sample.two</groupId>
            <artifactId>target</artifactId>
          </dependency>
        </dependencies>
      </project>
    """.trimIndent()) { rootTag ->
      targetDependencyTag(rootTag).findFirstSubTag("groupId")?.value?.textElements?.single()
      ?: error("Missing groupId text")
    }
    val after = fingerprintState("pom.xml", """
      <project>
        <dependencies>
          <dependency>
            <groupId>sample.two</groupId>
            <artifactId>target</artifactId>
          </dependency>
          <dependency>
            <groupId>sample.one</groupId>
            <artifactId>first</artifactId>
          </dependency>
        </dependencies>
      </project>
    """.trimIndent()) { rootTag ->
      targetDependencyTag(rootTag).findFirstSubTag("groupId")?.value?.textElements?.single()
      ?: error("Missing groupId text")
    }

    assertMatchingFingerprints(before, after)
  }

  fun testXmlTagFingerprintIgnoresNamespacePrefixRename() {
    val namespace = "http://www.springframework.org/schema/beans"
    val before = fingerprintState("spring.xml", """
      <beans xmlns:x="$namespace">
        <x:bean id="app"/>
      </beans>
    """.trimIndent()) { rootTag ->
      rootTag.subTags.single { it.localName == "bean" && it.getAttributeValue("id") == "app" }
    }
    val after = fingerprintState("spring.xml", """
      <beans xmlns:spring="$namespace">
        <spring:bean id="app"/>
      </beans>
    """.trimIndent()) { rootTag ->
      rootTag.subTags.single { it.localName == "bean" && it.getAttributeValue("id") == "app" }
    }

    assertMatchingFingerprints(before, after)
  }

  fun testXmlAttributeFingerprintIgnoresFormattingAndAttributeOrderChanges() {
    val before = fingerprintState("META-INF/plugin.xml", """
      <idea-plugin><extensions defaultExtensionNs="com.intellij"><extension point="com.intellij.foo" implementation="a.b.C" id="sample"/></extensions></idea-plugin>
    """.trimIndent()) { rootTag ->
      extensionTag(rootTag).getAttribute("point") ?: error("Missing point attribute")
    }
    val after = fingerprintState("META-INF/plugin.xml", """
      <idea-plugin>
        <extensions defaultExtensionNs="com.intellij">
          <extension
            implementation="a.b.C"
            id="sample"
            point="com.intellij.foo"
          />
        </extensions>
      </idea-plugin>
    """.trimIndent()) { rootTag ->
      extensionTag(rootTag).getAttribute("point") ?: error("Missing point attribute")
    }

    assertMatchingFingerprints(before, after)
  }

  fun testSimilarXmlSiblingsWithDifferentStableIdsDoNotMatch() {
    val xml = """
      <beans>
        <bean id="alpha"/>
        <bean id="beta"/>
      </beans>
    """.trimIndent()
    val alpha = fingerprintState("spring.xml", xml) { rootTag ->
      rootTag.subTags.single { it.localName == "bean" && it.getAttributeValue("id") == "alpha" }
    }
    val beta = fingerprintState("spring.xml", xml) { rootTag ->
      rootTag.subTags.single { it.localName == "bean" && it.getAttributeValue("id") == "beta" }
    }

    assertDifferentFingerprints(alpha, beta)
  }

  fun testHtmlTagsHaveNoXmlSemanticIdentity() {
    val psiFile = PsiFileFactory.getInstance(project).createFileFromText(
      "index.html",
      HTMLLanguage.INSTANCE,
      "<body><div class=\"navigation-bar\"><style>padding-left: 10px</style></div></body>",
    )
    val styleTag = PsiTreeUtil.findChildrenOfType(psiFile, XmlTag::class.java).single { it.name == "style" }

    val props = PropertyBag()
    PsiFingerprintDataExtractor.extract(styleTag).storeIn(props)

    assertNull(props.additionalFingerprintData().xmlSemanticIdentity)
  }

  private fun fingerprintState(
    filePath: String,
    xml: String,
    selector: (XmlTag) -> PsiElement,
  ): FingerprintState {
    val psiFile = PsiFileFactory.getInstance(project)
      .createFileFromText(filePath.substringAfterLast('/'), XMLLanguage.INSTANCE, xml) as XmlFile
    val rootTag = psiFile.rootTag ?: error("Missing root tag")
    val element = selector(rootTag)

    val props = PropertyBag()
    PsiFingerprintDataExtractor.extract(element).storeIn(props)
    val xmlSemanticIdentity = props.additionalFingerprintData().xmlSemanticIdentity?.identity
                              ?: error("Missing XML semantic identity")

    val result = Result()
      .withRuleId("XmlInspection")
      .withMessage(Message().withText("XML problem"))
      .withLocations(listOf(location(filePath, xml, element)))
    result.properties = props

    return FingerprintState(
      baselineV2 = BaselineEqualityV2.calculate(result),
      xmlSemanticIdentity = xmlSemanticIdentity,
    )
  }

  private fun location(filePath: String, xml: String, element: PsiElement): Location {
    val offset = element.textRange.startOffset
    val startLine = xml.substring(0, offset).count { it == '\n' } + 1
    val lineStartOffset = xml.substring(0, offset).lastIndexOf('\n').let { index -> if (index == -1) 0 else index + 1 }
    val startColumn = offset - lineStartOffset + 1

    return Location().withPhysicalLocation(
      PhysicalLocation()
        .withArtifactLocation(ArtifactLocation().withUri(filePath))
        .withRegion(
          Region()
            .withSourceLanguage("XML")
            .withStartLine(startLine)
            .withStartColumn(startColumn)
            .withCharLength(element.textLength)
        )
    )
  }

  private fun extensionTag(rootTag: XmlTag): XmlTag {
    return rootTag.findFirstSubTag("extensions")
             ?.subTags
             ?.single { it.localName == "extension" && it.getAttributeValue("point") == "com.intellij.foo" }
           ?: error("Missing extension tag")
  }

  private fun targetDependencyTag(rootTag: XmlTag): XmlTag {
    return rootTag.findFirstSubTag("dependencies")
             ?.subTags
             ?.single {
               it.localName == "dependency" &&
               it.findFirstSubTag("groupId")?.value?.trimmedText == "sample.two" &&
               it.findFirstSubTag("artifactId")?.value?.trimmedText == "target"
             }
           ?: error("Missing target dependency tag")
  }

  private fun assertMatchingFingerprints(before: FingerprintState, after: FingerprintState) {
    assertEquals(before.xmlSemanticIdentity, after.xmlSemanticIdentity)
    assertEquals(before.baselineV2, after.baselineV2)
  }

  private fun assertDifferentFingerprints(first: FingerprintState, second: FingerprintState) {
    assertNotEquals(first.xmlSemanticIdentity, second.xmlSemanticIdentity)
    assertNotEquals(first.baselineV2, second.baselineV2)
  }

  private data class FingerprintState(
    val baselineV2: String,
    val xmlSemanticIdentity: String,
  )
}
