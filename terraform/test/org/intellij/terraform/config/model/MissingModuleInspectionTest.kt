package org.intellij.terraform.config.model

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.TerraformTestUtils
import org.intellij.terraform.config.inspection.TFMissingModuleInspection
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MissingModuleInspectionTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String? = TerraformTestUtils.getTestDataPath()
  @org.junit.Test
  fun setOfModules() {
    myFixture.enableInspections(TFMissingModuleInspection::class.java)
    myFixture.copyDirectoryToProject("terraform/inspections/missing_module/.terraform", ".terraform")
    myFixture.configureByText("main.tf", """
      module "registry_short" {
        source = "terraform-aws-modules/vpc/aws"
      }

      module "registry_short_module" {
        source = "terraform-aws-modules/vpc/aws///modules/vpc-endpoints"
      }

      module "registry_full" {
        source = "registry.terraform.io/terraform-aws-modules/vpc/aws"
      }

      module "registry_full_module" {
        source = "registry.terraform.io/terraform-aws-modules/vpc/aws///modules/vpc-endpoints"
      }

      module "github_https" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc"
      }

      module "github_https_ref_branch" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc?ref=master"
      }

      module "github_https_ref_tag" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc?ref=v3.14.2"
      }

      module "github_https_ref_hash" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "github_https_ref_branch_module" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc///modules/vpc-endpoints?ref=master"
      }

      module "github_https_ref_tag_module" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc///modules/vpc-endpoints?ref=v3.14.2"
      }

      module "github_https_ref_hash_module" {
        source = "github.com/terraform-aws-modules/terraform-aws-vpc///modules/vpc-endpoints?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "github_git" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git"
      }

      module "github_git_module" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints"
      }

      module "github_git_ref_branch" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git?ref=master"
      }

      module "github_git_ref_tag" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git?ref=v3.14.2"
      }

      module "github_git_ref_hash" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "github_git_ref_branch_module" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=master"
      }

      module "github_git_ref_tag_module" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=v3.14.2"
      }

      module "github_git_ref_hash_module" {
        source = "git@github.com:terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "bitbucket" {
        source = "bitbucket.org/hashicorp/tf-test-git"
      }

      module "bitbucket_ref_branch" {
        source = "bitbucket.org/hashicorp/tf-test-git?ref=master"
      }

      module "bitbucket_ref_hash" {
        source = "bitbucket.org/hashicorp/tf-test-git?ref=9077dc1fa372aaeda1b2580eabf5627669be1e9b"
      }

      module "generic_git_https" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git"
      }

      module "generic_git_https_ref_branch" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git?ref=master"
      }

      module "generic_git_https_ref_tag" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git?ref=v3.14.2"
      }

      module "generic_git_https_ref_hash" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "generic_git_https_ref_branch_module" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=master"
      }

      module "generic_git_https_ref_tag_module" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=v3.14.2"
      }

      module "generic_git_https_ref_hash_module" {
        source = "git::https://github.com/terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=c3fd1566df23cc4a2d3447b1964956964b9830a3"
      }

      module "generic_git_ssh" {
        source = "git::ssh://git@github.com/terraform-aws-modules/terraform-aws-vpc.git"
      }

      module "generic_git_ssh_ref" {
        source = "git::ssh://git@github.com/terraform-aws-modules/terraform-aws-vpc.git?ref=v3.14.2"
      }

      module "generic_git_ssh_module" {
        source = "git::ssh://git@github.com/terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints"
      }

      module "generic_git_ssh_ref_module" {
        source = "git::ssh://git@github.com/terraform-aws-modules/terraform-aws-vpc.git///modules/vpc-endpoints?ref=v3.14.2"
      }

      <warning descr="Cannot locate module locally: Unknown reason">module "unresolved" {
        source = "git::ssh://git@github.com/not-found"
      }</warning>

    """.trimIndent())
    
    myFixture.checkHighlighting()

  }

}