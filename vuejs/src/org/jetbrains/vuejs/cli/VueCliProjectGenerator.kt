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
package org.jetbrains.vuejs.cli

import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.util.projectWizard.*
import com.intellij.internal.statistic.UsageTrigger
import com.intellij.javascript.nodejs.packageJson.PackageJsonDependenciesExternalUpdateManager
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.ui.RelativeFont
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.PathUtil
import com.intellij.util.io.exists
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.util.ui.UIUtil
import icons.VuejsIcons
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.*
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.*

class VueCliProjectGenerator : WebProjectTemplate<NpmPackageProjectGenerator.Settings>(),
                               CustomStepProjectGenerator<NpmPackageProjectGenerator.Settings> {
  companion object {
    internal val TEMPLATE_KEY = Key.create<String>("create.vue.app.project.template")
    private val TEMPLATES = mapOf(
      Pair("browserify", "A full-featured Browserify + vueify setup with hot-reload, linting & unit testing"),
      Pair("browserify-simple", "A simple Browserify + vueify setup for quick prototyping"),
      Pair("pwa", "PWA template for vue-cli based on the webpack template"),
      Pair("simple", "The simplest possible Vue setup in a single HTML file"),
      Pair("webpack", "A full-featured Webpack + vue-loader setup with hot reload, linting, testing & css extraction"),
      Pair("webpack-simple", "A simple Webpack + vue-loader setup for quick prototyping")
    )
  }

  override fun createStep(projectGenerator: DirectoryProjectGenerator<NpmPackageProjectGenerator.Settings>?,
                          callback: AbstractNewProjectStep.AbstractCallback<NpmPackageProjectGenerator.Settings>?): AbstractActionWithPanel {
    return VueCliProjectSettingsStep(projectGenerator, callback)
  }

  override fun createPeer(): ProjectGeneratorPeer<NpmPackageProjectGenerator.Settings> {
    return object : NpmPackageProjectGenerator.NpmPackageGeneratorPeer("vue-cli", "vue-cli", { null }) {
      var template: ComboBox<*>? = null

      override fun createPanel(): JPanel {
        val panel = super.createPanel()
        template = ComboBox(TEMPLATES.keys.toTypedArray())
        template!!.selectedItem = "webpack"
        template!!.isEditable = true
        template!!.renderer = object: ColoredListCellRenderer<Any?>() {
          override fun customizeCellRenderer(list: JList<out Any?>, value: Any?, index: Int, selected: Boolean, hasFocus: Boolean) {
            if (value is String) {
              append(value)
              val comment = TEMPLATES[value] ?: return
              append(" ")
              append(comment, SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
          }
        }
        val component = LabeledComponent.create(template!!, "Project template")
        component.labelLocation = BorderLayout.WEST
        component.anchor = panel.getComponent(0) as JComponent
        panel.add(component)
        return panel
      }

      @Suppress("OverridingDeprecatedMember", "DEPRECATION")
      override fun addSettingsStateListener(listener: SettingsStateListener) {
        super.addSettingsStateListener(listener)
        template!!.editor.editorComponent.addKeyListener(object: KeyAdapter() {
          override fun keyReleased(e: KeyEvent?) {
            listener.stateChanged(validate() == null)
          }
        })
        template!!.editor.editorComponent.addInputMethodListener(object: InputMethodListener {
          override fun caretPositionChanged(event: InputMethodEvent?) {
          }

          override fun inputMethodTextChanged(event: InputMethodEvent?) {
            listener.stateChanged(validate() == null)
          }
        })
        template!!.addItemListener { listener.stateChanged(validate() == null) }
      }

      override fun validate(): ValidationInfo? {
        val validate = super.validate()
        if (validate == null) {
          if (template!!.editor.item.toString().isBlank()) {
            return ValidationInfo("Please enter project template", template!!)
          }
        }
        return validate
      }

      override fun buildUI(settingsStep: SettingsStep) {
        super.buildUI(settingsStep)
        settingsStep.addSettingsField("Project template", template!!)
      }

      override fun getSettings(): NpmPackageProjectGenerator.Settings {
        val settings = super.getSettings()
        val text = template!!.selectedItem as? String
        if (text != null) {
          settings.putUserData<String>(TEMPLATE_KEY, text)
        }
        return settings
      }
    }
  }

  override fun generateProject(project: Project, baseDir: VirtualFile, settings: NpmPackageProjectGenerator.Settings, module: Module) {
    assert(false, { "Should not be called" })
  }

  override fun getName() = "Vue.js"
  override fun getDescription() = "vue-cli based project generator"
  override fun getIcon() = VuejsIcons.Vue
}

enum class VueProjectCreationState {
  Init, Process, User, Error, QuestionsFinished, Finished
}

class VueCliProjectSettingsStep(projectGenerator: DirectoryProjectGenerator<NpmPackageProjectGenerator.Settings>?,
                                callback: AbstractNewProjectStep.AbstractCallback<NpmPackageProjectGenerator.Settings>?)
  : ProjectSettingsStepBase<NpmPackageProjectGenerator.Settings>(projectGenerator, callback) {
  // checked in disposed condition
  @Volatile private var state: VueProjectCreationState = VueProjectCreationState.Init
  private var currentQuestion: VueCreateProjectProcess.Question? = null
  private var process: VueCreateProjectProcess? = null
  private var questionUi: QuestioningPanel? = null

  private fun initQuestioning(mainPanel: JPanel, location: Path) {
    val settings = peer.settings
    val templateName = settings.getUserData(VueCliProjectGenerator.TEMPLATE_KEY)!!

    questionUi = QuestioningPanel(replacePanel(mainPanel), templateName, {
      if (state == VueProjectCreationState.User) myCreateButton.isEnabled = it
    })

    process = VueCreateProjectProcess(location.parent, location.fileName.toString(), templateName, settings.myInterpreterRef,
                                      settings.myPackagePath, this)
    process!!.listener = {
      ApplicationManager.getApplication().invokeLater(
        Runnable {
          if (state != VueProjectCreationState.Process) return@Runnable

          val processState = process!!.getState()
          if (VueCreateProjectProcess.ProcessState.Error == processState.processState) {
            questionUi!!.error()
            onError(processState.globalProblem ?: "")
          } else if (VueCreateProjectProcess.ProcessState.QuestionsFinished == processState.processState) {
            state = VueProjectCreationState.QuestionsFinished
            DialogWrapper.findInstance(myCreateButton)?.close(DialogWrapper.OK_EXIT_CODE)
            val function = Runnable {
              val projectVFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(location.toString())
              if (projectVFolder == null) {
                VueCreateProjectProcess.LOG.info(String.format("Create Vue Project: can not find project directory in '%s'", location.toString()))
              } else {
                RecentProjectsManager.getInstance().lastProjectCreationLocation =
                  PathUtil.toSystemIndependentName(location.parent.normalize().toString())
                UsageTrigger.trigger("AbstractNewProjectStep." + projectGenerator.name)
                PlatformProjectOpenProcessor.doOpenProject(projectVFolder, null, -1, {
                  project, _ ->
                  val doneCallback = PackageJsonDependenciesExternalUpdateManager.getInstance(project).externalUpdateStarted(null, null)
                  createListeningProgress(project, doneCallback)
                }, EnumSet.noneOf(PlatformProjectOpenProcessor.Option::class.java))
              }
            }
            ApplicationManager.getApplication().invokeLater(function, ModalityState.NON_MODAL)
          } else if (VueCreateProjectProcess.ProcessState.Working == processState.processState) {
            currentQuestion = processState.question
            val error = processState.question?.validationError
            if (error != null) {
              setErrorText(error)
              questionUi!!.activateUi()
            } else {
              setErrorText("")
              questionUi!!.question(processState.question!!)
            }
            state = VueProjectCreationState.User
            myCreateButton.isEnabled = true
          }
        }, ModalityState.any(), Condition<Boolean> { state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error })
    }
  }

  override fun checkValid(): Boolean {
    val text = myLocationField.textField.text.trim()
    if (Files.exists(Paths.get(text))) {
      setErrorText("Project directory already exists. Please select other directory.")
      return false
    }
    return super.checkValid()
  }

  // this is separate since we mostly check project parent directory *creation* here - can not be done on typing
  private fun validateProjectLocation(projectLocation: String): Path? {
    var location: Path? = null
    var error: String? = null
    try {
      location = Paths.get(projectLocation).normalize()
      val parentFolder = location.parent
      if (!parentFolder.exists() && !FileUtil.createDirectory(parentFolder.toFile())) {
        error = "Can not create project directory: %s"
      }
    } catch (e: InvalidPathException) {
      error = "Invalid project path: %s"
    }
    if (error != null) {
      setErrorText(String.format(error, projectLocation))
      myCreateButton.text = "Close"
      state = VueProjectCreationState.Error
      myCreateButton.isEnabled = true
      return null
    }
    return location
  }

  private fun onError(errorText: String) {
    setErrorText("Error: " + errorText)
    myCreateButton.text = "Close"
    state = VueProjectCreationState.Error
    myCreateButton.isEnabled = true
    process!!.listener = null
    process!!.cancel()
  }

  internal fun createListeningProgress(project: Project, doneCallback: Runnable) {
    if (process == null) return
    val task = object: Task.Backgroundable(project, "Generating Vue project...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      override fun run(indicator: ProgressIndicator) {
        process!!.waitForProcessTermination(indicator)
      }

      override fun onFinished() {
        doneCallback.run()
        JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
      }
    }
    val indicator = BackgroundableProcessIndicator(task)
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
  }

  override fun createPanel(): JPanel {
    val mainPanel = super.createPanel()
    mainPanel.add(WebProjectTemplate.createTitlePanel(), BorderLayout.NORTH)

    myCreateButton.text = "Next"
    removeActionListeners()
    myCreateButton.addActionListener {
      if (state == VueProjectCreationState.Init) {
        val location = validateProjectLocation(projectLocation)
        if (location == null) {
          UIUtil.setEnabled((mainPanel.layout as BorderLayout).getLayoutComponent(BorderLayout.CENTER), false, true)
        } else {
          initQuestioning(mainPanel, location)
          state = VueProjectCreationState.Process
          myCreateButton.isEnabled = false
        }
      }
      else if (state == VueProjectCreationState.Process) {
        assert(false, { "should be waiting for process" })
      }
      else if (state == VueProjectCreationState.User) {
        assert(currentQuestion != null)
        setErrorText("")
        val answer = questionUi!!.getAnswer()!!
        process!!.answer(answer)
        state = VueProjectCreationState.Process
        questionUi!!.waitForNextQuestion()
        myCreateButton.isEnabled = false
      }
      else if (state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error) {
        DialogWrapper.findInstance(myCreateButton)?.close(DialogWrapper.OK_EXIT_CODE)
      }
      else assert(false, { "Unknown state" })
    }

    return mainPanel
  }

  private fun replacePanel(mainPanel: JPanel): JPanel {
    val newMainPanel = JPanel(BorderLayout())
    val wrapper = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    val progressLabel = JBLabel("Starting generation service...")
    progressLabel.font = UIUtil.getLabelFont()
    RelativeFont.ITALIC.install<JLabel>(progressLabel)
    wrapper.add(progressLabel)
    wrapper.add(AsyncProcessIcon(""))
    newMainPanel.add(wrapper, BorderLayout.NORTH)
    val scrollPane = (mainPanel.layout as BorderLayout).getLayoutComponent(BorderLayout.CENTER) as JBScrollPane
    scrollPane.setViewportView(newMainPanel)
    mainPanel.revalidate()
    mainPanel.repaint()
    return newMainPanel
  }

  private fun removeActionListeners() {
    val actionListeners = myCreateButton.actionListeners
    actionListeners.forEach { myCreateButton.removeActionListener(it) }
  }
}

class QuestioningPanel(private val panel: JPanel, private val generatorName: String, private val validationListener: (Boolean) -> Unit) {
  private var currentControl: (() -> String)? = null

  private fun addInput(message: String, defaultValue: String): () -> String {
    val formBuilder = questionHeader(message)
    val field = JBTextField(defaultValue)
    field.addKeyListener(object: KeyAdapter() {
      override fun keyReleased(e: KeyEvent?) {
        validationListener.invoke(field.text.isNotBlank())
      }
    })
    field.addActionListener(object: ActionListener {
      override fun actionPerformed(e: ActionEvent?) {
        validationListener.invoke(field.text.isNotBlank())
      }
    })
    formBuilder.addComponent(field)
    panel.add(SwingHelper.wrapWithHorizontalStretch(formBuilder.panel), BorderLayout.CENTER)
    return { field.text }
  }

  private fun questionHeader(message: String): FormBuilder {
    panel.removeAll()
    val formBuilder = FormBuilder.createFormBuilder()
    val titleLabel = JLabel(String.format("Running vue-init with %s template", generatorName))
    titleLabel.font = UIUtil.getLabelFont()
    RelativeFont.ITALIC.install<JLabel>(titleLabel)
    formBuilder.addComponent(titleLabel)
    formBuilder.addVerticalGap(5)
    val label = JBLabel(message)
    label.ui = MultiLineLabelUI()
    formBuilder.addComponent(label)
    return formBuilder
  }

  // todo implementation can be further zipped
  private fun addChoices(message: String, choices: List<VueCreateProjectProcess.Choice>): () -> String {
    val formBuilder = questionHeader(message)
    val box = ComboBox<VueCreateProjectProcess.Choice>(choices.toTypedArray())
    box.renderer = object: ListCellRendererWrapper<VueCreateProjectProcess.Choice?>() {
      override fun customize(list: JList<*>?, value: VueCreateProjectProcess.Choice?, index: Int, selected: Boolean, hasFocus: Boolean) {
        if (value != null) {
          setText(value.name)
        }
      }
    }
    box.isEditable = false
    formBuilder.addComponent(box)
    panel.add(SwingHelper.wrapWithHorizontalStretch(formBuilder.panel), BorderLayout.CENTER)
    return { (box.selectedItem as? VueCreateProjectProcess.Choice)?.value ?: "" }
  }

  private fun addConfirm(message: String): () -> String {
    val formBuilder = questionHeader(message)

    val yesBtn = JBRadioButton("yes")
    val noBtn = JBRadioButton("no")
    val buttonGroup = ButtonGroup()
    buttonGroup.add(yesBtn)
    buttonGroup.add(noBtn)
    yesBtn.isSelected = true
    noBtn.isSelected = false

    formBuilder.addComponent(yesBtn)
    formBuilder.addComponent(noBtn)
    panel.add(SwingHelper.wrapWithHorizontalStretch(formBuilder.panel), BorderLayout.CENTER)
    return { if (yesBtn.isSelected) "Yes" else "no" }
  }

  fun error() {
    panel.removeAll()
    panel.add(SwingHelper.wrapWithHorizontalStretch(JBLabel("Generation service error")), BorderLayout.CENTER)
    panel.revalidate()
    panel.repaint()
  }

  fun question(question: VueCreateProjectProcess.Question) {
    if (question.type == VueCreateProjectProcess.QuestionType.Input) {
      currentControl = addInput(question.message, question.defaultVal)
    } else if (question.type == VueCreateProjectProcess.QuestionType.Confirm) {
      currentControl = addConfirm(question.message)
    } else if (question.type == VueCreateProjectProcess.QuestionType.List) {
      currentControl = addChoices(question.message, question.choices)
    }
    panel.revalidate()
    panel.repaint()
  }

  fun getAnswer(): String? {
    return currentControl?.invoke()
  }

  fun activateUi() {
    UIUtil.setEnabled(panel, true, true)
  }

  fun waitForNextQuestion() {
    UIUtil.setEnabled(panel, false, true)
  }
}