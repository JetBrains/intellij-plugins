// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.install.TfToolType

class TfExecutorUtilTest : BasePlatformTestCase() {

  fun testTerraformFileDetection() {
    val tempDir = FileUtil.createTempDirectory(this.name, null)
    val file1 = tempDir.resolve("file1.tf")
    val file2 = tempDir.resolve("file2.tf")
    file1.writeText("resource \"aws_instance\" \"example\" {}")
    file2.writeText("resource \"google_storage_bucket\" \"example\" {}")
    val vFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file1.toPath())

    requireNotNull(vFile) { "Virtual file conversion failed for ${file1.absolutePath}" }
    val toolType = getApplicableToolType(vFile)

    assertEquals(TfToolType.TERRAFORM, toolType)
  }

  fun testTofuFileDetection() {
    val tempDir = FileUtil.createTempDirectory(this.name, null)
    val file1 = tempDir.resolve("file1.tofu")
    val file2 = tempDir.resolve("file2.tofu")
    file1.writeText("resource \"aws_instance\" \"example\" {}")
    file2.writeText("resource \"google_storage_bucket\" \"example\" {}")
    val vFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file1.toPath())

    requireNotNull(vFile) { "Virtual file conversion failed for ${file1.absolutePath}" }
    val toolType = getApplicableToolType(vFile)

    assertEquals(TfToolType.OPENTOFU, toolType)
  }

  fun testMixedFileDetection() {
    val tempDir = FileUtil.createTempDirectory(this.name, null)
    val file1 = tempDir.resolve("file1.tf")
    val file2 = tempDir.resolve("file2.tofu")
    file1.writeText("resource \"aws_instance\" \"example\" {}")
    file2.writeText("resource \"google_storage_bucket\" \"example\" {}")
    val vFileTF = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file1.toPath())

    requireNotNull(vFileTF) { "Virtual file conversion failed for ${file1.absolutePath}" }
    val toolTypeTF = getApplicableToolType(vFileTF)
    assertEquals(TfToolType.OPENTOFU, toolTypeTF)


    val vFileTofu = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file2.toPath())
    requireNotNull(vFileTofu) { "Virtual file conversion failed for ${file2.absolutePath}" }
    val toolTypeTofu = getApplicableToolType(vFileTofu)
    assertEquals(TfToolType.OPENTOFU, toolTypeTofu)
  }

  fun testNoParentTerraformFileDetection() {
    val vFile = LightVirtualFile("test.tf", "resource \"aws_instance\" \"example\" {}")
    val toolType = getApplicableToolType(vFile)
    assertEquals(TfToolType.TERRAFORM, toolType)
  }

  fun testNoParentTofuFileDetection() {
    val vFile = LightVirtualFile("test.tofu", "resource \"aws_instance\" \"example\" {}")
    val toolType = getApplicableToolType(vFile)
    assertEquals(TfToolType.OPENTOFU, toolType)
  }

}