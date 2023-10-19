package com.intellij.protobuf.ide.actions

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.datatransfer.StringSelection

class PbCopyPasteHandlerTest : BasePlatformTestCase() {
  fun `test insert into empty proto2`() {
    myFixture.configureByText("test.proto", "<caret>")
    CopyPasteManager.getInstance().setContents(StringSelection(JSON_WITH_DIFFERENT_FIELD_TYPES))
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResult("""
      message PastedObject {
        optional string title = 1;
        optional string year = 2;
        optional string writtenBy = 3;
        repeated Actors actors = 4;
        repeated Hashtags hashtags = 5;
      }
      message Actors {
        optional string name = 1;
        optional string surname = 2;
      }
      message Hashtags {
        optional string tag = 1;
        optional uint32 id = 2;
      }
    """.trimIndent())
  }

  fun `test insert into empty proto3`() {
    myFixture.configureByText("test.proto", """
      syntax="proto3";
      
      <caret>
    """.trimIndent())
    CopyPasteManager.getInstance().setContents(StringSelection(JSON_WITH_DIFFERENT_FIELD_TYPES))
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResult("""
      syntax="proto3";

      message PastedObject {
        string title = 1;
        string year = 2;
        string writtenBy = 3;
        repeated Actors actors = 4;
        repeated Hashtags hashtags = 5;
      }
      message Actors {
        string name = 1;
        string surname = 2;
      }
      message Hashtags {
        string tag = 1;
        uint32 id = 2;
      }
    """.trimIndent())
  }

  fun `test insert into nonempty proto`() {
    myFixture.configureByText("test.proto", """
      syntax="proto3";
      
      message PastedObject {}
      
      <caret>
    """.trimIndent())
    CopyPasteManager.getInstance().setContents(StringSelection(JSON_WITH_DIFFERENT_FIELD_TYPES))
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResult("""
      syntax="proto3";

      message PastedObject {}

      message PastedObject1 {
        string title = 1;
        string year = 2;
        string writtenBy = 3;
        repeated Actors actors = 4;
        repeated Hashtags hashtags = 5;
      }
      message Actors {
        string name = 1;
        string surname = 2;
      }
      message Hashtags {
        string tag = 1;
        uint32 id = 2;
      }
    """.trimIndent())
  }
}

private val JSON_WITH_DIFFERENT_FIELD_TYPES =
  """
    
    {
      "title": "Pulp fiction",
      "year": "1994",
      "writtenBy": "Quentin Tarantino",
      "actors": [
        {
          "name": "Bruce",
          "surname": "Willis"
        },
        {
          "name": "John",
          "surname": "Travolta"
        },
        {
          "name": "Uma",
          "surname": "Thurman"
        }
      ],
      "hashtags": [
        {
          "tag": "crime",
          "id": 1
        },
        {
          "tag": "humour",
          "id": 2
        }
      ]
    }
       
  """.trimIndent()