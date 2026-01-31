// Copyright 2000-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSParameterTypeDecoratorImpl
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.TypeScriptJSFunctionTypeImpl

const val EMIT_METHOD_EVENT_PARAM: String = "event"
const val EMIT_METHOD_REST_PARAM: String = "args"

val VueEmitCall.callSignature: JSFunctionType
  get() = createFunctionType(true)

val VueEmitCall.handlerSignature: JSFunctionType
  get() = createFunctionType(false)

private fun VueEmitCall.createFunctionType(addEventType: Boolean): JSFunctionType {
  val typeSource = JSTypeSourceFactory.createTypeSource(source, true).copyWithNewLanguage(JSTypeSource.SourceLanguage.TS)
  val signatureParameters: MutableList<JSParameterTypeDecorator> = if (addEventType) {
    mutableListOf(createEmitEventParam(name, typeSource))
  }
  else {
    mutableListOf()
  }

  if (params.isNotEmpty()) {
    signatureParameters.addAll(params)
  }
  else if (!hasStrictSignature) {
    signatureParameters.add(createEmitRestParam(typeSource))
  }
  return TypeScriptJSFunctionTypeImpl(typeSource, emptyList(), signatureParameters, null, JSNamedTypeFactory.createVoidType(typeSource))
}

fun createDefaultEmitCallSignature(source: JSTypeSource): JSFunctionType =
  TypeScriptJSFunctionTypeImpl(source, emptyList(), listOf(createEmitEventParam(source), createEmitRestParam(source)), null,
                               JSNamedTypeFactory.createVoidType(source))

fun createEmitEventParam(name: String, source: JSTypeSource): JSParameterTypeDecorator =
  createEmitEventParam(JSStringLiteralTypeImpl(name, false, source))

fun createEmitEventParam(source: JSTypeSource): JSParameterTypeDecorator =
  createEmitEventParam(JSNamedTypeFactory.createStringPrimitiveType(source))

fun createEmitEventParam(type: JSType): JSParameterTypeDecorator =
  JSParameterTypeDecoratorImpl(EMIT_METHOD_EVENT_PARAM, type, false, false, true)

fun createEmitRestParam(source: JSTypeSource): JSParameterTypeDecorator =
  JSParameterTypeDecoratorImpl(EMIT_METHOD_REST_PARAM, JSAnyType.get(source), false, true, true)
