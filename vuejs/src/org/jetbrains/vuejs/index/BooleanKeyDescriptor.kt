// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

class BooleanKeyDescriptor : KeyDescriptor<Boolean> {
  override fun getHashCode(value: Boolean): Int = value.hashCode()

  override fun isEqual(val1: Boolean, val2: Boolean): Boolean = val1 == val2

  override fun save(out: DataOutput, value: Boolean) = out.writeBoolean(value)

  override fun read(`in`: DataInput): Boolean = `in`.readBoolean()

}