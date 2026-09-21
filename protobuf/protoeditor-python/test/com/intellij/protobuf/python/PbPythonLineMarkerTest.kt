package com.intellij.protobuf.python

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer
import com.intellij.idea.TestFor
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.protobuf.lang.psi.PbPackageStatement
import com.intellij.protobuf.python.gutter.PbPythonLineMarkerProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.descendantsOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile

@TestFor(issues = ["IJPL-64759"], classes = [PbPythonLineMarkerProvider::class])
class PbPythonLineMarkerTest : PbPythonTestBase() {

  // A stub marker needs the .pyi file
  fun testNoMessageLineMarkerWithoutStub() = runWithGeneratedPb("all.proto") { context ->
    if (myFixture.findFileInTempDir(context.generatedPyiFileName) != null) return@runWithGeneratedPb
    myFixture.configureByFile(context.protoFilePath)

    assertEmpty(collectLineMarkers().filter { it.lineMarkerTooltip == PYTHON_STUB_MARKER_TOOLTIP })
  }

  // A file with the generated name, but generated from another .proto file, is not a target
  fun testPackageStatementIgnoresFileGeneratedFromOtherProto() = runWithGeneratedPb("all.proto") { context ->
    myFixture.copyFileToProject("gen/import_from_pb2.py", "other/${context.generatedPyFileName}")
    try {
      myFixture.configureByFile(context.protoFilePath)

      val packageMarkers = collectLineMarkers().filter { it.lineMarkerTooltip == GENERATED_PYTHON_MARKER_TOOLTIP }
      checkMarker(packageMarkers.singleOrNull(), GENERATED_PYTHON_MARKER_TOOLTIP) { target ->
        assertEquals(myFixture.findFileInTempDir(context.generatedPyFileName), (target as PyFile).virtualFile)
      }
    }
    finally {
      deleteFileFromProject("other/${context.generatedPyFileName}")
    }
  }

  companion object {
    private val GENERATED_PYTHON_MARKER_TOOLTIP = PbPythonBundle.message("line.marker.generated.python")
    private val PYTHON_STUB_MARKER_TOOLTIP = PbPythonBundle.message("line.marker.python.stub")
  }

  // Package statement -> generated .py
  fun testPackageStatementLineMarker() = runWithGeneratedPb("all.proto") { context ->
    myFixture.configureByFile(context.protoFilePath)

    val packageStmt = myFixture.file.childrenOfType<PbPackageStatement>().firstOrNull()
                      ?: error("Package statement not found in all.proto")

    val packageMarker = collectLineMarkers().find { it.element?.parent == packageStmt }
    checkMarker(packageMarker, GENERATED_PYTHON_MARKER_TOOLTIP) { target ->
      val targetFile = target as? PyFile ?: throw AssertionError("Target should be a Python file")
      assertEquals(context.generatedPyFileName, targetFile.name)
    }
  }

  // SomeMessage -> .pyi stub
  fun testMessageLineMarker() = runWithGeneratedPb("all.proto", requirePyi = true) { context ->
    myFixture.configureByFile(context.protoFilePath)

    val message = myFixture.file.descendantsOfType<PbMessageDefinition>()
                    .find { it.name == "SomeMessage" } ?: error("SomeMessage not found in all.proto")

    val messageMarker = collectLineMarkers().find { it.element == message.nameIdentifier }
    checkMarker(messageMarker, PYTHON_STUB_MARKER_TOOLTIP) { target ->
      checkClassTarget(target, "SomeMessage", context.generatedPyiFileName)
    }
  }

  // Nested_Message -> nested class in .pyi stub
  fun testNestedMessageLineMarker() = runWithGeneratedPb("all.proto", requirePyi = true) { context ->
    myFixture.configureByFile(context.protoFilePath)

    val nestedMessage = myFixture.file.descendantsOfType<PbMessageDefinition>()
                          .find { it.name == "Nested_Message" } ?: error("Nested_Message not found in all.proto")

    val nestedMessageMarker = collectLineMarkers().find { it.element == nestedMessage.nameIdentifier }
    checkMarker(nestedMessageMarker, PYTHON_STUB_MARKER_TOOLTIP) { target ->
      checkClassTarget(target, "Nested_Message", context.generatedPyiFileName)
    }
  }

  // SomeEnum -> .pyi stub
  fun testEnumLineMarker() = runWithGeneratedPb("all.proto", requirePyi = true) { context ->
    myFixture.configureByFile(context.protoFilePath)

    val enumDef = myFixture.file.descendantsOfType<PbEnumDefinition>()
                    .find { it.name == "SomeEnum" } ?: error("SomeEnum not found in all.proto")

    val enumMarker = collectLineMarkers().find { it.element == enumDef.nameIdentifier }
    checkMarker(enumMarker, PYTHON_STUB_MARKER_TOOLTIP) { target ->
      checkClassTarget(target, "SomeEnum", context.generatedPyiFileName)
    }
  }

  private fun collectLineMarkers(): List<LineMarkerInfo<*>> =
    myFixture.findAllGutters().mapNotNull {
      (it as? LineMarkerInfo.LineMarkerGutterIconRenderer<*>)?.lineMarkerInfo
    }

  private fun checkMarker(
    marker: LineMarkerInfo<*>?,
    expectedTooltip: String,
    checkTarget: (PsiElement) -> Unit,
  ) {
    marker ?: throw AssertionError("Line marker not found")

    assertEquals(expectedTooltip, marker.lineMarkerTooltip)

    val handler = marker.navigationHandler
    val targets = (handler as? NavigationGutterIconRenderer)?.targetElements
                  ?: throw AssertionError("Unexpected navigation handler: $handler")

    assertEquals(1, targets.size)
    checkTarget(targets.first())
  }

  private fun checkClassTarget(target: PsiElement, expectedClassName: String, expectedFileName: String) {
    val targetClass = target.parent as? PyClass ?: throw AssertionError("Target should be a Python class")
    assertEquals(expectedClassName, targetClass.name)
    assertEquals(expectedFileName, targetClass.containingFile.name)
  }
}
