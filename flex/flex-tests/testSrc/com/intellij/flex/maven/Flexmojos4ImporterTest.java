// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.flex.maven;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.javascript.flex.maven.Flexmojos4Configurator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ContentFolder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.server.MavenServerManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.flex.model.bc.OutputType.Application;
import static com.intellij.flex.model.bc.OutputType.RuntimeLoadedModule;
import static com.intellij.flex.model.bc.TargetPlatform.Web;

public class Flexmojos4ImporterTest extends FlexmojosImporterTestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    MavenServerManager.getInstance().shutdown(true);

    // Flexmojos4 config generator can't run on IDEA's default Maven 3.3.9 because of https://issues.apache.org/jira/browse/MNG-5958
    final File maven3Home = MavenUtil.getMavenHomeFile(MavenServerManager.BUNDLED_MAVEN_3); // 3.3.9
    // switch to Maven 3.0.5
    getMavenGeneralSettings().setMavenHome(StringUtil.replace(maven3Home.getPath(), "maven3-server-impl", "maven30-server-impl"));
  }

  @Override
  protected String getFlexmojosVersion() {
    return "4.0-RC2";
  }

  @Override
  protected String getConfigFilesBasePath(final Module module) {
    return module.getProject().getBasePath();
  }

  public void testAppWithModules() {
    // hacky way to make sure that compiler.pom is downloaded.
    importProject(mavenProjectDescription("fake", "pom") +
                  "  <dependencies>" +
                  "    <dependency>\n" +
                  "      <groupId>com.adobe.flex</groupId>" +
                  "      <artifactId>compiler</artifactId>" +
                  "      <version>4.5.1.21328</version>" +
                  "      <type>pom</type>" +
                  "    </dependency>\n" +
                  "  </dependencies>");

    final String pomContent = mavenProjectDescription("project", "swf") +
                              "<properties>" +
                              "  <my.suffix>zz</my.suffix>" +
                              "</properties>" +
                              "<build>" +
                              "  <plugins>" +
                              "    <plugin>" +
                              flexmojosPlugin() +
                              "      <dependencies>\n" +
                              "        <dependency>\n" +
                              "          <groupId>com.adobe.flex</groupId>\n" +
                              "          <artifactId>compiler</artifactId>\n" +
                              "          <version>4.5.1.21328</version>\n" +
                              "          <type>pom</type>\n" +
                              "        </dependency>\n" +
                              "      </dependencies>\n" +
                              "      <configuration>" +
                              "        <sourceFile>\n   pack\\pack2/Main.mxml\n   </sourceFile>" +
                              "        <classifier>\n   ${my.suffix}\n   </classifier>" +
                              "        <output>\n   output.swf\n   </output>" +
                              "        <moduleFiles>" +
                              "          <moduleFile>Module0.mxml</moduleFile>" +
                              "        </moduleFiles>" +
                              "        <modules>" +
                              "          <qq>\n  pack/ModuleBIG.mxml\n  </qq>" +
                              "          <qq>" +
                              "            <sourceFile> Module2MainClass.as</sourceFile>" +
                              "            <optimize>true</optimize>" +
                              "            <finalName>Module2FinalName</finalName>" +
                              "            <destinationPath>dir1\\dir2</destinationPath>" +
                              "          </qq>" +
                              "        </modules>" +
                              "      </configuration>" +
                              "     </plugin>" +
                              "  </plugins>" +
                              "</build>" +
                              flexFrameworkDependency("4.5.1.21328");

    importProject(pomContent);

    checkBCCount("project", 3);
    checkBC("project", "project", new BuildConfigurationNature(Web, false, Application), "pack.pack2.Main", "project-1.0-zz.swf", "target",
            "4.5.1.21328", "en_US", ".idea/flexmojos/project-com.intellij.flex.maven.test-zz.xml");
    checkBC("project", "ModuleBIG", new BuildConfigurationNature(Web, false, RuntimeLoadedModule), "pack.ModuleBIG",
            "project-1.0-modulebig.swf", "target", "4.5.1.21328", "en_US",
            ".idea/flexmojos/project-com.intellij.flex.maven.test-ModuleBIG.xml");
    checkBC("project", "Module2FinalName", new BuildConfigurationNature(Web, false, RuntimeLoadedModule), "Module2MainClass",
            "Module2FinalName.swf", "target/dir1/dir2", "4.5.1.21328", "en_US",
            ".idea/flexmojos/project-com.intellij.flex.maven.test-Module2FinalName.xml");
  }

  public void testTransitiveDepsOnPartialProjectImport() throws IOException {
    final File mavenHome = MavenUtil.resolveMavenHomeDirectory(myProjectsManager.getGeneralSettings().getMavenHome());
    if (mavenHome == null || !MavenUtil.isValidMavenHome(mavenHome)) {
      return;
    }

    createProjectPom("  <groupId>" + TEST_GROUP_ID + "</groupId>\n" +
                     "  <artifactId>aggregator</artifactId>\n" +
                     "  <version>1.0</version>\n" +
                     "  <packaging>pom</packaging>\n" +
                     "  \n" +
                     "<properties>\n" +
                     "    <fdk.version>4.5.1.21328</fdk.version>\n" +
                     "  </properties>" +
                     "  <modules>\n" +
                     "    <module>app</module>\n" +
                     "    <module>libA</module>\n" +
                     "    <module>libB</module>\n" +
                     "  </modules>\n" +
                     "  <dependencies>\n" +
                     "    <dependency>\n" +
                     "      <groupId>com.adobe.flex.framework</groupId>\n" +
                     "      <artifactId>playerglobal</artifactId>\n" +
                     "      <type>swc</type>\n" +
                     "      <classifier>10.2</classifier>\n" +
                     "      <version>${fdk.version}</version>\n" +
                     "    </dependency>\n" +
                     "  </dependencies>" +
                     repository() +
                     plugins());

    final VirtualFile app = createModulePom("app", parent() +
                                                   "  <artifactId>app</artifactId>\n" +
                                                   "  <packaging>swf</packaging>\n" +
                                                   "\n" + dependencies("libA"));
    createProjectSubFile("app/src/Main.as", """
      package {
      import flash.display.Sprite;

      public class Main extends Sprite {
        public function Main() {
          new B();
        }
      }
      }""");

    createModulePom("libA", parent() +
                            "  <artifactId>libA</artifactId>\n" +
                            "  <packaging>swc</packaging>\n" +
                            "  \n" + dependencies("libB"));

    createModulePom("libB", parent() +
                            "  <artifactId>libB</artifactId>\n" +
                            "  <packaging>swc</packaging>");

    importProject();
    // must be
    performPostImportTasks();

    // must be getCanonicalPath, according to flexmojos (see org.sonatype.flexmojos.util.PathUtil)
    final String localRepoDir = myProjectsManager.getGeneralSettings().getEffectiveLocalRepository().getCanonicalPath();

    final String rootDir = myProjectRoot.getPath();
    final String compilerConfigsDir = Flexmojos4Configurator.getCompilerConfigsDir(myProject);

    // must be findFileByPath, not refreshAndFindFileByPath - as part of testing
    final VirtualFile p = LocalFileSystem.getInstance().findFileByPath(compilerConfigsDir);
    assertNotNull(p);
    String s = loadContent(p, "app-com.intellij.flex.maven.test.xml");
    int index = assertContains(s, "\t\t<library-path>\n" +
                                  "\t\t\t<path-element>" +
                                  FileUtil.toSystemDependentName(rootDir + "/libA/target/libA-1.0.swc") +
                                  "</path-element>\n" +
                                  "\t\t\t<path-element>" +
                                  FileUtil.toSystemDependentName(rootDir + "/libB/target/libB-1.0.swc") +
                                  "</path-element>\n" +
                                  "\t\t</library-path>", 0);
    assertContains(s, "\t<file-specs>\n" +
                      "\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/app/src/Main.as") + "</path-element>\n" +
                      "\t</file-specs>", index);

    s = loadContent(p, "libA-com.intellij.flex.maven.test.xml");
    final String playerGlobalPathElement =
      "\t\t\t<path-element>" +
      FileUtil.toSystemDependentName(localRepoDir + "/com/adobe/flex/framework/playerglobal/4.5.1.21328/10.2/playerglobal.swc") +
      "</path-element>\n";
    assertContains(s, "\t\t<external-library-path>\n" +
                      "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/libB/target/libB-1.0.swc") + "</path-element>\n" +
                      playerGlobalPathElement +
                      "\t\t</external-library-path>", 0);

    s = loadContent(p, "libB-com.intellij.flex.maven.test.xml");
    assertContains(s, "\t\t<external-library-path>\n" +
                      playerGlobalPathElement +
                      "\t\t</external-library-path>", 0);
  }

  public void testAdditionalCompileSourceRoots() throws IOException {
    final File mavenHome = MavenUtil.resolveMavenHomeDirectory(myProjectsManager.getGeneralSettings().getMavenHome());
    if (mavenHome == null || !MavenUtil.isValidMavenHome(mavenHome)) {
      return;
    }

    createProjectPom("  <groupId>" + TEST_GROUP_ID + "</groupId>\n" +
                     "  <artifactId>testAdditionalCompileSourceRoots</artifactId>\n" +
                     "  <version>1.0</version>\n" +
                     "  <packaging>swc</packaging>\n" +
                     "  \n" +
                     "<properties>\n" +
                     "    <fdk.version>4.5.1.21328</fdk.version>\n" +
                     "  </properties>" +
                     "  <dependencies>\n" +
                     "    <dependency>\n" +
                     "      <groupId>com.adobe.flex.framework</groupId>\n" +
                     "      <artifactId>playerglobal</artifactId>\n" +
                     "      <type>swc</type>\n" +
                     "      <classifier>10.2</classifier>\n" +
                     "      <version>${fdk.version}</version>\n" +
                     "    </dependency>\n" +
                     "  </dependencies>" +
                     repository() +
                     plugins(null, """
                       <plugin>
                               <groupId>org.codehaus.mojo</groupId>
                               <artifactId>build-helper-maven-plugin</artifactId>
                               <executions>
                                 <execution>
                                   <id>add-sources</id>
                                   <phase>generate-sources</phase>
                                   <goals>
                                     <goal>add-source</goal>
                                   </goals>
                                   <configuration>
                                     <sources>
                                       <source>localAnotherSourceRoot</source>
                                     </sources>
                                   </configuration>
                                 </execution>
                               </executions>
                             </plugin>"""));
    createProjectSubDir("localAnotherSourceRoot");
    createProjectSubDir(SOURCE_DIR);

    importProject();
    // must be
    performPostImportTasks();

    final String compilerConfigsDir = Flexmojos4Configurator.getCompilerConfigsDir(myProject);
    final VirtualFile p = LocalFileSystem.getInstance().findFileByPath(compilerConfigsDir);
    assertNotNull(p);
    String s = loadContent(p, "testAdditionalCompileSourceRoots-" + TEST_GROUP_ID + ".xml");

    final String rootDir = myProjectRoot.getPath();
    String paths = "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/" + SOURCE_DIR) + "</path-element>\n" +
                   "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/localAnotherSourceRoot") + "</path-element>\n";
    int index = assertContains(s, "\t\t<source-path>\n" + paths + "\t\t</source-path>", 0);
    assertContains(s, "\t<include-sources>\n" + paths.replace("\t\t\t", "\t\t") + "\t</include-sources>", index);

    // well, current idea maven support impl doesn't add source folder if it is not part of the content root
    assertSources("testAdditionalCompileSourceRoots", "localAnotherSourceRoot", "src");
  }

  private void assertSources(String moduleName, String... expectedSources) {
    doAssertContentFolders(moduleName, JavaSourceRootType.SOURCE, expectedSources);
  }

  private void doAssertContentFolders(String moduleName, @NotNull JpsModuleSourceRootType<?> rootType, String... expected) {
    ContentEntry contentRoot = getContentRoot(moduleName);
    doAssertContentFolders(contentRoot, contentRoot.getSourceFolders(rootType), expected);
  }

  private ContentEntry getContentRoot(String moduleName) {
    ContentEntry[] ee = getContentRoots(moduleName);
    List<String> roots = new ArrayList<>();
    for (ContentEntry e : ee) {
      roots.add(e.getUrl());
    }

    String message = "Several content roots found: [" + StringUtil.join(roots, ", ") + "]";
    assertEquals(message, 1, ee.length);

    return ee[0];
  }

  private static void doAssertContentFolders(ContentEntry e,
                                             final List<? extends ContentFolder> folders,
                                             String... expected) {
    List<String> actual = new ArrayList<>();
    for (ContentFolder f : folders) {
      String rootUrl = e.getUrl();
      String folderUrl = f.getUrl();

      if (folderUrl.startsWith(rootUrl)) {
        int length = rootUrl.length() + 1;
        folderUrl = folderUrl.substring(Math.min(length, folderUrl.length()));
      }

      actual.add(folderUrl);
    }

    assertSameElements("Unexpected list of folders in content root " + e.getUrl(),
                       actual, Arrays.asList(expected));
  }

  private static String loadContent(VirtualFile p, String name) throws IOException {
    final VirtualFile appConfig = p.findChild(name);
    assertNotNull(appConfig);
    return VfsUtilCore.loadText(appConfig);
  }

  private static int assertContains(String content, String s, int fromIndex) {
    int index = content.indexOf(s, fromIndex);
    assertTrue("Content: " + content + "\n\n Expected substring: " + s, index != -1);
    return index + s.length();
  }
}