package org.intellij.terraform.config.model

import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.options.advanced.withAdvancedSettingValue
import com.intellij.openapi.util.Disposer
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.config.model.local.TERRAFORM_LOCK_FILE_NAME
import org.intellij.terraform.config.model.local.TFLocalMetaEntity
import org.intellij.terraform.config.util.TfCommandLineServiceMock
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TfProjectSettings
import org.junit.Assert
import org.junit.Assume
import java.nio.file.Files

open class TfLocalMetadataTest : BasePlatformTestCase() {

  override fun tearDown() {
    try {
      TfCommandLineServiceMock.instance.throwErrorsIfAny()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    TfCommandLineServiceMock.instance.clear() // to avoid getting errors from previous tests
    TypeModelProvider.globalModel // ensure loaded, to avoid falling on the timeout
    myFixture.project.service<TfProjectSettings>().toolPath = TfToolType.TERRAFORM.executableName
  }

  override fun runInDispatchThread(): Boolean = false

  private fun genDoModel(dummyPropName: String) = """
            {
              "format_version": "1.0",
              "provider_schemas": {
                "registry.terraform.io/digitalocean/digitalocean": {
                  "provider": {
                    "version": 0,
                    "block": {}
                  },
                  "resource_schemas": {
                    "digitalocean_droplet": {
                      "version": 1,
                      "block": {
                        "attributes": {
                          "id": {
                            "type": "string",
                            "description_kind": "plain",
                            "optional": true,
                            "computed": true
                          },
                          "monitoring": {
                            "type": "bool",
                            "description_kind": "plain",
                            "optional": true
                          },
                          "name": {
                            "type": "string",
                            "description_kind": "plain",
                            "required": true
                          },
                          "$dummyPropName": {
                            "type": "string",
                            "description_kind": "plain",
                            "required": true
                          }
                        },
                        "description_kind": "plain"
                      }
                    }
                  }
                }
              }
            }
    
          """.trimIndent()

  private val MY_DO_LOCK = """
        # This file is maintained automatically by "terraform init".
        # Manual edits may be lost in future updates.
  
        provider "registry.terraform.io/digitalocean/digitalocean" {
          version     = "2.34.1"
          constraints = "~> 2.0"
          hashes = [
            "h1:5tfXRq80lhTUCYxAqcUGL8BjR3SSTk+ggiW20UvK+JA=",
            "zh:022d4c97af3d022d4e3735a81c6a7297aa43c3b28a8cecaa0ff58273a5677e2e",
            "zh:1922f86d5710707eb497fbebcb1a1c5584c843a7e95c3900d750d81bd2785204",
            "zh:1b7ab7c67a26c399eb5aa8a7a695cb59279c6a1a562ead3064e4a6b17cdacabe",
            "zh:1dc666faa2ec0efc32329b4c8ff79813b54741ef1741bc42d90513e5ba904048",
            "zh:220dec61ffd9448a91cca92f2bc6642df10db57b25d3d27036c3a370e9870cb7",
            "zh:262301545057e654bd6193dc04b01666531fccfcf722f730827695098d93afa7",
            "zh:63677684a14e6b7790833982d203fb2f84b105ad6b9b490b3a4ecc7043cdba81",
            "zh:67a2932227623073aa9431a12916b52ce1ccddb96f9a2d6cdae2aaf7558ccbf8",
            "zh:70dfc6ac33ee140dcb29a971df7eeb15117741b5a75b9f8486c5468c9dd28f24",
            "zh:7e3b3b62754e86442048b4b1284e10807e3e58f417e1d59a4575dd29ac6ba518",
            "zh:7e6fe662b1e283ad498eb2549d0c2260b908ab5b848e05f84fa4acdca5b4d5ca",
            "zh:9c554170f20e659222896533a3a91954fb1d210eea60de05aea803b36d5ccd5d",
            "zh:ad2f64d758bd718eb39171f1c31219900fd2bfb552a14f6a90b18cfd178a74b4",
            "zh:cfce070000e95dfe56a901340ac256f9d2f84a73bf62391cba8a8e9bf1f857e0",
            "zh:d5ae30eccd53ca7314157e62d8ec53151697ed124e43b24b2d16c565054730c6",
            "zh:fbe5edf5337adb7360f9ffef57d02b397555b6a89bba68d1b60edfec6e23f02c",
          ]
        }
  
      """.trimIndent()

  private fun genInspectedMain(dummyPropName: String) = """
          terraform {
            required_providers {
              digitalocean = {
                source  = "digitalocean/digitalocean"
                version = "~> 2.0"
              }
            }
          }
    
          provider "digitalocean" {
            token = "11"
          }
    
          resource "digitalocean_droplet" <warning descr="Missing required properties: $dummyPropName, name">"web"</warning> {
          
          }
        """.trimIndent()

  private val localSchemaService: LocalSchemaService
    get() = project.service<LocalSchemaService>()

  private fun loadAndCheckDoMetadata(dummyPropName: String) {
    TfCommandLineServiceMock.instance.mockCommandLine(
      "$terraformExe providers schema -json", genDoModel(dummyPropName),
      testRootDisposable)

    myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)

    myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
    myFixture.configureByText("main.tf", genInspectedMain(dummyPropName))
    myFixture.doHighlighting() // to trigger model building
    timeoutRunBlocking {
      localSchemaService.awaitModelsReady()
    }
    myFixture.testHighlighting("main.tf")
  }

