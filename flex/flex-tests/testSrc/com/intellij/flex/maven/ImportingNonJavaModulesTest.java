package com.intellij.flex.maven;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.maven.testFramework.MavenImportingTestCase;

import java.util.concurrent.atomic.AtomicInteger;

public class ImportingNonJavaModulesTest extends MavenImportingTestCase {
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

  public void testRecreatingNonJavaModules() {
    Module m = createModule("flex-module", FlexModuleType.getInstance());

    VirtualFile file = createModulePom("flex-module",
                                       "<groupId>test</groupId>" +
                                       "<artifactId>flex-project</artifactId>" +
                                       "<version>1</version>");

    AtomicInteger counter = configConfirmationForYesAnswer();
    importProject(file);

    assertEquals(1, counter.get());
    assertTrue(m.isDisposed());

    assertModules("flex-project");
    assertEquals(StdModuleTypes.JAVA, ModuleType.get(getModule("flex-project")));
  }

  public void testChangeJavaModuleToFlexModule() {
    Module m = createModule("flex-module", StdModuleTypes.JAVA);

    VirtualFile file = createModulePom("flex-module",
                                       "<groupId>test</groupId>" +
                                       "<artifactId>flex-project</artifactId>" +
                                       "<version>1</version>" +
                                       "<packaging>swf</packaging>" +

                                       "<build>" +
                                       "  <plugins>" +
                                       "    <plugin>" +
                                       "      <groupId>org.sonatype.flexmojos</groupId>" +
                                       "      <artifactId>flexmojos-maven-plugin</artifactId>" +
                                       "      <version>3.5.0</version>" +
                                       "      <extensions>true</extensions>" +
                                       "     </plugin>" +
                                       "  </plugins>" +
                                       "</build>");

    AtomicInteger counter = configConfirmationForYesAnswer();
    importProject(file);

    assertEquals(1, counter.get());
    assertTrue(m.isDisposed());

    assertModules("flex-project");
    assertEquals(FlexModuleType.getInstance(), ModuleType.get(getModule("flex-project")));
  }

  public void testDoNotRecreateNonJavaModulesIfUserDoNotWantTo() {
    Module m = createModule("flex-module", FlexModuleType.getInstance());

    VirtualFile file = createModulePom("flex-module",
                                       "<groupId>test</groupId>" +
                                       "<artifactId>flex-project</artifactId>" +
                                       "<version>1</version>");

    configConfirmationForNoAnswer();
    importProject(file);

    assertFalse(m.isDisposed());
    assertModules("flex-module");
    assertSame(m, getModule("flex-module"));

    assertTrue(myProjectsManager.isIgnored(myProjectsManager.findProject(m)));
  }

  public void testDoNotAskToRecreateNonJavaModulesTwice() {
    createModule("flex-module", FlexModuleType.getInstance());

    VirtualFile file = createModulePom("flex-module",
                                       "<groupId>test</groupId>" +
                                       "<artifactId>flex-project</artifactId>" +
                                       "<version>1</version>");

    AtomicInteger counter = configConfirmationForNoAnswer();
    importProject(file);
    importProject(file);

    assertEquals(1, counter.get());
  }
}
