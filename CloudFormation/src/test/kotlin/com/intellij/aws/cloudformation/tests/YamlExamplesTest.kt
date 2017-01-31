package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import java.io.File

class YamlExamplesTest : LightPlatformCodeInsightTestCase() {
  fun test01() = runTest("cf-static-website-template.yaml")
  fun test02() = runTest("demo-2013-07-02.yaml")
  fun test03() = runTest("lab8-cfn-init-sample-solution.yaml")
  fun test04() = runTest("NetworkStack.yaml")
  fun test05() = runTest("nginx.yaml")
  fun test06() = runTest("Provision.yaml")
  fun test07() = runTest("RDS_MySQL_With_Read_Replica.yaml")
  fun test08() = runTest("RDSStack.yaml")
  fun test09() = runTest("samTemplate.yaml")
  fun test10() = runTest("stemflow-apigateway.yml")
  fun test11() = runTest("stemflow-vpc.yml")
  fun test12() = runTest("WordPress_Single_Instance.yaml")

  fun runTest(fileName: String) {
    println("Working on $fileName")

    configureByFile(fileName)

    val parsed = CloudFormationParser.parse(myFile)
    val inspectedProblems = CloudFormationInspections.inspectFile(parsed)

    TestUtil.checkContent(
        File(testDataPath, "$fileName.expected"),
        TestUtil.renderProblems(myFile, parsed.problems + inspectedProblems.problems)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("examples/yaml")
  }
}
