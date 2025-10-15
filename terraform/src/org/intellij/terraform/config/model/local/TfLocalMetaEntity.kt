// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

interface TfLocalMetaEntity : WorkspaceEntity {

  val timeStampLow: Int

  val timeStampHigh: Int

  val jsonPath: String

  val lockFile: VirtualFileUrl

  val timeStamp: Long
    get() {
      return timeStampLow.toLong() and 0xFFFFFFFFL or (timeStampHigh.toLong() shl 32)
    }

  object LockEntitySource : EntitySource

}
