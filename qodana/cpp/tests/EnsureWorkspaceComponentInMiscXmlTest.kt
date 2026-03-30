package org.jetbrains.qodana.cpp

import com.intellij.openapi.util.JDOMUtil
import org.assertj.core.api.Assertions.assertThat
import org.jdom.Element
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.div

class EnsureWorkspaceComponentInMiscXmlTest {
  @Test
  fun `creates file when it does not exist`(@TempDir tempDir: Path) {
    val miscXml = tempDir / ".idea" / "misc.xml"

    ensureWorkspaceComponentInMiscXml(miscXml, "CMakeWorkspace")

    assertThat(miscXml).exists()
    val root = JDOMUtil.load(miscXml)
    assertThat(root.name).isEqualTo("project")
    assertThat(root.getAttributeValue("version")).isEqualTo("4")
    val components = root.getChildren("component")
    assertThat(components).hasSize(1)
    assertThat(components[0].getAttributeValue("name")).isEqualTo("CMakeWorkspace")
    assertThat(components[0].getAttributeValue("PROJECT_DIR")).isEqualTo("\$PROJECT_DIR\$")
  }

  @Test
  fun `adds component to existing file with no CIDR components`(@TempDir tempDir: Path) {
    val miscXml = tempDir / "misc.xml"
    JDOMUtil.write(
      Element("project").setAttribute("version", "4"),
      miscXml
    )

    ensureWorkspaceComponentInMiscXml(miscXml, "CompDBWorkspace")

    val root = JDOMUtil.load(miscXml)
    val components = root.getChildren("component")
    assertThat(components).hasSize(1)
    assertThat(components[0].getAttributeValue("name")).isEqualTo("CompDBWorkspace")
    assertThat(components[0].getAttributeValue("PROJECT_DIR")).isEqualTo("\$PROJECT_DIR\$")
  }

  @Test
  fun `replaces stale workspace component`(@TempDir tempDir: Path) {
    val miscXml = tempDir / "misc.xml"
    JDOMUtil.write(
      Element("project").setAttribute("version", "4").apply {
        addContent(
          Element("component")
            .setAttribute("name", "CMakeWorkspace")
            .setAttribute("PROJECT_DIR", "\$PROJECT_DIR\$")
        )
      },
      miscXml
    )

    ensureWorkspaceComponentInMiscXml(miscXml, "MakefileWorkspace")

    val root = JDOMUtil.load(miscXml)
    val components = root.getChildren("component")
    assertThat(components).hasSize(1)
    assertThat(components[0].getAttributeValue("name")).isEqualTo("MakefileWorkspace")
  }

  @Test
  fun `preserves unrelated components`(@TempDir tempDir: Path) {
    val miscXml = tempDir / "misc.xml"
    JDOMUtil.write(
      Element("project").setAttribute("version", "4").apply {
        addContent(Element("component").setAttribute("name", "ProjectRootManager").setAttribute("version", "2"))
        addContent(Element("component").setAttribute("name", "CMakeWorkspace").setAttribute("PROJECT_DIR", "old"))
      },
      miscXml
    )

    ensureWorkspaceComponentInMiscXml(miscXml, "MesonWorkspace")

    val root = JDOMUtil.load(miscXml)
    val components = root.getChildren("component")
    assertThat(components).hasSize(2)
    assertThat(components.map { it.getAttributeValue("name") })
      .containsExactlyInAnyOrder("ProjectRootManager", "MesonWorkspace")
  }

  @Test
  fun `re-creates same component with fresh attributes`(@TempDir tempDir: Path) {
    val miscXml = tempDir / "misc.xml"
    JDOMUtil.write(
      Element("project").setAttribute("version", "4").apply {
        addContent(
          Element("component")
            .setAttribute("name", "CMakeWorkspace")
            .setAttribute("PROJECT_DIR", "stale_value")
        )
      },
      miscXml
    )

    ensureWorkspaceComponentInMiscXml(miscXml, "CMakeWorkspace")

    val root = JDOMUtil.load(miscXml)
    val components = root.getChildren("component").filter { it.getAttributeValue("name") == "CMakeWorkspace" }
    assertThat(components).hasSize(1)
    assertThat(components[0].getAttributeValue("PROJECT_DIR")).isEqualTo("\$PROJECT_DIR\$")
  }
}
