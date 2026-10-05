// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/jhipster/backend/testData/completion")
public class JdlCompletionTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("unused")
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  @Test
  public void testTopLevelKeywords() {
    testCompletionVariants("TopLevel.jdl",
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

  @Test
  public void testApplicationConfig() {
    testCompletionVariants("ApplicationConfig.jdl",
                           "applicationType", "authenticationType", "baseName", "blueprint", "blueprints", "buildTool",
                           "cacheProvider", "clientFramework", "clientPackageManager", "clientTheme", "clientThemeVariant",
                           "creationTimestamp", "databaseType", "devDatabaseType", "dtoSuffix", "enableHibernateCache",
                           "enableSwaggerCodegen", "enableTranslation", "entitySuffix", "jhiPrefix", "jwtSecretKey",
                           "languages", "messageBroker", "microfrontends", "nativeLanguage", "packageName",
                           "prodDatabaseType", "reactive", "searchEngine", "serverPort", "serviceDiscoveryType",
                           "skipClient", "skipServer", "skipUserManagement", "testFrameworks", "websocket");
  }

  @Test
  public void testApplicationOptions() {
    testCompletionVariants("ApplicationOptions.jdl",
                           "config", "dto", "entities", "except", "paginate", "with");
  }

  @Test
  public void testDeploymentOptions() {
    testCompletionVariants("DeploymentOptions.jdl",
                           "appsFolders", "clusteredDbApps", "deploymentType", "directoryPath", "dockerPushCommand",
                           "dockerRepositoryName", "gatewayType", "ingressDomain", "ingressType", "istio",
                           "kubernetesNamespace", "kubernetesServiceType", "kubernetesStorageClassName",
                           "kubernetesUseDynamicStorage", "monitoring", "openshiftNamespace", "registryReplicas",
                           "serviceDiscoveryType", "storageType");
  }

  @Test
  public void testFieldTypes() {
    testCompletionVariants("FieldTypes.jdl",
                           "SpaceEventType", "AnyBlob", "BigDecimal", "Blob", "Boolean", "Date", "Double", "Duration",
                           "Float", "ImageBlob", "Instant", "Integer", "LocalDate", "Long", "String", "TextBlob", "UUID",
                           "ZonedDateTime"
    );
  }

  @Test
  public void testRelationshipTypes() {
    testCompletionVariants("RelationshipTypes.jdl",
                           "ManyToMany", "ManyToOne", "OneToMany", "OneToOne");
  }

  @Test
  public void testBuildToolValues() {
    testCompletionVariants("BuildToolOptions.jdl",
                           "gradle", "maven");
  }

  @Test
  public void testDatabaseValues() {
    testCompletionVariants("DatabaseOptions.jdl",
                           "mariadb", "mongodb", "mssql", "mysql", "neo4j", "no", "oracle", "postgresql");
  }

  @Test
  public void testServiceDiscoveryTypes() {
    testCompletionVariants("ServiceDiscoveryOptions.jdl",
                           "consul", "eureka", "no");
  }

  @Test
  public void testFieldConstraints() {
    testCompletionVariants("FieldConstraints.jdl",
                           "max()", "maxbytes()", "maxlength()", "min()", "minbytes()",
                           "minlength()", "pattern()", "required", "unique"
    );
  }

  @Test
  public void testRelationshipOptions() {
    testCompletionVariants("RelationshipOptions.jdl",
                           "Id", "OnDelete", "OnUpdate"
    );
  }

  @Test
  public void testRelationshipOptionValues() {
    testCompletionVariants("RelationshipOptionValues.jdl",
                           "CASCADE", "NO ACTION", "RESTRICT", "SET DEFAULT", "SET NULL"
    );
  }

  private void testCompletionVariants(String file, String... items) {
    EdtTestUtil.runInEdtAndWait(() -> codeInsightFixture.get().testCompletionVariants(file, items));
  }
}