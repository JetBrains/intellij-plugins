package com.intellij.protobuf.python

import com.intellij.protobuf.python.PbPythonSourceContext.ApiVersion
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

/**
 * Instead of testing all real functions, we add and check for unique marker methods `{Type}Function`.
 * Example:
 * ```py
 * from google.protobuf.message import Message
 *
 * class Timestamp(Message):
 *     def TimestampFunction(self): ...
 * ```
 */
internal object PbPythonTestMocks {

  fun setupProtobufMocks(fixture: CodeInsightTestFixture) {
    pythonFile(fixture, "google/__init__.py")
    pythonFile(fixture, "google/protobuf/__init__.py")

    pythonFile(fixture, "google/protobuf/message.py") {
      pythonClass("Message")
    }

    pythonFile(fixture, "google/protobuf/internal/enum_type_wrapper.py") {
      pythonClass("EnumTypeWrapper")
    }

    pythonFile(fixture, "google/protobuf/descriptor.py") {
      pythonClass("FieldDescriptor")
    }

    pythonFile(fixture, "google/protobuf/internal/containers.py") {
      import("collections.abc", "MutableMapping")
      import("collections.abc", "MutableSequence")
      import("collections.abc", "Sequence")
      import("typing", "TypeVar")

      import("google.protobuf.message", "Message")

      typeVar("_T")
      typeVar("_K")
      typeVar("_V")
      // Optional bounds for generics (from Typeshed):
      //typeVar("_ScalarV", bound = "bool | int | float | str | bytes")
      //typeVar("_MessageV", bound = "Message")

      pythonClass("BaseContainer", "Sequence")
      pythonClass("RepeatedScalarFieldContainer", "BaseContainer[_T], MutableSequence[_T]") {
        pythonFunction("append", "value: _T")
      }
      pythonClass("RepeatedCompositeFieldContainer", "BaseContainer[_T], MutableSequence[_T]") {
        pythonFunction("append", "value: _T")
        pythonFunction("add", "**kwargs: Any", returnType = "_T")
      }

      pythonClass("ScalarMap", "MutableMapping[_K, _V]") {
        pythonFunction("__getitem__", "key: _K", returnType = "_V")
      }
      pythonClass("MessageMap", "MutableMapping[_K, _V]") {
        pythonFunction("__getitem__", "key: _K", returnType = "_V")
      }
    }

    for (apiVersion in ApiVersion.entries) {
      val suffix = apiVersion.suffix

      pythonFile(fixture, "google/protobuf/any$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Any", "Message")
      }

      pythonFile(fixture, "google/protobuf/api$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Api", "Message")
        pythonClass("Method", "Message")
        pythonClass("Mixin", "Message")
      }
      pythonFile(fixture, "google/protobuf/duration$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Duration", "Message")
      }

      pythonFile(fixture, "google/protobuf/empty$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Empty", "Message")
      }

      pythonFile(fixture, "google/protobuf/field_mask$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("FieldMask", "Message")
      }

      pythonFile(fixture, "google/protobuf/source_context$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("SourceContext", "Message")
      }

      pythonFile(fixture, "google/protobuf/struct$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Struct", "Message")
        pythonClass("Value", "Message")
        pythonClass("ListValue", "Message")
      }

      pythonFile(fixture, "google/protobuf/timestamp$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Timestamp", "Message")
      }

      pythonFile(fixture, "google/protobuf/type$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("Type", "Message")
        pythonClass("Field", "Message")
        pythonClass("Enum", "Message")
        pythonClass("EnumValue", "Message")
        pythonClass("Option", "Message")
      }

      pythonFile(fixture, "google/protobuf/wrappers$suffix.pyi") {
        import("google.protobuf.message", "Message")
        pythonClass("DoubleValue", "Message")
        pythonClass("FloatValue", "Message")
        pythonClass("Int64Value", "Message")
        pythonClass("UInt64Value", "Message")
        pythonClass("Int32Value", "Message")
        pythonClass("UInt32Value", "Message")
        pythonClass("BoolValue", "Message")
        pythonClass("StringValue", "Message")
        pythonClass("BytesValue", "Message")
      }

      generatedPy(fixture, "google/protobuf/any$suffix.py")
      generatedPy(fixture, "google/protobuf/api$suffix.py")
      generatedPy(fixture, "google/protobuf/descriptor$suffix.py")
      generatedPy(fixture, "google/protobuf/duration$suffix.py")
      generatedPy(fixture, "google/protobuf/empty$suffix.py")
      generatedPy(fixture, "google/protobuf/field_mask$suffix.py")
      generatedPy(fixture, "google/protobuf/source_context$suffix.py")
      generatedPy(fixture, "google/protobuf/struct$suffix.py")
      generatedPy(fixture, "google/protobuf/timestamp$suffix.py")
      generatedPy(fixture, "google/protobuf/type$suffix.py")
      generatedPy(fixture, "google/protobuf/wrappers$suffix.py")
    }
  }

  private fun pythonFile(
    fixture: CodeInsightTestFixture,
    path: String,
    builder: PythonFileBuilder.() -> Unit = {},
  ) {
    val fileBuilder = PythonFileBuilder()
    fileBuilder.builder()
    fixture.addFileToProject(path, fileBuilder.build())
  }

  private fun generatedPy(
    fixture: CodeInsightTestFixture,
    path: String,
  ) {
    val protoFilePathBase = ApiVersion.entries
      .fold(path) { acc, version -> acc.removeSuffix("${version.suffix}.py") }

    val header = """
      # -*- coding: utf-8 -*-
      # Generated by the protocol buffer compiler.  DO NOT EDIT!
      # NO CHECKED-IN PROTOBUF GENCODE
      # source: $protoFilePathBase.proto
    """.trimIndent()
    fixture.addFileToProject(path, header)
  }

  private class PythonClassBuilder(private val content: StringBuilder) {
    fun pythonFunction(name: String, vararg parameters: String, returnType: String? = null) {
      val params = (listOf("self") + parameters.toList()).joinToString(", ")
      val returnAnnotation = if (returnType != null) " -> $returnType" else ""
      content.appendLine("  def $name($params)$returnAnnotation: ...")
    }
  }

  private class PythonFileBuilder {
    private val content = StringBuilder()

    fun import(module: String) {
      content.appendLine("import $module")
    }

    fun import(from: String, what: String) {
      content.appendLine("from $from import $what")
    }

    fun typeVar(name: String, bound: String? = null) {
      val boundParam = if (bound != null) ", bound=$bound" else ""
      content.appendLine("$name = TypeVar(\"$name\"$boundParam)")
    }

    fun pythonClass(
      name: String,
      superclass: String? = null,
      builder: PythonClassBuilder.() -> Unit = {},
    ) {
      val inheritance = if (superclass != null) "($superclass)" else ""

      content.appendLine("class $name$inheritance:")
      val classBuilder = PythonClassBuilder(content)
      classBuilder.pythonFunction("${name}Function")
      classBuilder.builder()
    }

    fun build(): String = content.toString().trimEnd()
  }
}
