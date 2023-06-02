package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.reflect.KClass

abstract class DtsInspectionTest(private val inspectionClass: KClass<out LocalInspectionTool>) : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "testData/inspections"

    override fun setUp() {
        super.setUp()

        myFixture.enableInspections(inspectionClass.java)
    }
    protected fun doTestHighlighting(extension: String = "dtsi") {
        val path = "${basePath}/${getTestName(false)}.${extension}"

        myFixture.configureByFile(path)
        myFixture.checkHighlighting()
    }
}