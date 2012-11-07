package org.jetbrains.plugins.cucumber.groovy
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.plugins.groovy.GroovyLightProjectDescriptor
import org.jetbrains.plugins.groovy.LightGroovyTestCase
/**
 * @author Max Medvedev
 */
abstract class GrCucumberLightTestCase extends LightGroovyTestCase {
  static class GrCucumberLightProjectDescriptor extends GroovyLightProjectDescriptor {
    @Override
    void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
      super.configureModule(module, model, contentEntry)

      final Library.ModifiableModel modifiableModel = model.moduleLibraryTable.createLibrary("GROOVY_CUCUMBER").modifiableModel
      TestUtils.mockGroovyCucumberLibraryNames.each { jar ->
        final VirtualFile libJar = JarFileSystem.instance.refreshAndFindFileByPath("$jar!/")
        assert libJar != null
        modifiableModel.addRoot(libJar, OrderRootType.CLASSES)
      }
      modifiableModel.commit()
    }

    protected GrCucumberLightProjectDescriptor() {}

    public static final INSTANCE = new GrCucumberLightProjectDescriptor()
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return GrCucumberLightProjectDescriptor.INSTANCE
  }
}
