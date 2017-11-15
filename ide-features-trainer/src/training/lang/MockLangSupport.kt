package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId

/**
 * @author Sergey Karashevich
 */
class MockLangSupport(override val FILE_EXTENSION: String) : AbstractLangSupport() {
  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun checkSdkCompatibility(sdk: Sdk, sdkTypeId: SdkTypeId) {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun acceptLang(ext: String): Boolean {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun applyProjectSdk(project: Project) {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


}
