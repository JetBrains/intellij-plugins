package com.intellij.lang.javascript.maven;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.javascript.flex.maven.Flexmojos4Configurator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.server.MavenServerManager;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.File;
import java.io.IOException;

import static com.intellij.flex.model.bc.OutputType.Application;
import static com.intellij.flex.model.bc.OutputType.RuntimeLoadedModule;
import static com.intellij.flex.model.bc.TargetPlatform.Web;

public class Flexmojos4ImporterTest extends FlexmojosImporterTestBase {

  private String myInitialMavenHome;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myInitialMavenHome = getMavenGeneralSettings().getMavenHome();
    // Flexmojos4 config generator can't run on IDEA's default Maven 3.3.9 because of https://issues.apache.org/jira/browse/MNG-5958
    final File maven3Home = MavenServerManager.getMavenHomeFile(MavenServerManager.BUNDLED_MAVEN_3); // 3.3.9
    // switch to Maven 3.0.5
    getMavenGeneralSettings().setMavenHome(StringUtil.replace(maven3Home.getPath(), "maven3-server-impl", "maven30-server-impl"));
  }

  @Override
  protected void tearDown() throws Exception {
    getMavenGeneralSettings().setMavenHome(myInitialMavenHome);
    super.tearDown();
  }

  @Override
  protected String getFlexmojosVersion() {
    return "4.0-RC2";
  }

  protected String getConfigFilesBasePath(final Module module) {
    return module.getProject().getBaseDir().getPath();
  }

  public void testAppWithModules() throws Exception {
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

  public void testTransitiveDepsOnPartialProjectImport() throws IOException, MavenProcessCanceledException {
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
    createProjectSubFile("app/src/Main.as", "package {\n" +
                                        "import flash.display.Sprite;\n" +
                                        "\n" +
                                        "public class Main extends Sprite {\n" +
                                        "  public function Main() {\n" +
                                        "    new B();\n" +
                                        "  }\n" +
                                        "}\n" +
                                        "}");

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
                                  "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/libA/target/libA-1.0.swc") + "</path-element>\n" +
                                  "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/libB/target/libB-1.0.swc") + "</path-element>\n" +
                                  "\t\t</library-path>", 0);
    assertContains(s, "\t<file-specs>\n" +
                      "\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/app/src/Main.as") + "</path-element>\n" +
                      "\t</file-specs>", index);

    s = loadContent(p, "libA-com.intellij.flex.maven.test.xml");
    final String playerGlobalPathElement =
      "\t\t\t<path-element>" + FileUtil.toSystemDependentName(localRepoDir + "/com/adobe/flex/framework/playerglobal/4.5.1.21328/10.2/playerglobal.swc") + "</path-element>\n";
    assertContains(s, "\t\t<external-library-path>\n" +
                      "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/libB/target/libB-1.0.swc") + "</path-element>\n" +
                      playerGlobalPathElement +
                      "\t\t</external-library-path>", 0);

    s = loadContent(p, "libB-com.intellij.flex.maven.test.xml");
    assertContains(s, "\t\t<external-library-path>\n" +
                      playerGlobalPathElement +
                      "\t\t</external-library-path>", 0);
  }

  public void testAdditionalCompileSourceRoots() throws IOException, MavenProcessCanceledException {
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
                     plugins(null, "<plugin>\n" +
                                   "        <groupId>org.codehaus.mojo</groupId>\n" +
                                   "        <artifactId>build-helper-maven-plugin</artifactId>\n" +
                                   "        <executions>\n" +
                                   "          <execution>\n" +
                                   "            <id>add-sources</id>\n" +
                                   "            <phase>generate-sources</phase>\n" +
                                   "            <goals>\n" +
                                   "              <goal>add-source</goal>\n" +
                                   "            </goals>\n" +
                                   "            <configuration>\n" +
                                   "              <sources>\n" +
                                   "                <source>localAnotherSourceRoot</source>\n" +
                                   "                <source>${env." + getEnvVar() + "}</source>\n" +
                                   "              </sources>\n" +
                                   "            </configuration>\n" +
                                   "          </execution>\n" +
                                   "        </executions>\n" +
                                   "      </plugin>"));
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
                   "\t\t\t<path-element>" + FileUtil.toSystemDependentName(rootDir + "/localAnotherSourceRoot") + "</path-element>\n" +
                   "\t\t\t<path-element>" + new File(System.getenv(getEnvVar())).getCanonicalPath() + "</path-element>\n";
    int index = assertContains(s, "\t\t<source-path>\n" + paths + "\t\t</source-path>", 0);
    assertContains(s, "\t<include-sources>\n" + paths.replace("\t\t\t", "\t\t") + "\t</include-sources>", index);

    // well, current idea maven support impl doesn't add source folder if it is not part of the content root
    assertSources("testAdditionalCompileSourceRoots", "localAnotherSourceRoot", "src");
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