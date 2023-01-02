// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.lsp

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.internal.bind.TypeAdapters
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.lsp.LanguageServerConnectorStdio
import com.intellij.lsp.LspServerDescriptor
import java.io.IOException

class PrismaServerConnector(serverDescriptor: LspServerDescriptor, processHandler: OSProcessHandler) :
  LanguageServerConnectorStdio(serverDescriptor, processHandler) {

  override fun configureMessageJsonSerializer(gson: GsonBuilder) {
    // temp workaround for https://github.com/prisma/language-tools/issues/1086
    gson.registerTypeAdapterFactory(INTEGER_FACTORY)
  }

  companion object {
    private val INTEGER = object : TypeAdapter<Number>() {
      @Throws(IOException::class)
      override fun read(`in`: JsonReader): Number? {
        if (`in`.peek() == JsonToken.NULL) {
          `in`.nextNull()
          return null
        }
        return try {
          `in`.nextInt()
        }
        catch (e: NumberFormatException) {
          if (`in`.path.endsWith("range.end.character")) {
            `in`.skipValue()
            return Int.MAX_VALUE
          }

          throw JsonSyntaxException(e)
        }
      }

      @Throws(IOException::class)
      override fun write(out: JsonWriter, value: Number?) {
        if (value == null) {
          out.nullValue()
        }
        else {
          out.value(value.toInt())
        }
      }
    }
    private val INTEGER_FACTORY = TypeAdapters.newFactory(
      Int::class.javaPrimitiveType, Int::class.java, INTEGER)
  }
}

