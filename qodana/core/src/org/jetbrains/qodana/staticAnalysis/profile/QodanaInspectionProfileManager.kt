package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.InspectionProfile
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.configurationStore.OLD_NAME_CONVERTER
import com.intellij.configurationStore.SchemeDataHolder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.options.SchemeManagerFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.packageDependencies.DependencyValidationManager
import com.intellij.profile.codeInspection.*
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder
import com.intellij.util.application
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.profile.providers.QODANA_EMPTY_PROFILE_NAME

const val QODANA_BASE_PROFILE_NAME = "Qodana Base Inspection Profile"

abstract class QodanaInspectionProfileManager(
  val managerProject: Project?,
  schemeFactory: SchemeManagerFactory,
  dir: String
) : BaseInspectionProfileManager(managerProject?.messageBus ?: application.messageBus) {
  companion object {
    fun getInstance(project: Project? = null): QodanaInspectionProfileManager {
      return if (project != null) {
        QodanaProjectInspectionProfileManager.getInstance(project)
      } else {
        QodanaApplicationInspectionProfileManager.getInstance()
      }
    }
  }

  init {
    initPlatformProfileManager(managerProject)
  }

  abstract fun defaultScheme(): QodanaInspectionProfile

  override val schemeManager = schemeFactory.create(dir, object : InspectionProfileProcessor() {
    override fun createScheme(dataHolder: SchemeDataHolder<InspectionProfileImpl>,
                              name: String,
                              attributeProvider: (String) -> String?,
                              isBundled: Boolean): InspectionProfileImpl {
      return QodanaInspectionProfile.newWithEnabledByDefaultTools(name, this@QodanaInspectionProfileManager, dataHolder).apply {
        this.isProjectLevel = (this@QodanaInspectionProfileManager is ProjectBasedInspectionProfileManager)
      }
    }

    override fun isSchemeFile(name: CharSequence) = !StringUtil.equals(name, PROFILES_SETTINGS)
  }, schemeNameToFileName = OLD_NAME_CONVERTER, isAutoSave = false)

  override fun fireProfileChanged(profile: InspectionProfileImpl) { }

  val qodanaBaseProfile: QodanaInspectionProfile by lazy { createQodanaBaseProfile() }

  val qodanaEmptyProfile: QodanaInspectionProfile by lazy { createQodanaEmptyProfile() }

  @VisibleForTesting
  fun createQodanaBaseProfile(): QodanaInspectionProfile {
    return QodanaInspectionProfile(QODANA_BASE_PROFILE_NAME, this, baseProfile = null)
  }

  @VisibleForTesting
  fun createQodanaEmptyProfile(): QodanaInspectionProfile {
    @Language("XML")
    val content = """
        <profile version="1.0" is_locked="true">
            <option name="myName" value="$QODANA_EMPTY_PROFILE_NAME"/>
        </profile>
      """.trimIndent()
    val element = JDOMUtil.load(content).profileElement
    return QodanaInspectionProfile(QODANA_EMPTY_PROFILE_NAME, this, baseProfile = null).apply {
      readExternal(element)
    }
  }

  override fun getProfiles(): Collection<InspectionProfileImpl> {
    schemeManager.reload()
    currentProfile
    return schemeManager.allSchemes
  }

  override fun setRootProfile(name: String?) {  }

  @Synchronized
  override fun getCurrentProfile(): InspectionProfileImpl {
    if (schemeManager.findSchemeByName(PROJECT_DEFAULT_PROFILE_NAME) == null) {
      schemeManager.addScheme(defaultScheme(), true)
    }
    val currentScheme = schemeManager.allSchemes.first()
    schemeManager.setCurrent(currentScheme, false)
    return currentScheme
  }

  @Synchronized
  override fun getProfile(name: String, returnRootProfileIfNamedIsAbsent: Boolean): InspectionProfileImpl? {
    profiles
    val project = (this as? ProjectBasedInspectionProfileManager)?.project
    return schemeManager.findSchemeByName(name) ?: QodanaInspectionProfileProvider.runProviders(name, project)
  }

  fun getQodanaProfile(name: String): QodanaInspectionProfile? {
    return getProfile(name, false) as? QodanaInspectionProfile
  }

  fun cleanupProfiles(project: Project) {
    for (profile in schemeManager.allSchemes) {
      profile.cleanup(project)
    }
  }
}

@Service(Service.Level.APP)
class QodanaApplicationInspectionProfileManager :
  QodanaInspectionProfileManager(managerProject = null, SchemeManagerFactory.getInstance(), InspectionProfileManager.INSPECTION_DIR) {
  companion object {
    fun getInstance(): QodanaApplicationInspectionProfileManager = service()
  }

  override fun defaultScheme(): QodanaInspectionProfile =
    QodanaInspectionProfile.newWithEnabledByDefaultTools(InspectionProfile.DEFAULT_PROFILE_NAME, this)
}

@Service(Service.Level.PROJECT)
class QodanaProjectInspectionProfileManager(override val project: Project) :
  QodanaInspectionProfileManager(project, SchemeManagerFactory.getInstance(project), PROFILE_DIR),
  ProjectBasedInspectionProfileManager,
  Disposable {
  companion object {
    fun getInstance(project: Project): QodanaProjectInspectionProfileManager = project.service()
  }

  override fun defaultScheme(): QodanaInspectionProfile {
    return QodanaInspectionProfile.newWithEnabledByDefaultTools(PROJECT_DEFAULT_PROFILE_NAME, this).apply {
      isProjectLevel = true
    }
  }

  override fun dispose() {
    val cleanupInspectionProfilesRunnable = {
      cleanupProfiles(project)
      serviceIfCreated<QodanaApplicationInspectionProfileManager>()?.cleanupProfiles(project)
    }

    val app = ApplicationManager.getApplication()
    if (app.isUnitTestMode || app.isHeadlessEnvironment) {
      cleanupInspectionProfilesRunnable.invoke()
    }
    else {
      app.executeOnPooledThread(cleanupInspectionProfilesRunnable)
    }
  }

  override fun getProfile(name: String, returnRootProfileIfNamedIsAbsent: Boolean): InspectionProfileImpl? {
    super.getProfile(name, returnRootProfileIfNamedIsAbsent)?.let { return it }
    return getInstance().getProfile(name, returnRootProfileIfNamedIsAbsent)
  }

  fun getAllProfiles(): List<QodanaInspectionProfile> = QodanaInspectionProfileProvider.allProfiles(project)

  override fun getScopesManager(): NamedScopesHolder {
    return DependencyValidationManager.getInstance(project)
  }
}

private fun initPlatformProfileManager(project: Project? = null) {
  if (project == null) InspectionProfileManager.getInstance() else InspectionProfileManager.getInstance(project)
}
