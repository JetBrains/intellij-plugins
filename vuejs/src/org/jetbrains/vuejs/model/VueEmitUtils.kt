// Copyright 2000-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType

const val EMIT_METHOD_EVENT_PARAM = "event"
const val EMIT_METHOD_REST_PARAM = "args"

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
  return TypeScriptJSFunctionTypeImpl(typeSource, emptyList(), signatureParameters, null, JSVoidType(typeSource))
}

fun createDefaultEmitCallSignature(source: JSTypeSource): JSFunctionType =
  TypeScriptJSFunctionTypeImpl(source, emptyList(), listOf(createEmitEventParam(source), createEmitRestParam(source)), null, JSVoidType(source))

fun createEmitEventParam(name: String, source: JSTypeSource) =
  createEmitEventParam(JSStringLiteralTypeImpl(name, false, source))

fun createEmitEventParam(source: JSTypeSource) =
  createEmitEventParam(JSStringType(true, source, JSTypeContext.INSTANCE))

fun createEmitEventParam(type: JSType) =
  JSParameterTypeDecoratorImpl(EMIT_METHOD_EVENT_PARAM, type, false, false, true)

fun createEmitRestParam(source: JSTypeSource) =
  JSParameterTypeDecoratorImpl(EMIT_METHOD_REST_PARAM, JSAnyType.get(source), false, true, true)