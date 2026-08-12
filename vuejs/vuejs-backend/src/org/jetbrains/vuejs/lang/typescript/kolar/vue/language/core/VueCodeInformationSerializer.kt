// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import com.intellij.lang.typescript.kolar.KolarCodeInformation.CompletionInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.NavigationInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.SemanticInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object VueCodeInformationSerializer :
  KSerializer<VueCodeInformation> {

  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("VueCodeInformation") {
      element<Boolean>("verification", isOptional = true)
      element("completion", CompletionInfo.serializer().descriptor, isOptional = true)
      element<Boolean>("semantic", isOptional = true)
      element<Boolean>("navigation", isOptional = true)
    }

  override fun serialize(
    encoder: Encoder,
    value: VueCodeInformation,
  ) {
    encoder.encodeStructure(descriptor) {
      if (value.verification == VerificationInfo.Enabled)
        encodeBooleanElement(descriptor, 0, true)
      if (value.completion != null)
        encodeSerializableElement(descriptor, 1, CompletionInfo.serializer(), value.completion)
      if (value.semantic == SemanticInfo.Enabled)
        encodeBooleanElement(descriptor, 2, true)
      if (value.navigation == NavigationInfo.Enabled)
        encodeBooleanElement(descriptor, 3, true)
    }
  }

  override fun deserialize(
    decoder: Decoder,
  ): VueCodeInformation =
    decoder.decodeStructure(descriptor) {
      var verification: VerificationInfo? = null
      var completion: CompletionInfo? = null
      var semantic: SemanticInfo? = null
      var navigation: NavigationInfo? = null

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> {
            require(decodeBooleanElement(descriptor, index))
            verification = VerificationInfo.Enabled
          }
          1 -> completion = decodeSerializableElement(descriptor, index, CompletionInfo.serializer())
          2 -> {
            require(decodeBooleanElement(descriptor, index))
            semantic = SemanticInfo.Enabled
          }
          3 -> {
            require(decodeBooleanElement(descriptor, index))
            navigation = NavigationInfo.Enabled
          }

          CompositeDecoder.DECODE_DONE -> break

          else -> error("Unexpected index: $index")
        }
      }

      VueCodeInformation(
        verification = verification,
        completion = completion,
        semantic = semantic,
        navigation = navigation,
      )
    }
}
