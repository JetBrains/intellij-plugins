// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.stubs

import com.intellij.javascript.flex.compiled.DecompiledSwfParserDefinition
import com.intellij.lang.javascript.FlexFileElementTypes
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptAttributeListStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptClassStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptFunctionExpressionStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptFunctionStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptParameterStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.ActionScriptVariableStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.JSIncludeDirectiveStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.JSNamespaceDeclarationStubFactory
import com.intellij.lang.javascript.stubs.factories.actionscript.JSUseNamespaceDirectiveStubFactory
import com.intellij.lang.javascript.stubs.register
import com.intellij.lang.javascript.stubs.serializers.JSFileStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptAttributeListStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptClassStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptFunctionExpressionStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptFunctionStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptParameterStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.ActionScriptVariableStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.JSIncludeDirectiveStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.JSNamespaceDeclarationStubSerializer
import com.intellij.lang.javascript.stubs.serializers.actionscript.JSUseNamespaceDirectiveStubSerializer
import com.intellij.psi.stubs.StubRegistry
import com.intellij.psi.stubs.StubRegistryExtension

class ActionScriptStubRegistryExtension : StubRegistryExtension {
  override fun register(registry: StubRegistry) {
    listOf(
      FlexFileElementTypes.ECMA4_FILE,
      DecompiledSwfParserDefinition.FILE_TYPE,
    ).forEach {
      registry.registerStubSerializer(it, JSFileStubSerializer(it))
    }

    val actionScriptStubs = listOf(
      ActionScriptVariableStubFactory(),
      ActionScriptVariableStubSerializer(),

      ActionScriptFunctionStubFactory(),
      ActionScriptFunctionStubSerializer(),

      ActionScriptFunctionExpressionStubFactory(),
      ActionScriptFunctionExpressionStubSerializer(),

      JSUseNamespaceDirectiveStubFactory(),
      JSUseNamespaceDirectiveStubSerializer(),

      JSIncludeDirectiveStubFactory(),
      JSIncludeDirectiveStubSerializer(),

      JSNamespaceDeclarationStubFactory(),
      JSNamespaceDeclarationStubSerializer(),

      ActionScriptParameterStubFactory(),
      ActionScriptParameterStubSerializer(),

      ActionScriptAttributeListStubFactory(),
      ActionScriptAttributeListStubSerializer(),

      ActionScriptClassStubFactory(),
      ActionScriptClassStubSerializer(),
    )

    actionScriptStubs.forEach(registry::register)
  }
}