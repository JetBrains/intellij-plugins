package com.intellij.flex.maven;

import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.maven.testFramework.MavenImportingTestCase;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;

import java.io.File;

public class NonJarDependenciesImportingTest extends MavenImportingTestCase {

  @Override
  protected void tearDown() throws Exception {
    try {
      for (Sdk sdk : ProjectJdkTable.getInstance().getSdksOfType(FlexmojosSdkType.getInstance())) {
        WriteAction.run(() -> ProjectJdkTable.getInstance().removeJdk(sdk));
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testUpdateRootEntriesWithActualPathForNonJarDependencies() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<packaging>swf</packaging>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>com.adobe.flex.framework</groupId>" +
                  "    <artifactId>framework</artifactId>" +
                  "    <version>3.2.0.3958</version>" +
                  "    <type>swc</type>" +
                  "  </dependency>" +
                  "</dependencies>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>org.sonatype.flexmojos</groupId>" +
                  "      <artifactId>flexmojos-maven-plugin</artifactId>" +
                  "      <version>4.0-beta-3</version>" +
                  "      <extensions>true</extensions>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +

                  "<repositories>" +
                  "  <repository>" +
                  "    <id>flex-mojos-repository</id>" +
                  "    <url>http://svn.sonatype.org/flexmojos/repository/</url>" +
                  "    <releases>" +
                  "      <enabled>true</enabled>" +
                  "    </releases>" +
                  "  </repository>" +
                  "</repositories>");

    assertModuleLibDeps("project", "Maven: com.adobe.flex.framework:framework:swc:3.2.0.3958");
    assertModuleLibDep("project", "Maven: com.adobe.flex.framework:framework:swc:3.2.0.3958",
                       "jar://" + getRepositoryPath() + "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958.swc!/",
                       "jar://" + getRepositoryPath() + "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-sources.jar!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-asdoc.zip!/");

    setRepositoryPath(new File(myDir, "__repo").getPath());
    myProjectsManager.getEmbeddersManager().reset();

    resolveAndImportAllMavenProjects();
    resolveDependenciesAndImport();

    assertModuleLibDeps("project", "Maven: com.adobe.flex.framework:framework:swc:3.2.0.3958");
    assertModuleLibDep("project", "Maven: com.adobe.flex.framework:framework:swc:3.2.0.3958",
                       "jar://" + getRepositoryPath() + "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958.swc!/",
                       "jar://" + getRepositoryPath() + "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-sources.jar!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-asdoc.zip!/");
  }

  public void testRemovingUnusedNonJARLibrary() {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<packaging>war</packaging>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>group</groupId>" +
                  "    <artifactId>lib1</artifactId>" +
                  "    <version>1</version>" +
                  "    <type>ear</type>" +
                  "  </dependency>" +
                  "  <dependency>" +
                  "    <groupId>group</groupId>" +
                  "    <artifactId>lib2</artifactId>" +
                  "    <version>1</version>" +
                  "    <type>war</type>" +
                  "  </dependency>" +
                  "</dependencies>");

    assertProjectLibraries("Maven: group:lib1:ear:1",
                           "Maven: group:lib2:war:1");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<packaging>war</packaging>");

    assertProjectLibraries();
  }
}
