// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.config.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

public class TerraformModelProviderTest extends LightPlatformTestCase {
  public void testModelIsLoaded() {
    //noinspection unused
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
  }

  public void testProperlyParsedOsDiskConfig() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    final ResourceType azurerm_linux_virtual_machine = model.getResourceType("azurerm_linux_virtual_machine", null);
    assertNotNull(azurerm_linux_virtual_machine);
    final Map<String, PropertyOrBlockType> properties = azurerm_linux_virtual_machine.getProperties();
    final PropertyOrBlockType os_disk = properties.get("os_disk");
    assertNotNull(os_disk);
    final BlockType os_disk_block = (BlockType)os_disk;
    assertNotNull(os_disk_block);
    Map<String, PropertyOrBlockType> properties3 = os_disk_block.getProperties();
    final PropertyOrBlockType diff_disk_settings = properties3.get("diff_disk_settings");
    assertNotNull(diff_disk_settings);
    final BlockType diff_disk_settings_block = (BlockType)diff_disk_settings;
    assertNotNull(diff_disk_settings_block);
    Map<String, PropertyOrBlockType> properties2 = diff_disk_settings_block.getProperties();
    assertNotNull(properties2.get("option"));
    assertNotNull(properties2.get("placement"));
  }

  // Test for #67
  public void test_azurerm_kubernetes_cluster_values() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    final ResourceType azurerm_kubernetes_cluster = model.getResourceType("azurerm_kubernetes_cluster", null);
    assertNotNull(azurerm_kubernetes_cluster);
    final Map<String, PropertyOrBlockType> properties = azurerm_kubernetes_cluster.getProperties();

    final PropertyOrBlockType default_node_pool = properties.get("default_node_pool");
    assertNotNull(default_node_pool);
    final BlockType default_node_pool_block = (BlockType)default_node_pool;
    assertNotNull(default_node_pool_block);

    Map<String, PropertyOrBlockType> properties3 = default_node_pool_block.getProperties();
    final PropertyOrBlockType linux_os_config = properties3.get("linux_os_config");
    assertNotNull(linux_os_config);
    final BlockType linux_os_config_block = (BlockType)linux_os_config;
    assertNotNull(linux_os_config_block);

    Map<String, PropertyOrBlockType> properties2 = linux_os_config_block.getProperties();
    PropertyOrBlockType sysctl_config = properties2.get("sysctl_config");
    assertTrue(sysctl_config instanceof BlockType);
    assertNotNull(sysctl_config);
    assertFalse(sysctl_config.getRequired());

    Map<String, PropertyOrBlockType> properties1 = ((BlockType)sysctl_config).getProperties();
    PropertyOrBlockType kernel_threads_max = properties1.get("kernel_threads_max");
    assertNotNull(kernel_threads_max);
  }

  public void test_azurerm_storage_encryption_scope_depends_on() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    final DataSourceType azurerm_storage_encryption_scope = model.getDataSourceType("azurerm_storage_encryption_scope", null);
    assertNotNull(azurerm_storage_encryption_scope);
    final Map<String, PropertyOrBlockType> properties = azurerm_storage_encryption_scope.getProperties();

    final PropertyOrBlockType depends_on = properties.get("depends_on");
    assertNotNull(depends_on);
    assertInstanceOf(depends_on, PropertyType.class);

    PropertyType contextProperty = (PropertyType)depends_on;
    Type type = contextProperty.getType();
    assertEquals("list", type.getPresentableText().toLowerCase());
  }

  // Have explicit mode == attr, yet it's a block
  public void test_azurerm_application_security_group_provisioner() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    final ResourceType azurerm_application_security_group = model.getResourceType("azurerm_application_security_group", null);
    assertNotNull(azurerm_application_security_group);
    final Map<String, PropertyOrBlockType> properties = azurerm_application_security_group.getProperties();

    final PropertyOrBlockType provisioner = properties.get("provisioner");
    assertNotNull(provisioner);
    assertInstanceOf(provisioner, BlockType.class);
  }

  public void test_containers_as_block_type_if_non_scalar_typed() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    assertInstanceOf(model.getResourceType("vault_audit", null).getProperties().get("path"), PropertyType.class);
    PropertyOrBlockType accessPolicyBlockType = model.getResourceType("azurerm_key_vault", null).getProperties().get("access_policy");
    assertInstanceOf(accessPolicyBlockType, BlockType.class);
    assertContainsElements(((BlockType)accessPolicyBlockType).getProperties().keySet(),
                           "application_id", "certificate_permissions", "key_permissions", "object_id", "secret_permissions",
                           "storage_permissions", "tenant_id");

  }

  // Have dynamic attributes
  public void test_external_result() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    final DataSourceType external = model.getDataSourceType("external", null);
    assertNotNull(external);
    final Map<String, PropertyOrBlockType> properties = external.getProperties();

    final PropertyOrBlockType result = properties.get("result");
    assertNotNull(result);

    assertInstanceOf(result, PropertyType.class);
    PropertyType resultAsProperty = (PropertyType)result;
    Type type = resultAsProperty.getType();
    assertEquals("map(string)", type.getPresentableText().toLowerCase());
  }

  public void test_kubernetes_provider_exec() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    ProviderType k8s = model.getProviderType("kubernetes", null);
    assertNotNull(k8s);
    PropertyOrBlockType pobt = k8s.getProperties().get("exec");
    assertNotNull(pobt);
    assertInstanceOf(pobt, BlockType.class);
    BlockType prop = (BlockType)pobt;
    assertEquals("exec({api_version=string, args=list(string), command=string, env=map(string)})",
                 prop.getPresentableText().toLowerCase());
  }

  /*
  public void testAllResourceHasProviderNameAsPrefix() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    final List<ResourceType> failedResources = new ArrayList<>();
    for (ResourceType block : model.allResources()) {
      final String rt = block.getType();
      String pt = block.getProvider().getType();
      String fullProvName = block.getProvider().getFullName();
      if (pt.equals("azure-classic")) {
        pt = "azure";
      }
      if (pt.equals("google-beta")) {
        pt = "google";
      }
      if (pt.equals("kubernetes-alpha")) {
        pt = "kubernetes";
      }
      if (pt.equals("cloudvision")) {
        pt = "cvprovider";
      }
      if (pt.equals("infinity-next")) {
        pt = "inext";
      }
      if (pt.equals("better-uptime")) {
        pt = "betteruptime";
      }
      if (pt.equals("gcorelabs")) {
        pt = "gcore";
      }
      if (pt.equals("samsungcloudplatform")) {
        pt = "scp";
      } 
      if (pt.equals("netapp-elementsw")) {
        pt = "elementsw";
      }
      if (pt.equals("cloudfoundry-v3")) {
        pt = "cloudfoundry";
      }
      if (pt.equals("dbt-cloud")) {
        pt = "dbt_cloud";
      }
      if (pt.equals("fly-io")) {
        pt = "fly";
      }
      if (pt.equals("splunk-itsi")) {
        pt = "itsi";
      }
      if (pt.equals("1password")) {
        pt = "onepassword";
      }
      if (pt.equals("sumologic-cse")) {
        pt = "sumologiccse";
      }
      if (pt.equals("zentest")) {
        pt = "zenduty";
      }
      if (pt.equals("aws-extras")) {
        pt = "awsx_lb_listener_rules";
      }
      if (pt.equals("data-utils")) {
        pt = "deep_merge";
      }
      if (pt.equals("go-cat")) {
        pt = "gocat";
      }
      if (pt.equals("http-full")) {
        pt = "http";
      }
      if (pt.equals("http-client")) {
        pt = "httpclient";
      }
      if (pt.equals("klayer")) {
        pt = "klayers";
      }
      if (pt.equals("confluent-schema-registry")) {
        pt = "schemaregistry";
      }  
      if (pt.equals("marketplace")) {
        pt = "app_tile";
      }  
      if (pt.equals("supermegaapplestest")) {
        pt = "apples";
      }  
      if (pt.equals("aws-spot-instance")) {
        pt = "aws_spot_instance";
      }    
      if (pt.equals("cloudux-utils")) {
        pt = "cloudux-utils-site";
      }   
      if (pt.equals("config-service")) {
        pt = "configuration";
      }  
      if (pt.equals("jks-trust-store")) {
        pt = "jks_trust_store";
      }   
      if (pt.equals("k8s-sops-secrets")) {
        pt = "sops_secret";
      }    
      if (pt.equals("gaia")) {
        continue; // it is known to have types with no common prefixes
      }
      if (fullProvName.equals("figma/aws-4-49-0")) {
        pt = "aws";
      }
      if (pt.equals("data-platform-kafka")) {
        pt = "kafkamanager";
      }
      if (pt.equals("skysql-beta")) {
        pt = "skysql";
      }
      if (pt.equals("centrify")) {
        if (rt.startsWith("centrify" + '_')) continue;
        if (rt.startsWith("centrifyvault" + '_')) continue;
      }
      if (pt.equals("dbtcloud")) {
        if (rt.startsWith("dbtcloud" + '_')) continue;
        if (rt.startsWith("dbt_cloud" + '_')) continue;
      }
      if (pt.equals("ks3")) {
        pt = "ksyun_ks3";
      }
      if (pt.equals("onelogin-1")) {
        pt = "onelogin";
      }
      if (pt.equals("gcp-ipam-autopilot")) {
        pt = "ipam";
      }
      if (pt.equals("port-labs")) {
        pt = "port";
      }
      if (pt.equals("appstore")) {
        pt = "applet";
      }
      if (fullProvName.equals("cisco-open/appd")) {
        pt = "appdynamicscloud";
      }
      if (fullProvName.equals("mehdiatbud/http")) {
        pt = "http-wait";
      }
      if (fullProvName.equals("cisco-open/appd")) {
        pt = "appdynamicscloud";
      }
      if (fullProvName.equals("jonwoodlief/catalog")) {
        pt = "ibm";
      }
      if (fullProvName.equals("vmware/nsxt-virtual-private-cloud")) {
        pt = "nsxt";
      }
      if (fullProvName.equals("openvpn/openvpn-cloud")) {
        if (rt.startsWith("openvpncloud" + '_')) continue;
        if (rt.startsWith("cloudconnexa" + '_')) continue;
      }
      if (fullProvName.equals("timeweb-cloud/timeweb-cloud")) {
        pt = "twc";
      }
      if (fullProvName.equals("toluna-terraform/toluna-v2")) {
        pt = "toluna";
      }
      if (fullProvName.equals("andrei-funaru/vault-starter")) {
        pt = "vaultstarter";
      }
      if (fullProvName.equals("kopicloud-ad-api/ad")) {
        pt = "kopicloud";
      }
      if (rt.equals(pt)) continue;
      if (rt.startsWith(pt + '_')) continue;
      failedResources.add(block);
    }
    then(failedResources).isEmpty();
  }

  public void testResourceWithSimilarNameInDifferentProviders() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    assertContainsElements(
      model.getResources().stream().filter(it -> it.getType().equals("google_sql_database")).map(it -> it.getType()+": "+it.getProvider().getFullName()).toList(),
      "google_sql_database: drfaust92/google", "google_sql_database: hashicorp/google-beta");
    assertContainsElements(
      model.getDataSources().stream().filter(it -> it.getType().equals("google_compute_instance")).map(it -> it.getType()+": "+it.getProvider().getFullName()).toList(),
      "google_compute_instance: drfaust92/google", "google_compute_instance: hashicorp/google-beta");
  }

  public void testDataSourcesHasProviderNameAsPrefix() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    final List<DataSourceType> failedDataSources = new ArrayList<>();
    for (DataSourceType block : model.getDataSources()) {
      final String rt = block.getType();
      String pt = block.getProvider().getType();
      String fullProvName = block.getProvider().getFullName();
      if (pt.equals("azure-classic")) {
        pt = "azure";
      }
      if (pt.equals("google-beta")) {
        pt = "google";
      }
      if (pt.equals("cloudvision")) {
        pt = "cvprovider";
      }  
      if (pt.equals("better-uptime")) {
        pt = "betteruptime";
      }  
      if (pt.equals("gcorelabs")) {
        pt = "gcore";
      } 
      if (pt.equals("samsungcloudplatform")) {
        pt = "scp";
      }    
      if (pt.equals("cloudfoundry-v3")) {
        pt = "cloudfoundry";
      }  
      if (pt.equals("dbt-cloud")) {
        pt = "dbt_cloud";
      }  
      if (pt.equals("fly-io")) {
        pt = "fly";
      }   
      if (pt.equals("splunk-itsi")) {
        pt = "itsi";
      } 
      if (pt.equals("1password")) {
        pt = "onepassword";
      }   
      if (pt.equals("sumologic-cse")) {
        pt = "sumologiccse";
      }   
      if (pt.equals("zentest")) {
        pt = "zenduty";
      }  
      if (pt.equals("aws-extras")) {
        pt = "awsx_lb_listener_rules";
      }   
      if (pt.equals("data-utils")) {
        pt = "deep_merge";
      }    
      if (pt.equals("go-cat")) {
        pt = "gocat";
      }    
      if (pt.equals("http-full")) {
        pt = "http";
      }  
      if (pt.equals("http-client")) {
        pt = "httpclient";
      }    
      if (pt.equals("klayer")) {
        pt = "klayers";
      }   
      if (pt.equals("gaia")) {
        pt = "scaffolding_data_source";
      }  
      if (pt.equals("confluent-schema-registry")) {
        pt = "schemaregistry";
      }  
      if (pt.equals("awsutils")) {
        continue; // it is known to have types with no common prefixes
      }
      if (fullProvName.equals("figma/aws-4-49-0")) {
        pt = "aws";
      }
      if (pt.equals("data-platform-kafka")) {
        pt = "kafkamanager";
      }
      if (pt.equals("skysql-beta")) {
        pt = "skysql";
      }
      if (pt.equals("hash-sum")) {
        pt = "hashsum";
      }
      if (pt.equals("centrify")) {
        if (rt.startsWith("centrify" + '_')) continue;
        if (rt.startsWith("centrifyvault" + '_')) continue;
      }
      if (pt.equals("dbtcloud")) {
        if (rt.startsWith("dbtcloud" + '_')) continue;
        if (rt.startsWith("dbt_cloud" + '_')) continue;
      }
      if (pt.equals("ks3")) {
        pt = "ksyun_ks3";
      }
      if (pt.equals("onelogin-1")) {
        pt = "onelogin";
      }
      if (pt.equals("sloth-sli")) {
        pt = "sli";
      }
      if (pt.equals("version-validator")) {
        pt = "version_validator";
      }
      if (pt.equals("alibabacloudstack")) {
        if (rt.startsWith("alibabacloudStack" + '_')) continue;
        if (rt.startsWith("alibabacloudstack" + '_')) continue;
      }
      if (fullProvName.equals("mehdiatbud/http")) {
        pt = "http-wait";
      }
      if (fullProvName.equals("cisco-open/appd")) {
        pt = "appdynamicscloud";
      }
      if (fullProvName.equals("jonwoodlief/catalog")) {
        pt = "ibm";
      }
      if (fullProvName.equals("vmware/nsxt-virtual-private-cloud")) {
        pt = "nsxt";
      }
      if (fullProvName.equals("openvpn/openvpn-cloud")) {
        if (rt.startsWith("openvpncloud" + '_')) continue;
        if (rt.startsWith("cloudconnexa" + '_')) continue;
      }
      if (fullProvName.equals("timeweb-cloud/timeweb-cloud")) {
        pt = "twc";
      }
      if (fullProvName.equals("toluna-terraform/toluna-v2")) {
        pt = "toluna";
      }
      if (fullProvName.equals("andrei-funaru/vault-starter")) {
        pt = "vaultstarter";
      }
      if (fullProvName.equals("saritasa-nest/mssql")) {
        pt = "mysql";
      }
      if (fullProvName.equals("axiotl/nftower") && rt.equals("scaffolding_example")) {
        pt = "scaffolding_example";
      }
      if (fullProvName.equals("mildred/sys") && rt.equals("uname")) {
        pt = "uname";
      }
      if (fullProvName.equals("kopicloud-ad-api/ad")) {
        pt = "kopicloud";
      }
      if (rt.equals(pt)) continue;
      if (rt.startsWith(pt + '_')) continue;
      failedDataSources.add(block);
    }
    then(failedDataSources).isEmpty();
  }
  */

  public void testProvisionersLoaded() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    List<@NotNull ProvisionerType> provisionerTypes = model.getProvisioners().stream().filter(prov -> prov.getType().contains("chef")).toList();
    assertEquals(1, provisionerTypes.size());
  }



}
