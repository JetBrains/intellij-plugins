package com.intellij.flex.maven;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.SharedLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FilteringIterator;
import org.jetbrains.annotations.Nullable;
import com.intellij.maven.testFramework.MavenImportingTestCase;

import java.util.List;

public abstract class FlexmojosImporterTestBase extends MavenImportingTestCase {
  protected static final String TEST_GROUP_ID = "com.intellij.flex.maven.test";
  protected static final String TEST_VERSION = "1.0";

  protected static final String SOURCE_DIR = "src";

  protected abstract String getFlexmojosVersion();

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

  protected void checkBCCount(final String moduleName, int bcCount) {
    final Module module = ModuleManager.getInstance(myProject).findModuleByName(moduleName);
    assertNotNull("Module '" + moduleName + "' not found", module);
    assertTrue(ModuleType.get(module) == FlexModuleType.getInstance());
    assertEquals(bcCount, FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations().length);
  }

  protected FlexBuildConfiguration checkBC(final String moduleName,
                                           final String bcName,
                                           final BuildConfigurationNature nature,
                                           final String mainClass,
                                           final String outputFileName,
                                           final String outputFolderRelPath,
                                           final String sdkVersion,
                                           final String locales,
                                           final String configFileRelPath) {
    final Module module = ModuleManager.getInstance(myProject).findModuleByName(moduleName);
    assertNotNull("Module '" + moduleName + "' not found", module);
    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(bcName);
    assertNotNull("Build configuration '" + bcName + "' not found", bc);
    checkLibrariesOfFlexType(module, bc);
    assertEquals(nature, bc.getNature());
    assertEquals(mainClass, bc.getMainClass());
    assertEquals(outputFileName, bc.getOutputFileName());
    assertEquals(PathUtil.getParentPath(module.getModuleFilePath()) + (outputFolderRelPath.isEmpty() ? "" : "/") + outputFolderRelPath,
                 bc.getOutputFolder());
    assertFalse(bc.isUseHtmlWrapper());
    assertFalse(bc.isSkipCompile());
    assertEquals(locales, bc.getCompilerOptions().getOption("compiler.locale"));
    assertEquals(getConfigFilesBasePath(module) + "/" + configFileRelPath, bc.getCompilerOptions().getAdditionalConfigFilePath());
    final Sdk sdk = bc.getSdk();
    assertNotNull(sdk);
    assertInstanceOf(sdk.getSdkType(), FlexmojosSdkType.class);
    assertEquals(sdk.getName(), "Flexmojos SDK " + sdkVersion);
    assertEquals(sdk.getVersionString(), sdkVersion);

    return bc;
  }

  protected abstract String getConfigFilesBasePath(final Module module);

  private static void checkLibrariesOfFlexType(final Module module, final FlexBuildConfiguration bc) {
    final List<LibraryOrderEntry> moduleLibEntries = ContainerUtil.filter(ModuleRootManager.getInstance(module).getOrderEntries(),
                                                                          new FilteringIterator.InstanceOf(LibraryOrderEntry.class));
    final List<SharedLibraryEntry> bcLibEntries = ContainerUtil.filter(bc.getDependencies().getEntries(),
                                                                       new FilteringIterator.InstanceOf(SharedLibraryEntry.class));
    assertTrue(bcLibEntries.size() > 0);
    assertEquals(moduleLibEntries.size(), bcLibEntries.size());
    for (SharedLibraryEntry entry : bcLibEntries) {
      assertTrue(entry.getLibraryName().contains(":swc:") ||
                 entry.getLibraryName().contains(":rb.swc:") ||
                 entry.getLibraryName().contains(":resource-bundle:"));
      assertEquals(LibraryTablesRegistrar.PROJECT_LEVEL, entry.getLibraryLevel());
      final Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), entry);
      assertNotNull(library);
      assertTrue(((LibraryEx)library).getKind() == FlexLibraryType.FLEX_LIBRARY);
      checkContainsLibrary(moduleLibEntries, library);
    }
  }

  private static void checkContainsLibrary(final List<LibraryOrderEntry> entries, final Library library) {
    for (LibraryOrderEntry entry : entries) {
      if (library.equals(entry.getLibrary())) return;
    }
    fail("Module entries do not contain library " + library.getName());
  }

  protected static String mavenProjectDescription(final String projectName, final String packaging) {
    return "<groupId>" + TEST_GROUP_ID + "</groupId>" +
           "<artifactId>" + projectName + "</artifactId>" +
           "<version>1.0</version>" +
           "<packaging>" + packaging + "</packaging>";
  }

  protected String flexmojosPlugin() {
    return "<groupId>org.sonatype.flexmojos</groupId>" +
           "<artifactId>flexmojos-maven-plugin</artifactId>" +
           "<version>" + getFlexmojosVersion() + "</version>" +
           "<extensions>true</extensions>";
  }

  protected String plugins() {
    return plugins(null, null);
  }

  protected String plugins(@Nullable String configuration, @Nullable String otherPlugins) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<build><sourceDirectory>").append(SOURCE_DIR).append("</sourceDirectory><plugins><plugin>")
      .append(flexmojosPlugin());
    if (configuration != null) {
      stringBuilder.append("<configuration>").append(configuration).append("</configuration>");
    }
    stringBuilder.append("</plugin>");
    if (otherPlugins != null) {
      stringBuilder.append(otherPlugins);
    }
    return stringBuilder.append("</plugins></build>").toString();
  }

  protected static String parent() {
    return "  <parent>\n" +
           "    <groupId>" + TEST_GROUP_ID + "</groupId>\n" +
           "    <artifactId>aggregator</artifactId>\n" +
           "    <version>1.0</version>\n" +
           "  </parent>\n";
  }

  protected static String dependencies(String artifactId) {
    return dependencies(artifactId, "swc");
  }

  protected static String dependencies(String artifactId, String type) {
    return "  <dependencies>\n" +
           "    <dependency>\n" +
           "      <groupId>" + TEST_GROUP_ID + "</groupId>\n" +
           "      <artifactId>" + artifactId + "</artifactId>\n" +
           "      <version>1.0</version>\n" +
           "      <type>" + type + "</type>\n" +
           "    </dependency>\n" +
           "  </dependencies>";
  }

  protected static String flexFrameworkDependency(final String version) {
    return "  <dependencies>\n" +
           "    <dependency>\n" +
           "      <groupId>com.adobe.flex.framework</groupId>" +
           "      <artifactId>flex-framework</artifactId>" +
           "      <version>" + version + "</version>" +
           "      <type>pom</type>" +
           "    </dependency>\n" +
           "  </dependencies>" +
           repository();
  }

  protected static String repository() {
    return "  <repositories>\n" +
           "    <repository>\n" +
           "      <id>flex-repository</id>\n" +
           "      <url>https://repo.labs.intellij.net/flex</url>\n" +
           "      <releases>\n" +
           "        <enabled>true</enabled>\n" +
           "      </releases>\n" +
           "    </repository>\n" +
           "  </repositories>\n" +
           "  <pluginRepositories>\n" +
           "    <pluginRepository>\n" +
           "      <id>flex-repository</id>\n" +
           "      <url>https://repo.labs.intellij.net/flex</url>\n" +
           "      <releases>\n" +
           "        <enabled>true</enabled>\n" +
           "      </releases>\n" +
           "    </pluginRepository>\n" +
           "  </pluginRepositories>\n";
  }
}
