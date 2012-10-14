package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveTest;

/**
 * User: Andrey.Vokin
 * Date: 8/9/12
 */
public abstract class BaseCucumberJavaResolveTest extends CucumberResolveTest {
  @Nullable
  @Override
  protected String getStepDefinitionName(@NotNull final PsiElement stepDefinition) {
    if (stepDefinition instanceof PsiMethod) {
      return ((PsiMethod)stepDefinition).getName();
    }
    return null;
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "resolve";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }

  public static final DefaultLightProjectDescriptor DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
      VirtualFile sourceRoot = VirtualFileManager.getInstance().refreshAndFindFileByUrl("temp:///src");
      if (sourceRoot != null) {
        contentEntry.removeSourceFolder(contentEntry.getSourceFolders()[0]);
        contentEntry.addSourceFolder(sourceRoot, true);
      }
    }
  };
}
