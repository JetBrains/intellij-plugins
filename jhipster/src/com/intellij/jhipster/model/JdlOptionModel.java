// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.jhipster.model.JdlPrimitiveType.BOOLEAN_TYPE;
import static com.intellij.jhipster.model.JdlPrimitiveType.INTEGER_TYPE;
import static com.intellij.jhipster.model.JdlPrimitiveType.STRING_ARRAY_TYPE;
import static com.intellij.jhipster.model.JdlPrimitiveType.STRING_TYPE;

public final class JdlOptionModel {
  public static final JdlOptionModel INSTANCE = new JdlOptionModel();

  private final Map<String, JdlOptionMapping> applicationConfigOptions;
  private final Map<String, JdlOptionMapping> deploymentOptions;

  private JdlOptionModel() {
    this.applicationConfigOptions = getSortedOptions(List.of(
      new JdlEnumMapping("applicationType", JdlApplicationType.class, JdlApplicationType.MONOLITH),
      new JdlEnumMapping("authenticationType", JdlAuthenticationType.class, JdlAuthenticationType.JWT),
      new JdlOptionMapping("baseName", STRING_TYPE, "jhipster"),
      new JdlOptionMapping("blueprint", STRING_TYPE),
      new JdlOptionMapping("blueprints", STRING_ARRAY_TYPE),
      new JdlEnumMapping("buildTool", JdlBuildTool.class, JdlBuildTool.MAVEN),
      new JdlEnumMapping("cacheProvider", JdlCacheProvider.class),
      new JdlOptionMapping("enableHibernateCache", BOOLEAN_TYPE, "true"),
      new JdlEnumMapping("clientFramework", JdlClientFramework.class, JdlClientFramework.ANGULARX),
      new JdlEnumMapping("clientPackageManager", JdlClientPackageManager.class, JdlClientPackageManager.NPM),
      new JdlOptionMapping("clientTheme", STRING_TYPE),
      new JdlOptionMapping("clientThemeVariant", STRING_TYPE),
      new JdlOptionMapping("creationTimestamp", INTEGER_TYPE),
      new JdlEnumMapping("databaseType", JdlDatabaseType.class, JdlDatabaseType.SQL),
      new JdlEnumMapping("devDatabaseType", JdlDevDatabaseType.class, JdlDevDatabaseType.H2DISK),
      new JdlOptionMapping("dtoSuffix", STRING_TYPE, "DTO"),
      new JdlOptionMapping("enableHibernateCache", BOOLEAN_TYPE, "true"),
      new JdlOptionMapping("enableSwaggerCodegen", BOOLEAN_TYPE, "false"),
      new JdlOptionMapping("enableTranslation", BOOLEAN_TYPE, "true"),
      new JdlOptionMapping("entitySuffix", STRING_TYPE),
      new JdlOptionMapping("jhiPrefix", STRING_TYPE, "jhi"),
      new JdlOptionMapping("jwtSecretKey", STRING_TYPE),
      new JdlEnumListMapping("languages", JdlLanguage.class),
      new JdlEnumMapping("messageBroker", JdlMessageBroker.class, JdlMessageBroker.FALSE),
      new JdlEnumMapping("nativeLanguage", JdlLanguage.class, JdlLanguage.ENGLISH),
      new JdlOptionMapping("packageName", STRING_TYPE),
      new JdlEnumMapping("prodDatabaseType", JdlProdDatabaseType.class, JdlProdDatabaseType.MSSQL),
      new JdlOptionMapping("reactive", BOOLEAN_TYPE),
      new JdlEnumMapping("searchEngine", JdlSearchEngine.class, JdlSearchEngine.FALSE),
      new JdlOptionMapping("serverPort", INTEGER_TYPE, "8080, 8081 or 9999"),
      new JdlEnumMapping("serviceDiscoveryType", JdlServiceDiscoveryType.class, JdlServiceDiscoveryType.NO),
      new JdlOptionMapping("skipClient", BOOLEAN_TYPE, "false"),
      new JdlOptionMapping("skipServer", BOOLEAN_TYPE, "false"),
      new JdlOptionMapping("skipUserManagement", BOOLEAN_TYPE, "false"),
      new JdlEnumListMapping("testFrameworks", JdlTestFramework.class),
      new JdlEnumMapping("websocket", JdlWebsocket.class),
      new JdlOptionMapping("microfrontends", STRING_ARRAY_TYPE, "[]")
    ));

    this.deploymentOptions = getSortedOptions(List.of(
      new JdlEnumMapping("deploymentType", JdlDeploymentType.class, JdlDeploymentType.DOCKER_COMPOSE),
      new JdlOptionMapping("directoryPath", STRING_TYPE, "../"),
      new JdlOptionMapping("appsFolders", STRING_ARRAY_TYPE, "[]"),
      new JdlOptionMapping("clusteredDbApps", STRING_ARRAY_TYPE, "[]"),
      new JdlOptionMapping("gatewayType", STRING_TYPE, "SpringCloudGateway"),
      new JdlEnumMapping("monitoring", JdlMonitoringType.class, JdlMonitoringType.NO),
      new JdlEnumMapping("serviceDiscoveryType", JdlServiceDiscoveryType.class, JdlServiceDiscoveryType.CONSUL),
      new JdlOptionMapping("dockerRepositoryName", STRING_TYPE),
      new JdlOptionMapping("dockerPushCommand", STRING_TYPE, "docker push"),
      new JdlOptionMapping("kubernetesNamespace", STRING_TYPE, "default"),
      new JdlOptionMapping("kubernetesUseDynamicStorage", BOOLEAN_TYPE, "false"),
      new JdlOptionMapping("kubernetesStorageClassName", STRING_TYPE),
      new JdlEnumMapping("kubernetesServiceType", JdlKubernetesServiceType.class, JdlKubernetesServiceType.LOAD_BALANCER),
      new JdlOptionMapping("ingressDomain", STRING_TYPE),
      new JdlEnumMapping("ingressType", JdlIngressType.class, JdlIngressType.NGINX),
      new JdlOptionMapping("istio", BOOLEAN_TYPE, "false"),
      new JdlOptionMapping("openshiftNamespace", STRING_TYPE, "default"),
      new JdlEnumMapping("storageType", JdlStorageType.class, JdlStorageType.EPHEMERAL),
      new JdlOptionMapping("registryReplicas", INTEGER_TYPE, "2")
    ));
  }

  public Map<String, JdlOptionMapping> getApplicationConfigOptions() {
    return applicationConfigOptions;
  }

  public Map<String, JdlOptionMapping> getDeploymentOptions() {
    return deploymentOptions;
  }

  public static Map<String, JdlOptionMapping> getSortedOptions(List<JdlOptionMapping> options) {
    List<JdlOptionMapping> mutableOptions = new ArrayList<>(options);

    mutableOptions.sort(Comparator.comparing(JdlOptionMapping::getName));

    Map<String, JdlOptionMapping> orderedOptions = new LinkedHashMap<>();
    for (JdlOptionMapping option : mutableOptions) {
      orderedOptions.put(option.getName(), option);
    }

    return orderedOptions;
  }
}
