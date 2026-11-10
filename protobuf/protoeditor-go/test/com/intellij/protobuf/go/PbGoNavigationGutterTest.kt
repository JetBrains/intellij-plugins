package com.intellij.protobuf.go

import com.goide.GoTestUtil
import com.goide.SdkAware
import com.goide.junit5.goCodeInsightFixture
import com.goide.junit5.goSharedProjectFixture
import com.goide.psi.GoTypeSpec
import com.intellij.openapi.application.readAction
import com.intellij.protobuf.ProtoeditorCoreIcons
import com.intellij.protobuf.ide.gutter.findImplementations
import com.intellij.protobuf.ide.gutter.findProtoDefinitions
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.swing.Icon

@TestApplication
@SdkAware(GoTestUtil.NO_SDK)
class PbGoNavigationGutterTest {
  companion object {
    private val goProject = goSharedProjectFixture()
  }

  private val myFixture by goProject.goCodeInsightFixture("")

  @Test
  fun `test no gutters in pb file without implementations`() {
    addProtoFile()
    assertTrue(myFixture.findGuttersAtCaret().isEmpty())
  }

  @Test
  fun `test gutters present in pb file`() {
    addGoPbGeneratedFile()
    addProtoFile()

    val gutter = gutterAtCaret()
    assertEquals(ProtoeditorCoreIcons.GoToImplementation, gutter.first)
    assertEquals("Navigate to implementation", gutter.second)
  }

  @Test
  fun `test gutters present in go file`() {
    addProtoFile()
    addGoPbGeneratedFile()

    val gutter = gutterAtCaret()
    assertEquals(ProtoeditorCoreIcons.GoToDeclaration, gutter.first)
    assertEquals("Navigate to Protocol Buffers declaration", gutter.second)
  }

  @Test
  fun `test navigation from go to pb message`() {
    addProtoFile()
    addGoPbGeneratedFile()

    val definitionName = timeoutRunBlocking {
      readAction {
        val psiElement = myFixture.elementAtCaret.parentOfType<PsiNameIdentifierOwner>(true)!!
        val actualDefinition = findProtoDefinitions(psiElement).single()
        assertTrue(actualDefinition is PbMessageDefinition)
        actualDefinition as PbMessageDefinition
        actualDefinition.name
      }
    }
    assertEquals("HelloReply", definitionName)
  }

  @Test
  fun `test navigation from pb message to go`() {
    addGoPbGeneratedFile()
    addProtoFile()

    val implementationName = timeoutRunBlocking {
      readAction {
        val pbElement = myFixture.elementAtCaret.parentOfType<PsiNameIdentifierOwner>(true) as PbElement
        val actualImplementation = findImplementations(pbElement).single()
        assertTrue(actualImplementation is GoTypeSpec)
        actualImplementation as GoTypeSpec
        actualImplementation.name
      }
    }
    assertEquals("HelloRequest", implementationName)
  }

  /** Returns the icon and the tooltip text of the only gutter mark at the caret. */
  private fun gutterAtCaret(): Pair<Icon?, String?> {
    val gutter = myFixture.findGuttersAtCaret().single()
    return timeoutRunBlocking { readAction { gutter.icon to gutter.tooltipText } }
  }

  private fun addProtoFile() {
    myFixture.configureByText("helloworld.proto", """
      syntax = "proto3";

      option go_package = "google.golang.org/grpc/examples/helloworld/helloworld";

      package helloworld;

      service Greeter {
        rpc SayHello (HelloRequest) returns (HelloReply) {}
      }

      message Hello<caret>Request {
        string name = 1;
      }

      message HelloReply {
        string message = 1;
      }
    """.trimIndent())
  }

  private fun addGoPbGeneratedFile() {
    myFixture.configureByText("helloworld.pb.go", """
      package helloworld

      type HelloRequest struct {
      	state         protoimpl.MessageState
      	sizeCache     protoimpl.SizeCache
      	unknownFields protoimpl.UnknownFields

      	Name string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
      }

      type Hello<caret>Reply struct {
      	state         protoimpl.MessageState
      	sizeCache     protoimpl.SizeCache
      	unknownFields protoimpl.UnknownFields

      	Message string `protobuf:"bytes,1,opt,name=message,proto3" json:"message,omitempty"`
      }
    """.trimIndent())
  }
}