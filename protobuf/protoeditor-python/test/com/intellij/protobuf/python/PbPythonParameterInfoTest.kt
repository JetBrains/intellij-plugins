package com.intellij.protobuf.python

import com.intellij.protobuf.gencodeutils.ParameterInfoExpectationMarker

class PbPythonParameterInfoTest : PbPythonTestBase() {

  fun test() = runWithGeneratedPb("all.proto") { context ->
    configureUser("param_info.py.test", context)

    testExpectations(ParameterInfoExpectationMarker::parseExpectations) { expectation, lineNumber ->
      val hintText = myFixture.parameterInfoAtCaret
                     ?: throw AssertionError("No parameter info found at line $lineNumber")

      expectation.checkParameterInfo(hintText, lineNumber)
    }
  }
}
