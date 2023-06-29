// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.config.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

public class TerraformModelProviderTest extends LightPlatformTestCase {
  public void testModelIsLoaded() {
    //noinspection unused
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
  }

  public void testProperlyParsedNetworkInterface() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);
    final ResourceType google_compute_instance = model.getResourceType("google_compute_instance");
    assertNotNull(google_compute_instance);
    final Map<String, PropertyOrBlockType> properties = google_compute_instance.getProperties();
    final PropertyOrBlockType network_interface = findProperty(properties, "network_interface");
    assertNotNull(network_interface);
    final BlockType network_interfaceBlock = (BlockType)network_interface;
    assertNotNull(network_interfaceBlock);
    final PropertyOrBlockType access_config = findProperty(network_interfaceBlock.getProperties(), "access_config");
    assertNotNull(access_config);
    final BlockType access_configBlock = (BlockType)access_config;
    assertNotNull(access_configBlock);
    assertNotNull(findProperty(access_configBlock.getProperties(), "network_tier"));
    assertNotNull(findProperty(access_configBlock.getProperties(), "nat_ip"));
  }

  // Test for #67
  public void test_aws_cloudfront_distribution_forwarded_values() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    final ResourceType aws_cloudfront_distribution = model.getResourceType("aws_cloudfront_distribution");
    assertNotNull(aws_cloudfront_distribution);
    final Map<String, PropertyOrBlockType> properties = aws_cloudfront_distribution.getProperties();

    final PropertyOrBlockType default_cache_behavior = findProperty(properties, "default_cache_behavior");
    assertNotNull(default_cache_behavior);
    final BlockType default_cache_behavior_block = (BlockType)default_cache_behavior;
    assertNotNull(default_cache_behavior_block);

    final PropertyOrBlockType forwarded_values = findProperty(default_cache_behavior_block.getProperties(), "forwarded_values");
    assertNotNull(forwarded_values);
    final BlockType forwarded_values_block = (BlockType)forwarded_values;
    assertNotNull(forwarded_values_block);

    PropertyOrBlockType query_string = findProperty(forwarded_values_block.getProperties(), "query_string");
    assertTrue(query_string.getRequired());
    assertNotNull(query_string);

    PropertyOrBlockType cookies = findProperty(forwarded_values_block.getProperties(), "cookies");
    assertNotNull(cookies);
  }

  public void test_data_aws_kms_ciphertext_context() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    final DataSourceType aws_kms_ciphertext = model.getDataSourceType("aws_kms_ciphertext");
    assertNotNull(aws_kms_ciphertext);
    final Map<String, PropertyOrBlockType> properties = aws_kms_ciphertext.getProperties();

    final PropertyOrBlockType context = findProperty(properties, "context");
    assertNotNull(context);
    assertInstanceOf(context, PropertyType.class);

    PropertyType contextProperty = (PropertyType)context;
    Type type = contextProperty.getType();
    assertEquals("map(string)", type.getPresentableText().toLowerCase());
  }

  // Have explicit mode == attr, yet it's a block
  public void test_aws_security_group_ingress() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    final ResourceType aws_security_group = model.getResourceType("aws_security_group");
    assertNotNull(aws_security_group);
    final Map<String, PropertyOrBlockType> properties = aws_security_group.getProperties();

    final PropertyOrBlockType ingress = findProperty(properties, "ingress");
    assertNotNull(ingress);
    assertInstanceOf(ingress, BlockType.class);
  }

  public void test_aws_instance_security_groups() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    final ResourceType aws_security_group = model.getResourceType("aws_instance");
    assertNotNull(aws_security_group);
    final Map<String, PropertyOrBlockType> properties = aws_security_group.getProperties();

    final PropertyOrBlockType ingress = findProperty(properties, "security_groups");
    assertNotNull(ingress);
    assertInstanceOf(ingress, PropertyType.class);
  }

  // Have dynamic attributes
  public void test_external_result() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    final DataSourceType external = model.getDataSourceType("external");
    assertNotNull(external);
    final Map<String, PropertyOrBlockType> properties = external.getProperties();

    final PropertyOrBlockType result = findProperty(properties, "result");
    assertNotNull(result);

    assertInstanceOf(result, PropertyType.class);
    PropertyType resultAsProperty = (PropertyType)result;
    Type type = resultAsProperty.getType();
    assertEquals("map(string)", type.getPresentableText().toLowerCase());
  }

  public void test_kubernetes_provider_exec() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);
    ProviderType k8s = model.getProviderType("kubernetes");
    assertNotNull(k8s);
    PropertyOrBlockType pobt = k8s.getProperties().get("exec");
    assertNotNull(pobt);
    assertInstanceOf(pobt, BlockType.class);
    BlockType prop = (BlockType)pobt;
    assertEquals("exec({api_version=string, args=list(string), command=string, env=map(string)})",
                 prop.getPresentableText().toLowerCase());
  }

  public void testAllResourceHasProviderNameAsPrefix() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);
    final List<ResourceType> failedResources = new ArrayList<>();
    for (ResourceType block : model.getResources()) {
      final String rt = block.getType();
      String pt = block.getProvider().getType();
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
      if (pt.equals("aws-4-49-0")) {
        pt = "aws";
      }
      if (pt.equals("data-platform-kafka")) {
        pt = "kafkamanager";
      }
      if (pt.equals("skysql-beta")) {
        pt = "skysql";
      }
      if (rt.equals(pt)) continue;
      if (rt.startsWith(pt + '_')) continue;
      failedResources.add(block);
    }
    then(failedResources).isEmpty();
  }

  public void testResourceWithSimilarNameInDifferentProviders() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);

    assertContainsElements(
      model.getResources().stream().filter(it -> it.getType().equals("google_sql_database")).map(Object::toString).toList(),
      "ResourceType(type='google_sql_database', provider=google)", "ResourceType(type='google_sql_database', provider=google-beta)");
    assertContainsElements(
      model.getDataSources().stream().filter(it -> it.getType().equals("google_compute_instance")).map(Object::toString).toList(),
      "DataSourceType(type='google_compute_instance', provider=google)", "DataSourceType(type='google_compute_instance', provider=google-beta)");
  }

  public void testDataSourcesHasProviderNameAsPrefix() {
    final TypeModel model = TypeModelProvider.Companion.getModel(getProject());
    assertNotNull(model);
    final List<DataSourceType> failedDataSources = new ArrayList<>();
    for (DataSourceType block : model.getDataSources()) {
      final String rt = block.getType();
      String pt = block.getProvider().getType();
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
      if (pt.equals("aws-4-49-0")) {
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
      if (rt.equals(pt)) continue;
      if (rt.startsWith(pt + '_')) continue;
      failedDataSources.add(block);
    }
    then(failedDataSources).isEmpty();
  }

  private PropertyOrBlockType findProperty(Map<String, PropertyOrBlockType> properties, String name) {
    return properties.get(name);
  }
}