  fun testLocalMetadataLoaded() {
    loadAndCheckDoMetadata("dummyProp")
    timeoutRunBlocking {
      waitUntil("one metadata file remains") {
        1L == Files.list(localSchemaService.localModelPath).use { it.count() }
      }
    }
  }

  fun testLocalMetadataUpdated() {
    // setup prev meta
    loadAndCheckDoMetadata("dummyProp")
    // test metadata refreshed
    loadAndCheckDoMetadata("dummyPro2")
  }

  fun testLocalMetadataNotUpdatedIfForbidden() {
    // setup prev meta
    loadAndCheckDoMetadata("dummyProp1")
    assertFalse("Commands should have been executed", TfCommandLineServiceMock.instance.requestsToVerify().isEmpty())

    withAdvancedSettingValue("org.intellij.terraform.config.build.metadata.auto", false) {
      // testing no process was started implicitly
      myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)
      myFixture.configureByText("main.tf", genInspectedMain("dummyProp1"))
      myFixture.doHighlighting() // should not trigger model building
      timeoutRunBlocking {
        localSchemaService.awaitModelsReady()
      }
      myFixture.testHighlighting("main.tf")
      assertTrue("Commands should have been not executed", TfCommandLineServiceMock.instance.requestsToVerify().isEmpty())
    }
  }

  fun testActionWorksEvenIfLoadingIsForbidden() =
    withAdvancedSettingValue("org.intellij.terraform.config.build.metadata.auto", false) {
      timeoutRunBlocking {
        TfCommandLineServiceMock.instance.mockCommandLine(
          "$terraformExe providers schema -json", genDoModel("dummyProp1"),
          testRootDisposable)

        myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)
        myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
        myFixture.configureByText("main.tf", genInspectedMain("dummyProp1"))
        withContext(Dispatchers.EDT) {
          writeIntentReadAction {
            myFixture.testAction(ActionUtil.getAction("TfGenerateLocalMetadataAction")!!)
          }
        }
        waitUntil { TfCommandLineServiceMock.instance.requestsToVerify().isNotEmpty() }
        localSchemaService.awaitModelsReady()
        myFixture.testHighlighting("main.tf")
      }
    }

  fun testNewLockPickedUp() = withAdvancedSettingValue("org.intellij.terraform.config.build.metadata.eagerly", true) {
    TfCommandLineServiceMock.instance.mockCommandLine(
      "$terraformExe providers schema -json", genDoModel("dummyProp"),
      testRootDisposable)

    val lockFile = myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)

    myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
    timeoutRunBlocking {
      localSchemaService.awaitModelsReady()
      readAction {
        val entities = WorkspaceModel.getInstance(project).currentSnapshot.entities(TFLocalMetaEntity::class.java).toList()
        Assert.assertEquals(entities.single().lockFile.virtualFile, lockFile.virtualFile)
      }
    }
  }

  fun testNewLockNotPickedUpIfLazy() = withAdvancedSettingValue("org.intellij.terraform.config.build.metadata.eagerly", false) {
    val lock = myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)

    myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
    timeoutRunBlocking {
      localSchemaService.awaitModelsReady()
      readAction {
        val entities = WorkspaceModel.getInstance(project).currentSnapshot.entities(TFLocalMetaEntity::class.java)
          .filter { it.lockFile.virtualFile != lock.virtualFile }
          .toList()
        assertEmpty(entities)
      }
    }
  }

  fun testPickUpOldMetaOnError() {
    loadAndCheckDoMetadata("dummyProp")
    TfCommandLineServiceMock.instance.mockCommandLine(
      "$terraformExe providers schema -json", "", 1,
      testRootDisposable)

    myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)

    myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
    myFixture.configureByText("main.tf", genInspectedMain("dummyProp"))
    myFixture.doHighlighting()
    timeoutRunBlocking {
      waitUntil { TfCommandLineServiceMock.instance.requestsToVerify().isNotEmpty() }
      localSchemaService.awaitModelsReady()
    }
    myFixture.testHighlighting("main.tf")
  }

  fun testNotPumpOnError() {
    TfCommandLineServiceMock.instance.mockCommandLine(
      "$terraformExe providers schema -json", "", 1,
      testRootDisposable)

    myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, MY_DO_LOCK)
    myFixture.enableInspections(HCLBlockMissingPropertyInspection::class.java)
    myFixture.configureByText("main.tf", genInspectedMain("dummyProp"))
    myFixture.doHighlighting()
    timeoutRunBlocking {
      waitUntil {
        localSchemaService.awaitModelsReady()
        TfCommandLineServiceMock.instance.requestsToVerify().isNotEmpty()
      }
    }
    (0..5).forEach { i ->
      timeoutRunBlocking {
        localSchemaService.awaitModelsReady()
      }
      myFixture.doHighlighting()
    }

    assertEquals("", TfCommandLineServiceMock.instance.requestsToVerify().joinToString("\n"))
  }

  private val terraformExe: String
    get() = TfToolType.TERRAFORM.executableName

}

class TfEagerLocalMetadataTest : TfLocalMetadataTest() {

  override fun setUp() {
    super.setUp()
    val currentValue = AdvancedSettings.getBoolean("org.intellij.terraform.config.build.metadata.eagerly")
    Assume.assumeFalse(currentValue)
    AdvancedSettings.setBoolean("org.intellij.terraform.config.build.metadata.eagerly", true)

    Disposer.register(testRootDisposable, {
      AdvancedSettings.setBoolean("org.intellij.terraform.config.build.metadata.eagerly", currentValue)
    })
  }
}