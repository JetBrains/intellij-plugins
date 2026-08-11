// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object VueCodeInformationSerializer :
  KSerializer<VueCodeInformation> {

  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("VueCodeInformation")

  override fun serialize(
    encoder: Encoder,
    value: VueCodeInformation,
  ) {
    encoder.encodeStructure(descriptor) {}
  }

  override fun deserialize(
    decoder: Decoder,
  ): VueCodeInformation =
    decoder.decodeStructure(descriptor) {
      VueCodeInformation()
    }
}
