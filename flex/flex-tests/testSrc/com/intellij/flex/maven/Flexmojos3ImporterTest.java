// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.maven;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkAdditionalData;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.PathUtil;
import org.jetbrains.idea.maven.model.*;
import org.jetbrains.idea.maven.project.MavenEmbeddersManager;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectResolver;
import org.jetbrains.idea.maven.project.MavenWorkspaceSettingsComponent;
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.server.MavenServerExecutionResult;
import org.jetbrains.idea.maven.server.MavenServerManager;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.flex.model.bc.OutputType.*;
import static com.intellij.flex.model.bc.TargetPlatform.Desktop;
import static com.intellij.flex.model.bc.TargetPlatform.Web;

public class Flexmojos3ImporterTest extends FlexmojosImporterTestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MavenWorkspaceSettingsComponent.getInstance(myProject).getSettings().generalSettings.setMavenHome(MavenServerManager.BUNDLED_MAVEN_2);
  }

  @Override
  protected String getFlexmojosVersion() {
    return "3.5.0";
  }

  @Override
  protected String getConfigFilesBasePath(final Module module) {
    return PathUtil.getParentPath(module.getModuleFilePath());
  }

  public void testAppWithModulesAndLib() {
    final String pomContent = mavenProjectDescription("project", "swf") +
                              "<properties>" +
                              "  <my.suffix>zz</my.suffix>" +
                              "</properties>" +
                              "<build>" +
                              "  <plugins>" +
                              "    <plugin>" +
                              flexmojosPlugin() +
                              "      <configuration>" +
                              "        <sourceFile>\n   pack\\pack2/Main.as\n   </sourceFile>" +
                              "        <classifier>\n   ${my.suffix}\n   </classifier>" +
                              "        <output>\n   output.swf\n   </output>" +
                              "        <moduleFiles>" +
                              "          <a>\n  Module1.mxml\n  </a>" +
                              "          <moduleFile>ignore.txt</moduleFile>" +
                              "          <moduleFile>\n  pack\\Module2.as\n  </moduleFile>" +
                              "        </moduleFiles>" +
                              "      </configuration>" +
                              "     </plugin>" +
                              "     <plugin>" +
                              "       <groupId>org.codehaus.gmaven</groupId>" +
                              "       <artifactId>gmaven-plugin</artifactId>" +
                              "       <version>1.3</version>" +
                              "     </plugin>" +
                              "  </plugins>" +
                              "</build>" +
                              flexFrameworkDependency("3.2.0.3958");

    importProject(pomContent);

    checkBCCount("project", 3);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Application), "pack.pack2.Main", "output.swf", "", "3.2.0.3958",
            "en_US", "target/project-1.0-zz-config-report.xml");
    checkBC("project", "Module1", new BuildConfigurationNature(Web, false, RuntimeLoadedModule), "Module1", "project-1.0-Module1.swf",
            "target", "3.2.0.3958", "en_US", "target/project-1.0-Module1-config-report.xml");
    checkBC("project", "Module2", new BuildConfigurationNature(Web, false, RuntimeLoadedModule), "pack.Module2", "project-1.0-Module2.swf",
            "target", "3.2.0.3958", "en_US", "target/project-1.0-Module2-config-report.xml");

    final Module module = ModuleManager.getInstance(myProject).findModuleByName("project");
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      for (ModifiableFlexBuildConfiguration bc : editor.getConfigurations(module)) {
        bc.getCompilerOptions().setAdditionalOptions("custom options");
      }
    });

    importProject(pomContent.replace("swf", "swc"));
    checkBCCount("project", 3);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "output.swc", "", "3.2.0.3958",
            "", "target/project-1.0-zz-config-report.xml");
    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName("project");
    assertEquals("custom options", bc.getCompilerOptions().getAdditionalOptions());
  }

  public void testMainCassAndFinalName() throws Exception {
    final VirtualFile file = createProjectSubFile("src/main/flex/SomeClass.mxml");
    WriteAction.runAndWait(() -> VfsUtil.saveText(file, "<mx:Application xmlns:mx=\"http://www.adobe.com/2006/mxml\"/>"));

    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <finalName>foo</finalName>" +
                  "  <sourceDirectory>src/main/flex</sourceDirectory>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <dependencies>\n" +
                  "        <dependency>\n" +
                  "          <groupId>com.adobe.flex</groupId>\n" +
                  "          <artifactId>compiler</artifactId>\n" +
                  "          <version>3.5.0.12683</version>\n" +
                  "          <type>pom</type>\n" +
                  "        </dependency>\n" +
                  "      </dependencies>\n" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.5.0.12683"));
    performPostImportTasks();

    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Application), "SomeClass", "foo.swf", "target", "3.5.0.12683",
            "en_US", "target/foo-config-report.xml");
  }

  public void testConfiguringResourceBundleDependency() {
    importProject(mavenProjectDescription("project", "swf") +
                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>com.adobe.flex.framework</groupId>" +
                  "    <artifactId>framework</artifactId>" +
                  "    <version>3.2.0.3958</version>" +
                  "    <type>resource-bundle</type>" +
                  "    <classifier>en_US</classifier>" +
                  "  </dependency>" +
                  "</dependencies>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" + flexmojosPlugin() + "</plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModuleLibDep("project", "Maven: com.adobe.flex.framework:framework:resource-bundle:en_US:3.2.0.3958",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-en_US.rb.swc!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-sources.jar!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-asdoc.zip!/");
  }

  public void testConfiguringResourceBundleRbSwcDependency() {
    importProject(mavenProjectDescription("project", "swf") +
                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>com.adobe.flex.framework</groupId>" +
                  "    <artifactId>framework</artifactId>" +
                  "    <version>3.2.0.3958</version>" +
                  "    <type>rb.swc</type>" +
                  "    <classifier>en_US</classifier>" +
                  "  </dependency>" +
                  "</dependencies>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" + flexmojosPlugin() + "</plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModuleLibDep("project", "Maven: com.adobe.flex.framework:framework:rb.swc:en_US:3.2.0.3958",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-en_US.rb.swc!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-sources.jar!/",
                       "jar://" + getRepositoryPath() +
                       "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-asdoc.zip!/");
  }

  /*
  public void testConfiguringRuntimeLocalesForLibrary() throws Exception {
    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <runtimeLocales>" +
                  "          <locale>en_US</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    checkBC(findResourceFacet("project", "en_US"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "project-1.0-en_US.rb.swc",
            "target/locales/project-1.0-en_US-config-report.xml",
            FlexmojosSdkType.getInstance());

    checkBC(findResourceFacet("project", "ru_RU"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "project-1.0-ru_RU.rb.swc",
            "target/locales/project-1.0-ru_RU-config-report.xml",
            FlexmojosSdkType.getInstance());
  }

  public void testConfiguringRuntimeLocalesAndRuntimeLocaleOutputPathForLibrary() throws Exception {
    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <runtimeLocaleOutputPath>/{contextRoot}/locales/foo-{artifactId}-{version}-{locale}.{extension}</runtimeLocaleOutputPath>" +
                  "        <runtimeLocales>" +
                  "          <locale>en_US</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    checkBC(findResourceFacet("project", "en_US"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "foo-project-1.0-en_US.rb.swc",
            "target/locales/foo-project-1.0-en_US-config-report.xml",
            FlexmojosSdkType.getInstance());

    checkBC(findResourceFacet("project", "ru_RU"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "foo-project-1.0-ru_RU.rb.swc",
            "target/locales/foo-project-1.0-ru_RU-config-report.xml",
            FlexmojosSdkType.getInstance());
  }

  public void testConfiguringRuntimeLocalesForApplication() throws Exception {
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <runtimeLocales>" +
                  "          <locale>en_US</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    checkBC(findResourceFacet("project", "en_US"),
            FlexBuildConfiguration.APPLICATION,
            "target/locales",
            "project-1.0-en_US.swf",
            "target/locales/project-1.0-en_US-config-report.xml",
            FlexmojosSdkType.getInstance());

    checkBC(findResourceFacet("project", "ru_RU"),
            FlexBuildConfiguration.APPLICATION,
            "target/locales",
            "project-1.0-ru_RU.swf",
            "target/locales/project-1.0-ru_RU-config-report.xml",
            FlexmojosSdkType.getInstance());
  }

  public void testDeletingUnnecessaryLocalesAfterReimport() throws Exception {
    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <runtimeLocales>" +
                  "          <locale>en_US</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertNotNull(findResourceFacet("project", "en_US"));
    assertNotNull(findResourceFacet("project", "ru_RU"));

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>swc</packaging>" +

                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     flexmojosPlugin() +
                     "      <configuration>" +
                     "        <runtimeLocales>" +
                     "          <locale>ru_RU</locale>" +
                     "          <locale>fr_FR</locale>" +
                     "        </runtimeLocales>" +
                     "      </configuration>" +
                     "    </plugin>" +
                     "  </plugins>" +
                     "</build>");

    importProject();

    assertNull(findResourceFacet("project", "en_US"));
    assertNotNull(findResourceFacet("project", "ru_RU"));
    assertNotNull(findResourceFacet("project", "fr_FR"));
  }
  */
  public void test2Projects() {
    createProjectPom(mavenProjectDescription("project", "pom") +
                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1",
                    mavenProjectDescription("m1", "swf") +
                    "<build>" +
                    "  <plugins>" +
                    "    <plugin>" +
                    flexmojosPlugin() +
                    "      <configuration>" +
                    "        <runtimeLocales>" +
                    "          <locale>en_US</locale>" +
                    "          <locale>ru_RU</locale>" +
                    "        </runtimeLocales>" +
                    "      </configuration>" +
                    "    </plugin>" +
                    "  </plugins>" +
                    "</build>" +
                    flexFrameworkDependency("3.2.0.3958"));

    createModulePom("m2",
                    mavenProjectDescription("m2", "swc") +
                    "<build>" +
                    "  <plugins>" +
                    "    <plugin>" +
                    flexmojosPlugin() +
                    "      <configuration>" +
                    "        <runtimeLocales>" +
                    "          <locale>en_US</locale>" +
                    "          <locale>ru_RU</locale>" +
                    "        </runtimeLocales>" +
                    "      </configuration>" +
                    "    </plugin>" +
                    "  </plugins>" +
                    "</build>" +
                    flexFrameworkDependency("3.2.0.3958"));

    importProject();

    checkBCCount("m1", 1);
    checkBC("m1", "m1", new BuildConfigurationNature(Web, false, Application), "", "m1-1.0.swf", "target", "3.2.0.3958",
            "en_US", "target/m1-1.0-config-report.xml");

    /*checkBC(findResourceFacet("m1", "en_US"),
            FlexBuildConfiguration.APPLICATION,
            "target/locales",
            "m1-1.0-en_US.swf",
            "target/locales/m1-1.0-en_US-config-report.xml",
            FlexmojosSdkType.getInstance());

    checkBC(findResourceFacet("m1", "ru_RU"),
            FlexBuildConfiguration.APPLICATION,
            "target/locales",
            "m1-1.0-ru_RU.swf",
            "target/locales/m1-1.0-ru_RU-config-report.xml",
            FlexmojosSdkType.getInstance());*/

    checkBCCount("m2", 1);
    checkBC("m2", "m2", new BuildConfigurationNature(Web, false, Library), "", "m2-1.0.swc", "target", "3.2.0.3958",
            "", "target/m2-1.0-config-report.xml");
    /*checkBC(findResourceFacet("m2", "en_US"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "m2-1.0-en_US.rb.swc",
            "target/locales/m2-1.0-en_US-config-report.xml",
            FlexmojosSdkType.getInstance());

    checkBC(findResourceFacet("m2", "ru_RU"),
            FlexBuildConfiguration.LIBRARY,
            "target/locales",
            "m2-1.0-ru_RU.rb.swc",
            "target/locales/m2-1.0-ru_RU-config-report.xml",
            FlexmojosSdkType.getInstance());*/
  }

  public void testConfiguringCompiledLocalesAsSourceFolders() {
    createProjectSubDirs("src/main/locales/en_US",
                         "src/main/locales/ru_RU",
                         "src/main/locales/fr_FR");

    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <compiledLocales>" +
                  "          <locale>en_US</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </compiledLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));
    assertSources("project", "src/main/locales/en_US", "src/main/locales/ru_RU");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "project-1.0.swc", "target", "3.2.0.3958",
            "en_US\nru_RU", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringRuntimeLocalesAsSourceFolders() {
    createProjectSubDirs("src/main/locales/pt_BR",
                         "src/main/locales/ru_RU");
    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <runtimeLocales>" +
                  "          <locale>pt_BR</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));
    assertSources("project", "src/main/locales/pt_BR", "src/main/locales/ru_RU");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "project-1.0.swc", "target", "3.2.0.3958",
            "", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringRuntimeAndCompiledLocalesAsSourceFolders() {
    createProjectSubDirs("src/main/locales/pt_BR",
                         "src/main/locales/ru_RU",
                         "src/main/locales/en_GB",
                         "src/main/locales/fr_FR");
    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <compiledLocales>" +
                  "          <locale>en_GB</locale>" +
                  "          <locale>fr_FR</locale>" +
                  "        </compiledLocales>" +
                  "        <runtimeLocales>" +
                  "          <locale>pt_BR</locale>" +
                  "          <locale>ru_RU</locale>" +
                  "        </runtimeLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));

    assertSources("project", "src/main/locales/en_GB", "src/main/locales/fr_FR", "src/main/locales/pt_BR", "src/main/locales/ru_RU");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "project-1.0.swc", "target", "3.2.0.3958",
            "en_GB\nfr_FR", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringCompiledLocalesSpecifiedByOldStyleLocalesOption() {
    createProjectSubDirs("src/main/locales/en_US",
                         "src/main/locales/ru_RU");

    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <locales>" +
                  "          <locale>en_US</locale>" +
                  "          <param>ru_RU</param>" +
                  "        </locales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));
    assertSources("project", "src/main/locales/en_US", "src/main/locales/ru_RU");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "project-1.0.swc", "target", "3.2.0.3958",
            "en_US\nru_RU", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringCompiledLocalesSpecifiedBySpecifiedDefaultLocale() {
    createProjectSubDirs("src/main/locales/ru_RU");
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <defaultLocale>ru_RU</defaultLocale>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));
    assertSources("project", "src/main/locales/ru_RU");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Application), "", "project-1.0.swf", "target", "3.2.0.3958",
            "ru_RU", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringCompiledLocalesSpecifiedByDefaultLocale() {
    createProjectSubDirs("src/main/locales/en_US");
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" + flexmojosPlugin() + "</plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));
    assertSources("project", "src/main/locales/en_US");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Application), "", "project-1.0.swf", "target", "3.2.0.3958",
            "en_US", "target/project-1.0-config-report.xml");
  }

  public void testConfiguringCompiledLocalesFromCustomDirAsSourceFolders() {
    createProjectSubDirs("locales/en_US");

    importProject(mavenProjectDescription("project", "swc") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  "        <resourceBundlePath>${basedir}/locales/{locale}</resourceBundlePath>" +
                  "        <compiledLocales>" +
                  "          <locale>en_US</locale>" +
                  "        </compiledLocales>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));

    assertSources("project", "locales/en_US");
    checkBCCount("project", 1);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Library), "", "project-1.0.swc", "target", "3.2.0.3958",
            "en_US", "target/project-1.0-config-report.xml");
  }

  public void testThatRbSwcPlaceholdersFixedForDefaultLocale() {
    commonTestForRbSwcPlaceholders();
  }

  public void testThatRbSwcPlaceholdersFixedForAllLocales() {
    commonTestForRbSwcPlaceholders("ja_JP", "it_IT");
  }

  private void commonTestForRbSwcPlaceholders(String... compiledLocales) {
    final String LIB_PATH_TEMPLATE =
      "jar://" + getRepositoryPath() + "/com/adobe/flex/framework/framework/3.2.0.3958/framework-3.2.0.3958-{0}.rb.swc!/";
    final List<String> expectedLibPaths = new ArrayList<>();
    final String localesConfiguration;
    if (compiledLocales.length == 0) {
      localesConfiguration = "";
      expectedLibPaths.add(MessageFormat.format(LIB_PATH_TEMPLATE, "en_US"));
    }
    else {
      final StringBuilder builder = new StringBuilder("<compiledLocales>");
      for (final String locale : compiledLocales) {
        builder.append("<locale>").append(locale).append("</locale>");
        expectedLibPaths.add(MessageFormat.format(LIB_PATH_TEMPLATE, locale));
      }
      builder.append("</compiledLocales>");
      localesConfiguration = builder.toString();
    }

    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>" +
                  localesConfiguration +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>com.adobe.flex.framework</groupId>" +
                  "    <artifactId>flex-framework</artifactId>" +
                  "    <version>3.2.0.3958</version>" +
                  "    <type>pom</type>" +
                  "  </dependency>" +
                  "</dependencies>");

    assertModuleLibDep("project", "Maven: com.adobe.flex.framework:framework:rb.swc:3.2.0.3958", expectedLibPaths, null, null);
  }

  public void testAdditionalJarsAddedToFlexmojosSdkClasspath() {
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <dependencies>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex</groupId>\n" +
                  "              <artifactId>compiler</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "              <type>pom</type>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>afe</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>aglj32</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>flex-fontkit</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>license</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>rideau</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>incorrect</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "          </dependency>\n" +
                  "      </dependencies>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.2.0.3958"));

    final String[] jarNames =
      {"asc", "asdoc", "batik-all-flex", "commons-collections", "commons-discovery", "commons-logging", "compc", "copylocale", "digest",
        "fcsh", "fdb", "flex-compiler-oem", "flex-messaging-common", "mm-velocity-1.4", "mxmlc", "optimizer", "swfutils", "xalan",
        "xercesImpl", "xercesPatch", "xmlParserAPIs", "afe", "aglj32", "flex-fontkit", "license", "rideau"};

    final String[] expected = new String[jarNames.length];
    for (int i = 0; i < jarNames.length; i++) {
      expected[i] = getRepositoryPath() + "/com/adobe/flex/compiler/" + jarNames[i] + "/3.5.0.12683/" + jarNames[i] + "-3.5.0.12683.jar";
    }

    checkFlexmojosSdkClasspath("3.5.0.12683", expected);

    // Remport with different version of aglj library. It must be replaced in Flexmojos SDK as well.
    importProject("<groupId>test</groupId>" + "<artifactId>project</artifactId>" + "<version>1</version>" + "<packaging>swf</packaging>" +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <dependencies>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex</groupId>\n" +
                  "              <artifactId>compiler</artifactId>\n" +
                  "              <version>3.5.0.12683</version>\n" +
                  "              <type>pom</type>\n" +
                  "          </dependency>\n" +
                  "          <dependency>\n" +
                  "              <groupId>com.adobe.flex.compiler</groupId>\n" +
                  "              <artifactId>aglj40</artifactId>\n" +
                  "              <version>666</version>\n" +
                  "          </dependency>\n" +
                  "      </dependencies>" +
                  "</plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.5.0.12683"));

    expected[22] = expected[22].replace("aglj32/3.5.0.12683/aglj32-3.5.0.12683", "aglj40/666/aglj40-666");
    checkFlexmojosSdkClasspath("3.5.0.12683", expected);
  }

  private static void checkFlexmojosSdkClasspath(final String sdkVersion, final String[] expectedPaths) {
    final Sdk sdk = ProjectJdkTable.getInstance().findJdk("Flexmojos SDK " + sdkVersion);
    assertNotNull(sdk);
    assertInstanceOf(sdk.getSdkType(), FlexmojosSdkType.class);
    final SdkAdditionalData additionalData = sdk.getSdkAdditionalData();
    assertInstanceOf(additionalData, FlexmojosSdkAdditionalData.class);
    final Collection<String> classpath = ((FlexmojosSdkAdditionalData)additionalData).getFlexCompilerClasspath();
    assertSameElements(classpath, expectedPaths);
  }

  public void testTransitiveDepsOnPartialProjectImport() throws MavenProcessCanceledException {
    createProjectPom(mavenProjectDescription("aggregator", "pom") +
                     "  <modules>\n" +
                     "    <module>app</module>\n" +
                     "    <module>libA</module>\n" +
                     "    <module>libB</module>\n" +
                     "  </modules>\n");

    final VirtualFile app = createModulePom("app", parent() +
                                                   "  <artifactId>ttApp</artifactId>\n" +
                                                   "  <packaging>jar</packaging>\n" +
                                                   "\n" + dependencies("A", "jar"));

    createModulePom("libA", parent() +
                            "  <artifactId>A</artifactId>\n" +
                            "  <packaging>jar</packaging>\n" +
                            "  \n" +
                            dependencies("B", "jar"));

    createModulePom("libB", parent() +
                            "  <artifactId>B</artifactId>\n" +
                            "  <packaging>jar</packaging>");

    importProject();

    MavenProjectResolver.EmbedderTask task = new MavenProjectResolver.EmbedderTask() {
      @Override
      public void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
        MavenWorkspaceMap workspaceMap = new MavenWorkspaceMap();
        for (MavenProject mavenProject : myProjectsTree.getProjects()) {
          if (MavenConstants.TYPE_JAR.equalsIgnoreCase(mavenProject.getPackaging())) {
            workspaceMap.register(mavenProject.getMavenId(), new File(mavenProject.getFile().getPath()),
                                  new File(mavenProject.getMavenId().getArtifactId() + ".jar"));
          }
          else {
            workspaceMap.register(mavenProject.getMavenId(), new File(mavenProject.getFile().getPath()));
          }
        }

        embedder.customizeForStrictResolve(workspaceMap, NULL_MAVEN_CONSOLE, getMavenProgressIndicator());
        MavenServerExecutionResult result =
          embedder.execute(app, Collections.emptyList(), Collections.emptyList(), Collections.singletonList("compile"));
        assertEmpty(result.problems);
        assertNotNull(result);
        // test find all transitive deps (MavenWorkspaceMap filler is responsible for)
        MavenModel mavenModel = result.projectData.mavenModel;
        assertTransitiveDeps(TEST_GROUP_ID, TEST_VERSION, mavenModel.getDependencies());

        // test correct artifact file (must be SWC/JAR (project product), but not POM) (CustomArtifactResolver is responsible for)
        MavenArtifact resolve =
          embedder.resolve(new MavenArtifactInfo(TEST_GROUP_ID, "B", TEST_VERSION, MavenConstants.TYPE_JAR, null),
                           mavenModel.getRemoteRepositories());
        assertNotNull(resolve);
        assertEquals("B.jar", resolve.getFile().getPath());
      }
    };

    MavenProject appProject = myProjectsTree.findProject(new MavenId(TEST_GROUP_ID, "ttApp", TEST_VERSION));
    assertNotNull(appProject);
    myProjectResolver.executeWithEmbedder(appProject, myProjectsManager.getEmbeddersManager(), MavenEmbeddersManager.FOR_POST_PROCESSING,
                                          NULL_MAVEN_CONSOLE, getMavenProgressIndicator(), task);

    List<MavenArtifact> appSubProjectDeps = appProject.getDependencies();
    assertTransitiveDeps(TEST_GROUP_ID, TEST_VERSION, appSubProjectDeps);
  }

  private static void assertTransitiveDeps(String groupId, String version, List<MavenArtifact> appSubProjectDeps) {
    assertEquals(2, appSubProjectDeps.size());
    assertEquals(new MavenId(groupId, "A", version), appSubProjectDeps.get(0).getMavenId());
    assertEquals(new MavenId(groupId, "B", version), appSubProjectDeps.get(1).getMavenId());
  }

  public void testDependencyOnJavaIgnored() {
    createProjectPom(mavenProjectDescription("aggregator", "pom") +
                     "  <modules>\n" +
                     "    <module>app</module>\n" +
                     "    <module>lib1</module>\n" +
                     "    <module>lib2</module>\n" +
                     "  </modules>\n" +
                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     flexmojosPlugin() +
                     "     </plugin>" +
                     "  </plugins>" +
                     "</build>" +
                     flexFrameworkDependency("3.2.0.3958"));

    createModulePom("app", parent() +
                           mavenProjectDescription("app", "swf") +
                           "  <dependencies>\n" +
                           "    <dependency>\n" +
                           "      <groupId>" + TEST_GROUP_ID + "</groupId>" +
                           "      <artifactId>lib1</artifactId>" +
                           "      <version>1.0</version>" +
                           "    </dependency>\n" +
                           "    <dependency>\n" +
                           "      <groupId>" + TEST_GROUP_ID + "</groupId>" +
                           "      <artifactId>lib1</artifactId>" +
                           "      <version>1.0</version>" +
                           "      <classifier>test</classifier>" +
                           "      <scope>test</scope>" +
                           "    </dependency>\n" +
                           "    <dependency>\n" +
                           "      <groupId>" + TEST_GROUP_ID + "</groupId>" +
                           "      <artifactId>lib2</artifactId>" +
                           "      <version>1.0</version>" +
                           "    </dependency>\n" +
                           "    <dependency>\n" +
                           "      <groupId>com.adobe.flex.compiler</groupId>\n" +
                           "      <artifactId>flex-fontkit</artifactId>\n" +
                           "      <version>4.0.0.14159</version>\n" +
                           "      <type>jar</type>\n" +
                           "    </dependency>\n" +
                           "  </dependencies>");

    createModulePom("lib1", parent() +
                            "  <artifactId>lib1</artifactId>\n" +
                            "  <packaging>swc</packaging>");

    createModulePom("lib2", parent() +
                            "  <artifactId>lib2</artifactId>\n" +
                            "  <packaging>jar</packaging>");
    importProject();

    checkBC("app", "app", new BuildConfigurationNature(Web, false, Application), "", "app-1.0.swf", "target", "3.2.0.3958", "en_US",
            "target/app-1.0-config-report.xml");
    assertModules("aggregator", "app", "lib1", "lib2");
    assertModuleType("aggregator", StdModuleTypes.JAVA);
    assertModuleType("app", FlexModuleType.getInstance());
    assertModuleType("lib1", FlexModuleType.getInstance());
    assertModuleType("lib2", StdModuleTypes.JAVA);
    checkBCOnBCDependencies("app", "lib1:Merged");
  }

  public void testAppOnRlmDependency() {
    createProjectPom(mavenProjectDescription("aggregator", "pom") +
                     "  <modules>\n" +
                     "    <module>app</module>\n" +
                     "    <module>rlm</module>\n" +
                     "  </modules>\n" +
                     "<build>" +
                     "  <plugins>" +
                     "    <plugin>" +
                     flexmojosPlugin() +
                     "     </plugin>" +
                     "  </plugins>" +
                     "</build>" +
                     flexFrameworkDependency("3.2.0.3958"));

    createModulePom("app", parent() + mavenProjectDescription("app", "swf") +
                           "  <dependencies>\n" +
                           "    <dependency>\n" +
                           "      <groupId>" + TEST_GROUP_ID + "</groupId>" +
                           "      <artifactId>rlm</artifactId>" +
                           "      <version>1.0</version>" +
                           "      <scope>runtime</scope>" +
                           "    </dependency>\n" +
                           "  </dependencies>");

    createModulePom("rlm", parent() + mavenProjectDescription("rlm", "swf"));

    importProject();

    checkBC("app", "app", new BuildConfigurationNature(Web, false, Application), "", "app-1.0.swf", "target", "3.2.0.3958", "en_US",
            "target/app-1.0-config-report.xml");
    final FlexBuildConfiguration rlmBC =
      checkBC("rlm", "rlm", new BuildConfigurationNature(Web, false, Application), "", "rlm-1.0.swf", "target", "3.2.0.3958", "en_US",
              "target/rlm-1.0-config-report.xml");
    assertModules("aggregator", "app", "rlm");
    assertModuleType("aggregator", StdModuleTypes.JAVA);
    assertModuleType("app", FlexModuleType.getInstance());
    assertModuleType("rlm", FlexModuleType.getInstance());
    checkBCOnBCDependencies("app", "rlm:Loaded");

    // check that manually set RLM output type is preserved
    ((ModifiableFlexBuildConfiguration)rlmBC).setOutputType(RuntimeLoadedModule);

    // fake change
    createModulePom("rlm", parent() + mavenProjectDescription("rlm", "fake"));
    createModulePom("rlm", parent() + mavenProjectDescription("rlm", "swf"));

    importProject();
    checkBC("rlm", "rlm", new BuildConfigurationNature(Web, false, RuntimeLoadedModule), "", "rlm-1.0.swf", "target", "3.2.0.3958", "en_US",
            "target/rlm-1.0-config-report.xml");
  }

  public void testSignAirGoal() {
    importProject(mavenProjectDescription("project", "swf") +
                  "<build>" +
                  "  <sourceDirectory>src/main/flex</sourceDirectory>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <executions>\n" +
                  "        <execution>\n" +
                  "          <goals>\n" +
                  "            <goal>sign-air</goal>\n" +
                  "          </goals>\n" +
                  "        </execution>\n" +
                  "      </executions>\n" +
                  "      <dependencies>\n" +
                  "        <dependency>\n" +
                  "          <groupId>com.adobe.flex</groupId>\n" +
                  "          <artifactId>compiler</artifactId>\n" +
                  "          <version>3.5.0.12683</version>\n" +
                  "          <type>pom</type>\n" +
                  "        </dependency>\n" +
                  "      </dependencies>\n" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.5.0.12683"));

    checkBCCount("project", 1);
    final FlexBuildConfiguration bc =
      checkBC("project", "project", new BuildConfigurationNature(Desktop, false, Application), "", "project-1.0.swf", "target",
              "3.5.0.12683", "en_US", "target/project-1.0-config-report.xml");

    final Module module = ModuleManager.getInstance(myProject).findModuleByName("project");
    final String basePath = PathUtil.getParentPath(module.getModuleFilePath());

    assertFalse(bc.getAirDesktopPackagingOptions().isUseGeneratedDescriptor());
    assertEquals(basePath + "/src/main/resources/descriptor.xml",
                 bc.getAirDesktopPackagingOptions().getCustomDescriptorPath());
    assertFalse(bc.getAirDesktopPackagingOptions().getSigningOptions().isUseTempCertificate());
    assertEquals(basePath + "/src/main/resources/sign.p12",
                 bc.getAirDesktopPackagingOptions().getSigningOptions().getKeystorePath());
  }

  public void testAirPackaging() {
    importProject(mavenProjectDescription("project", "air") +
                  "<build>" +
                  "  <sourceDirectory>src/main/flex</sourceDirectory>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  flexmojosPlugin() +
                  "      <configuration>\n" +
                  "        <descriptorTemplate>d.xml</descriptorTemplate>\n" +
                  "        <keystore>c.p12</keystore>" +
                  "      </configuration>\n" +
                  "      <dependencies>\n" +
                  "        <dependency>\n" +
                  "          <groupId>com.adobe.flex</groupId>\n" +
                  "          <artifactId>compiler</artifactId>\n" +
                  "          <version>3.5.0.12683</version>\n" +
                  "          <type>pom</type>\n" +
                  "        </dependency>\n" +
                  "      </dependencies>\n" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +
                  flexFrameworkDependency("3.5.0.12683"));

    checkBCCount("project", 1);
    final FlexBuildConfiguration bc =
      checkBC("project", "project", new BuildConfigurationNature(Desktop, false, Application), "", "project-1.0.swf", "target",
              "3.5.0.12683", "en_US", "target/project-1.0-config-report.xml");

    assertFalse(bc.getAirDesktopPackagingOptions().isUseGeneratedDescriptor());
    assertEquals("d.xml", bc.getAirDesktopPackagingOptions().getCustomDescriptorPath());
    assertFalse(bc.getAirDesktopPackagingOptions().getSigningOptions().isUseTempCertificate());
    assertEquals("c.p12", bc.getAirDesktopPackagingOptions().getSigningOptions().getKeystorePath());
  }

  private void assertModuleType(final String moduleName, final ModuleType moduleType) {
    final Module module = ModuleManager.getInstance(myProject).findModuleByName(moduleName);
    assertNotNull("Module '" + moduleName + "' not found", module);
    assertEquals("Unexpected module type: " + ModuleType.get(module).getName(), moduleType, ModuleType.get(module));
  }

  private void checkBCOnBCDependencies(final String moduleName, final String... dependencyModuleNames) {
    final Module module = ModuleManager.getInstance(myProject).findModuleByName(moduleName);
    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(moduleName);
    final List<String> realDependencyModuleNames = new ArrayList<>();
    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        assertEquals(((BuildConfigurationEntry)entry).getModuleName(), ((BuildConfigurationEntry)entry).getBcName());
        realDependencyModuleNames.add(((BuildConfigurationEntry)entry).getModuleName() + ":"
                                      + entry.getDependencyType().getLinkageType().getShortText());
      }
    }
    assertSameElements(realDependencyModuleNames, dependencyModuleNames);
  }
}