// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JdlCompletionTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/jhipster/testData/completion";
  }

  public void testTopLevelKeywords() {
    myFixture.testCompletionVariants("TopLevel.jdl",
                                     "application",
                                     "deployment",
                                     "dto",
                                     "entities",
                                     "entity",
                                     "enum",
                                     "except",
                                     "microservice",
                                     "paginate",
                                     "relationship",
                                     "search",
                                     "service",
                                     "use",
                                     "with"
    );
  }

  public void testApplicationConfig() {
    myFixture.testCompletionVariants("ApplicationConfig.jdl",
                                     "applicationType", "authenticationType", "baseName", "blueprint", "blueprints", "buildTool",
                                     "cacheProvider", "clientFramework", "clientPackageManager", "clientTheme", "clientThemeVariant",
                                     "creationTimestamp", "databaseType", "devDatabaseType", "dtoSuffix", "enableHibernateCache",
                                     "enableSwaggerCodegen", "enableTranslation", "entitySuffix", "jhiPrefix", "jwtSecretKey",
                                     "languages", "messageBroker", "microfrontends", "nativeLanguage", "packageName",
                                     "prodDatabaseType", "reactive", "searchEngine", "serverPort", "serviceDiscoveryType",
                                     "skipClient", "skipServer", "skipUserManagement", "testFrameworks", "websocket");
  }

  public void testApplicationOptions() {
    myFixture.testCompletionVariants("ApplicationOptions.jdl",
                                     "config", "dto", "entities", "except", "paginate", "with");
  }

  public void testDeploymentOptions() {
    myFixture.testCompletionVariants("DeploymentOptions.jdl",
                                     "appsFolders", "clusteredDbApps", "deploymentType", "directoryPath", "dockerPushCommand",
                                     "dockerRepositoryName", "gatewayType", "ingressDomain", "ingressType", "istio",
                                     "kubernetesNamespace", "kubernetesServiceType", "kubernetesStorageClassName",
                                     "kubernetesUseDynamicStorage", "monitoring", "openshiftNamespace", "registryReplicas",
                                     "serviceDiscoveryType", "storageType");
  }

  public void testFieldTypes() {
    myFixture.testCompletionVariants("FieldTypes.jdl",
                                     "SpaceEventType", "AnyBlob", "BigDecimal", "Blob", "Boolean", "Date", "Double", "Duration",
                                     "Float", "ImageBlob", "Instant", "Integer", "LocalDate", "Long", "String", "TextBlob", "UUID",
                                     "ZonedDateTime"
    );
  }

  public void testRelationshipTypes() {
    myFixture.testCompletionVariants("RelationshipTypes.jdl",
                                     "ManyToMany", "ManyToOne", "OneToMany", "OneToOne");
  }

  public void testBuildToolValues() {
    myFixture.testCompletionVariants("BuildToolOptions.jdl",
                                     "gradle", "maven");
  }

  public void testDatabaseValues() {
    myFixture.testCompletionVariants("DatabaseOptions.jdl",
                                     "mariadb", "mongodb", "mssql", "mysql", "neo4j", "no", "oracle", "postgresql");
  }

  public void testServiceDiscoveryTypes() {
    myFixture.testCompletionVariants("ServiceDiscoveryOptions.jdl",
                                     "consul", "eureka", "no");
  }

  public void testFieldConstraints() {
    myFixture.testCompletionVariants("FieldConstraints.jdl",
                                     "max()", "maxbytes()", "maxlength()", "min()", "minbytes()",
                                     "minlength()", "pattern()", "required", "unique"
    );
  }

  public void testRelationshipOptions() {
    myFixture.testCompletionVariants("RelationshipOptions.jdl",
                                     "Id", "OnDelete", "OnUpdate"
    );
  }

  public void testRelationshipOptionValues() {
    myFixture.testCompletionVariants("RelationshipOptionValues.jdl",
                                     "CASCADE", "NO ACTION", "RESTRICT", "SET DEFAULT", "SET NULL"
    );
  }
}
