package org.intellij.terraform.config.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModuleDetectionTest {

  @Test
  fun registryMatches() = assertAccepts("registry.terraform.io/hashicorp/consul/aws", "hashicorp/consul/aws")

  @Test
  fun notPublicRegistry() = assertNotAccepts("my.registry.io/hashicorp/consul/aws", "hashicorp/consul/aws")

  @Test
  fun gitSshMatch() = assertAccepts("git::ssh://git@github.com/terraform-community-modules/tf_aws_elasticsearch.git?ref=v1.1.0",
                                    "git@github.com:terraform-community-modules/tf_aws_elasticsearch.git?ref=v1.1.0")

  @Test
  fun gitSshNoMatchWithoutRev() = assertNotAccepts(
    "git::ssh://git@github.com/terraform-community-modules/tf_aws_elasticsearch.git?ref=v1.1.0",
    "git@github.com:terraform-community-modules/tf_aws_elasticsearch.git")

  @Test
  fun gitHttpMatch() = assertAccepts("git::https://github.com/terraform-community-modules/tf_aws_bastion_s3_keys.git",
                                     "github.com/terraform-community-modules/tf_aws_bastion_s3_keys")

  @Test
  fun gitGitWithDirs() = assertAccepts("git::ssh://git@gitserver.com/mygroup/myrepo.git//myfolder?ref=myBranch",
                                       "git@gitserver.com/mygroup/myrepo.git//myfolder?ref=myBranch")

  @Test
  fun gitGitWithMisplacedDir() = assertAccepts("git::ssh://git@github.com/MyRepo/my-project.git?ref=v1.3.0//infrastructure/modules/vpc",
                                               "git@github.com:MyRepo/my-project.git//infrastructure/modules/vpc?ref=v1.3.0") 
  
  @Test
  fun gitGitWithQAfterSlashes() = assertAccepts("git::ssh://git@github.com/terraform-aws-modules/terraform-aws-vpc.git//?ref=v3.14.2",
                                               "git@github.com:terraform-aws-modules/terraform-aws-vpc.git?ref=v3.14.2")

  @Test
  fun gitGitWithMisplacedDirWithScheme() = assertAccepts(
    "git::https://github.mycompany.com/project/terraform-modules?ref=v23.5.0&e=//my-sub-dir",
    "git::https://github.mycompany.com/project/terraform-modules//my-sub-dir?ref=v23.5.0&e=") 
  
  @Test
  fun gitGitWithSlashInRef() = assertAccepts(
    "git::https://github.mycompany.com/project/terraform-modules?ref=some-module%2F1.0.0&mode=1//my-sub-dir",
    "git::https://github.mycompany.com/project/terraform-modules//my-sub-dir?mode=1&ref=some-module/1.0.0") 
  
  @Test
  fun gitGitWithSlashInRefBothEncoded() = assertAccepts(
    "git::https://github.mycompany.com/project/terraform-modules?ref=some-module%2F1.0.0&mode=1//my-sub-dir",
    "git::https://github.mycompany.com/project/terraform-modules//my-sub-dir?mode=1&ref=some-module%2F1.0.0")

  @Test
  fun gitGitSshWithMisplacedDirWithScheme() = assertAccepts(
    "git::ssh://git@github.com/mycompany/terraform-aws-ecs.git?ref=v0.33.2//modules/ecs-service",
    "git::git@github.com:mycompany/terraform-aws-ecs.git//modules/ecs-service?ref=v0.33.2")

  private fun assertAccepts(fromJson: String, given: String) {
    assertTrue(ModuleDetectionUtil.sourceMatch(fromJson, given), "descriptor '$fromJson' should accept '$given'")
  }

  private fun assertNotAccepts(fromJson: String, given: String) {
    assertFalse(ModuleDetectionUtil.sourceMatch(fromJson, given), "descriptor '$fromJson' should not accept '$given'")
  }
}