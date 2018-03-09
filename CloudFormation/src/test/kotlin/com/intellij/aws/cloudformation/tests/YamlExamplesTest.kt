package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationProblem
import com.intellij.aws.cloudformation.inspections.YamlUnresolvedReferencesInspection
import com.intellij.codeInspection.InspectionManager
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
  fun test13() = runTest("getatt-nested-stack.yaml")
  fun test14() = runTest("select.yaml")
  fun test15() = runTest("nested-sub-function.yaml")
  fun test16() = runTest("serverless-swagger-cors.yaml")
  fun test17() = runTest("serverless-kinesis-stream.yaml")
  fun test18() = runTest("serverless-iot-backend.yaml")

  fun testParameters1() = runTest("parameters1.yaml")
  fun testParameters2() = runTest("parameters2.yaml")
  fun testParameters3() = runTest("parameters3.yaml")
  fun testParameters4() = runTest("parameters4.yaml")
  fun testParameters5() = runTest("parameters5.yaml")
  fun testParameters6() = runTest("parameters6.yaml")
  fun testParameters7() = runTest("parameters7.yaml")

  fun testFindInMapBroken() = runTest("findinmap_broken.yaml")
  fun testFnSplit() = runTest("fn-split.yaml")
  fun testFnGetCidr() = runTest("fn-getcidr.yaml")
  fun testFnJoinCommaDelimitedList() = runTest("fn-join-commadelimitedlist.yaml")
  fun testRefSingleQuotes() = runTest("ref_single_quotes.yaml")

  fun runTest(fileName: String) {
    println("Working on $fileName")

    configureByFile(fileName)

    val parsed = CloudFormationParser.parse(myFile)
    val inspected = CloudFormationInspections.inspectFile(parsed)

    val unresolvedReferenceProblems = YamlUnresolvedReferencesInspection()
        .checkFile(myFile, InspectionManager.getInstance(myFile.project), true)!!
        .sortedBy { it.psiElement.textOffset }
        .map { CloudFormationProblem(it.psiElement, it.descriptionTemplate) }

    TestUtil.checkContent(
        File(testDataPath, "$fileName.expected"),
        TestUtil.renderProblems(myFile, parsed.problems + inspected.problems + unresolvedReferenceProblems)
    )
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("examples/yaml")
  }
}
