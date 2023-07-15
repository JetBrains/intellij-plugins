package com.intellij.protobuf.go

import com.goide.GoCodeInsightFixtureTestCase
import com.goide.psi.GoTypeSpec
import com.intellij.protobuf.ProtoeditorCoreIcons
import com.intellij.protobuf.ide.gutter.findImplementations
import com.intellij.protobuf.ide.gutter.findProtoDefinitions
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import org.junit.Assert

class PbGoNavigationGutterTest : GoCodeInsightFixtureTestCase() {

  fun `test no gutters in pb file without implementations`() {
    addProtoFile()
    Assert.assertTrue(myFixture.findGuttersAtCaret().isEmpty())
  }

  fun `test gutters present in pb file`() {
    addGoPbGeneratedFile()
    addProtoFile()

    val gutter = myFixture.findGuttersAtCaret().single()
    Assert.assertEquals(gutter.icon, ProtoeditorCoreIcons.GoToImplementation)
    Assert.assertEquals("Navigate to implementation", gutter.tooltipText)
  }

  fun `test gutters present in go file`() {
    addProtoFile()
    addGoPbGeneratedFile()

    val gutter = myFixture.findGuttersAtCaret().single()
    Assert.assertEquals(gutter.icon, ProtoeditorCoreIcons.GoToDeclaration)
    Assert.assertEquals("Navigate to Protocol Buffers declaration", gutter.tooltipText)
  }

  fun `test navigation from go to pb message`() {
    addProtoFile()
    addGoPbGeneratedFile()

    val psiElement = myFixture.elementAtCaret.parentOfType<PsiNameIdentifierOwner>(true)!!
    val actualDefinition = findProtoDefinitions(psiElement).single()
    Assert.assertTrue(actualDefinition is PbMessageDefinition)
    actualDefinition as PbMessageDefinition
    Assert.assertEquals("HelloReply", actualDefinition.name)
  }

  fun `test navigation from pb message to go`() {
    addGoPbGeneratedFile()
    addProtoFile()

    val pbElement = myFixture.elementAtCaret.parentOfType<PsiNameIdentifierOwner>(true) as PbElement
    val actualImplementation = findImplementations(pbElement).single()
    Assert.assertTrue(actualImplementation is GoTypeSpec)
    actualImplementation as GoTypeSpec
    Assert.assertEquals("HelloRequest", actualImplementation.name)
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