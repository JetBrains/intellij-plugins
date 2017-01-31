package training.lang

import com.intellij.ide.impl.NewProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl
import com.intellij.openapi.util.Computable
import training.learn.Lesson
import training.util.JdkSetupUtil
import java.util.*

/**
 * @author Sergey Karashevich
 */
class JavaLangSupport : LangSupport {

    private val acceptableExtensions = setOf("java", "kt", "html")

    override fun acceptExtension(ext: String) = acceptableExtensions.contains(ext)

    override val FILE_EXTENSION: String
        get() = "java"

    override fun initLearnProject(project: Project?): Project {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLangName() = "Java"

    override fun openLessonInFile(lesson: Lesson) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyProjectSdk(): (Project) -> Unit = { newProject ->
        val projectSdk = getJavaSdkInWA()
        if (projectSdk != null) {
            CommandProcessor.getInstance().executeCommand(newProject, { ApplicationManager.getApplication().runWriteAction { NewProjectUtil.applyJdkToProject(newProject, projectSdk) } }, null, null)
        }
    }


    //Java SDK and project configuration staff

    private fun getJavaSdkInWA() =
        if (ApplicationManager.getApplication().isUnitTestMode)
            ApplicationManager.getApplication().runWriteAction({ getJavaSdk() } as Computable<Sdk>)
        else
            getJavaSdk()


    private fun getJavaSdk(): Sdk {

        //check for stored jdk
        val jdkList = getJdkList()
        if (!jdkList.isEmpty()) {
            jdkList
                    .filter { JavaSdk.getInstance().getVersion(it) != null && JavaSdk.getInstance().getVersion(it)!!.isAtLeast(JavaSdkVersion.JDK_1_6) }
                    .forEach { return it }
        }

        //if no predefined jdks -> add bundled jdk to available list and return it
        val javaSdk = JavaSdk.getInstance()

        val bundleList = JdkSetupUtil.findJdkPaths().toArrayList()
        //we believe that Idea has at least one bundled jdk
        val jdkBundle = bundleList[0]
        val jdkBundleLocation = JdkSetupUtil.getJavaHomePath(jdkBundle)
        val jdk_name = "JDK_" + jdkBundle.version!!.toString()
        val newJdk = javaSdk.createJdk(jdk_name, jdkBundleLocation, false)

        val foundJdk = ProjectJdkTable.getInstance().findJdk(newJdk.name, newJdk.sdkType.name)
        if (foundJdk == null) ApplicationManager.getApplication().runWriteAction { ProjectJdkTable.getInstance().addJdk(newJdk) }

        ApplicationManager.getApplication().runWriteAction {
            val modifier = newJdk.sdkModificator
            JavaSdkImpl.attachJdkAnnotations(modifier)
            modifier.commitChanges()
        }

        return newJdk
    }


    private fun getJdkList(): ArrayList<Sdk> {

        val type = JavaSdk.getInstance()
        val allJdks = ProjectJdkTable.getInstance().allJdks
        val compatibleJdks = allJdks.filterTo(ArrayList<Sdk>()) { isCompatibleJdk(it, type) }
        return compatibleJdks
    }

    private fun isCompatibleJdk(projectJdk: Sdk, type: SdkType?) = (type == null || projectJdk.sdkType === type)

}