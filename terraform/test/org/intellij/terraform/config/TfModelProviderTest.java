// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.config.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TfModelProviderTest extends LightPlatformTestCase {
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

  public void testAllResourcesForSameProviderHasSamePrefix() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    Set<String> exceptions = Set.of("marcozj/centrify", "max-gabriel-susman/gaia", "catchpoint/catchpoint");
    model.allProviders().iterator().forEachRemaining(provider -> {
      String providerFullName = provider.getFullName().toLowerCase(Locale.getDefault());
      final Set<String> names = new HashSet<>();
      List<ResourceType> resourceTypes = model.getResourcesByProvider().get(providerFullName);
      if (resourceTypes != null) {
        resourceTypes.iterator().forEachRemaining(resource -> {
          String typePrefix = StringUtil.substringBefore(resource.getType(), "_");
          if (typePrefix != null) {
            names.add(typePrefix);
          }
        });
      }
      if (!names.isEmpty() && !exceptions.contains(providerFullName)) {
        assertSame("%s does not have the same prefix for its resources".formatted(providerFullName), 1, names.size());
      }
    });
  }

  public void testAllDatasourcesForSameProviderHasSamePrefix() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);
    Set<String> exceptions = Set.of("cloudposse/awsutils", "marcozj/centrify", "axiotl/nftower");
    model.allProviders().iterator().forEachRemaining(provider -> {
      String providerFullName = provider.getFullName().toLowerCase(Locale.getDefault());
      final Set<String> names = new HashSet<>();
      List<DataSourceType> datasourceTypes = model.getDatasourcesByProvider().get(providerFullName);
      if (datasourceTypes != null) {
        datasourceTypes.iterator().forEachRemaining(resource -> {
          String typePrefix = StringUtil.substringBefore(resource.getType(), "_");
          if (typePrefix != null) {
            names.add(typePrefix.toLowerCase(Locale.getDefault()));
          }
        });
      }
      if (!names.isEmpty() && !exceptions.contains(providerFullName)) {
        assertSame("%s does not have the same prefix for its datasources".formatted(providerFullName), 1, names.size());
      }
    });
  }


  public void testResourceWithSimilarNameInDifferentProviders() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    assertNotNull(model);

    List<String> resources = new ArrayList<>();
    model.allResources().iterator().forEachRemaining(it -> {
      if (it.getType().equals("google_sql_database")) {
        resources.add(it.getType()+": "+it.getProvider().getFullName());
      }
    });

    List<String> datasources = new ArrayList<>();
    model.allDatasources().iterator().forEachRemaining(it -> {
      if (it.getType().equals("google_compute_instance")) {
        datasources.add(it.getType()+": "+it.getProvider().getFullName());
      }
    });
    assertContainsElements(resources, "google_sql_database: DrFaust92/google", "google_sql_database: hashicorp/google-beta", "google_sql_database: hashicorp/google");
    assertContainsElements(datasources, "google_compute_instance: DrFaust92/google", "google_compute_instance: hashicorp/google-beta", "google_compute_instance: hashicorp/google");
  }

  public void testProvisionersLoaded() {
    final TypeModel model = TypeModelProvider.Companion.getGlobalModel();
    List<@NotNull ProvisionerType> provisionerTypes = model.getProvisioners().stream().filter(prov -> prov.getType().contains("chef")).toList();
    assertEquals(1, provisionerTypes.size());
  }



}
