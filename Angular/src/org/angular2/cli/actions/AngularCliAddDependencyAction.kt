// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.actions

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.execution.ExecutionException
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.CompletionModuleInfo
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.gist.GistManager
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.angular2.cli.AngularCliFilter
import org.angular2.cli.AngularCliProjectGenerator
import org.angular2.cli.AngularCliSchematicsRegistryService
import org.angular2.cli.AngularCliUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.io.IOException
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class AngularCliAddDependencyAction : DumbAwareAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    if (project == null || file == null) {
      return
    }
    val cli = AngularCliUtil.findAngularCliFolder(project, file)
    val packageJson = PackageJsonUtil.findChildPackageJsonFile(cli)
    if (cli == null || packageJson == null) {
      return
    }
    if (!AngularCliUtil.hasAngularCLIPackageInstalled(cli)) {
      AngularCliUtil.notifyAngularCliNotInstalled(
        project, cli, Angular2Bundle.message("angular.action.ng-add.cant-add-new-dependency"))
      return
    }

    val existingPackages = PackageJsonData.getOrCreate(packageJson).allDependencies

    val model = SortedListModel(
      Comparator.comparing { p: NodePackageBasicInfo -> if (p === OTHER) 1 else 0 }
        .thenComparing<String> { it.name }
    )
    val list = JBList(model)
    list.setCellRenderer(object : ColoredListCellRenderer<NodePackageBasicInfo>() {
      override fun customizeCellRenderer(
        list: JList<out NodePackageBasicInfo>,
        value: NodePackageBasicInfo,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean,
      ) {
        if (!selected && index % 2 == 0) {
          background = UIUtil.getDecoratedRowColor()
        }
        setIcon(JBUIScale.scaleIcon(EmptyIcon.create(5)))
        append(value.name, if (value !== OTHER) SimpleTextAttributes.REGULAR_ATTRIBUTES else SimpleTextAttributes.LINK_ATTRIBUTES, true)
        if (value.description != null) {
          append(" - " + value.description!!, SimpleTextAttributes.GRAY_ATTRIBUTES, false)
        }
      }
    })

    val scroll = ScrollPaneFactory.createScrollPane(list)
    scroll.border = JBUI.Borders.empty()
    val pane = ListWithFilter.wrap(list, scroll) { it.name }

    @Suppress("DialogTitleCapitalization")
    val builder = JBPopupFactory
      .getInstance()
      .createComponentPopupBuilder(pane, list)
      .setMayBeParent(true)
      .setRequestFocus(true)
      .setFocusable(true)
      .setFocusOwners(arrayOf<Component>(list))
      .setLocateWithinScreenBounds(true)
      .setCancelOnOtherWindowOpen(true)
      .setMovable(true)
      .setResizable(true)
      .setCancelOnWindowDeactivation(false)
      .setTitle(Angular2Bundle.message("angular.action.ng-add.title"))
      .setCancelOnClickOutside(true)
      .setDimensionServiceKey(project, "org.angular.cli.generate", true)
      .setMinSize(Dimension(JBUIScale.scale(350), JBUIScale.scale(300)))
      .setCancelButton(IconButton(Angular2Bundle.message("angular.action.ng-add.button-close"),
                                  AllIcons.Actions.Close, AllIcons.Actions.CloseHovered))

    val popup = builder.createPopup()

    val action = { pkgInfo: NodePackageBasicInfo ->
      popup.closeOk(null)
      if (pkgInfo == OTHER) {
        chooseCustomPackageAndInstall(project, cli, existingPackages)
      }
      else {
        runAndShowConsole(project, cli, pkgInfo.name, false)
      }
    }
    list.addKeyListener(object : KeyAdapter() {
      override fun keyPressed(e: KeyEvent) {
        if (list.selectedValue == null) return
        if (e.keyCode == KeyEvent.VK_ENTER) {
          e.consume()
          action(list.selectedValue)
        }
      }
    })
    object : DoubleClickListener() {
      public override fun onDoubleClick(event: MouseEvent): Boolean {
        if (list.selectedValue == null) return true
        action(list.selectedValue)
        return true
      }
    }.installOn(list)
    popup.showCenteredInCurrentWindow(project)
    updateListAsync(list, model, popup, existingPackages)
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    e.presentation.isEnabledAndVisible = (project != null
                                          && file != null
                                          && AngularCliUtil.findAngularCliFolder(project, file) != null)
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  private class SelectCustomPackageDialog(
    private val myProject: Project,
    private val myExistingPackages: Set<String>,
  ) : DialogWrapper(myProject) {
    private var myTextEditor: EditorTextField? = null

    val `package`: String
      get() = myTextEditor!!.text

    init {
      @Suppress("DialogTitleCapitalization")
      title = Angular2Bundle.message("angular.action.ng-add.title")
      init()
      okAction.putValue(Action.NAME, Angular2Bundle.message("angular.action.ng-add.button-install"))
    }

    override fun getPreferredFocusedComponent(): JComponent? {
      return myTextEditor
    }

    override fun createCenterPanel(): JComponent {
      val panel = JPanel(BorderLayout(0, 4))
      myTextEditor = TextFieldWithAutoCompletion(
        myProject, NodePackagesCompletionProvider(myExistingPackages), false, null)
      myTextEditor!!.setPreferredWidth(250)
      panel.add(LabeledComponent.create(myTextEditor!!, Angular2Bundle.message("angular.action.ng-add.package-name"), BorderLayout.NORTH))
      return panel
    }
  }

  private class NodePackagesCompletionProvider(private val myExistingPackages: Set<String>) : TextFieldWithAutoCompletionListProvider<NodePackageBasicInfo>(
    emptyList()) {

    override fun getLookupString(item: NodePackageBasicInfo): String {
      return item.name
    }

    override fun acceptChar(c: Char): CharFilter.Result? {
      return if (c == '@' || c == '/') CharFilter.Result.ADD_TO_PREFIX else null
    }

    override fun applyPrefixMatcher(result: CompletionResultSet, prefix: String): CompletionResultSet {
      val res = super.applyPrefixMatcher(result, prefix)
      res.restartCompletionOnAnyPrefixChange()
      return res
    }

    override fun getItems(prefix: String, cached: Boolean, parameters: CompletionParameters): Collection<NodePackageBasicInfo> {
      if (cached) {
        return emptyList()
      }
      val result = ArrayList<NodePackageBasicInfo>()
      try {
        NpmRegistryService.instance.findPackages(
          ProgressManager.getInstance().progressIndicator,
          NpmRegistryService.namePrefixSearch(prefix), 20, { true },
          { pkg ->
            if (!myExistingPackages.contains(pkg.name)) {
              result.add(pkg)
            }
          })
      }
      catch (e: IOException) {
        LOG.info(e)
      }

      return result
    }

    override fun createLookupBuilder(item: NodePackageBasicInfo): LookupElementBuilder {
      return super.createLookupBuilder(item)
        .withTailText(if (item.description != null) "  " + item.description!! else null, true)
    }
  }

  companion object {

    private val OTHER = NodePackageBasicInfo(Angular2Bundle.message("angular.action.ng-add.install-other"), null)

    @NonNls
    private val LOG = Logger.getInstance(AngularCliAddDependencyAction::class.java)
    private const val TIMEOUT: Long = 2000

    @NonNls
    private val LATEST = "latest"

    @JvmStatic
    fun runAndShowConsoleLater(
      project: Project, cli: VirtualFile, packageName: String,
      packageVersion: String?, proposeLatestVersionIfNeeded: Boolean,
    ) {
      ApplicationManager.getApplication().executeOnPooledThread {
        if (project.isDisposed) {
          return@executeOnPooledThread
        }
        val version = Ref(StringUtil.defaultIfEmpty(packageVersion, LATEST))
        val proposeLatestVersion = proposeLatestVersionIfNeeded && !AngularCliSchematicsRegistryService.instance.supportsNgAdd(packageName,
                                                                                                                               version.get(),
                                                                                                                               TIMEOUT)
        ApplicationManager.getApplication().invokeLater(
          {
            if (proposeLatestVersion) {
              @Suppress("DialogTitleCapitalization")
              when (Messages.showDialog(
                project,
                Angular2Bundle.message("angular.action.ng-add.not-supported-specified-try-latest"),
                Angular2Bundle.message("angular.action.ng-add.title"),
                arrayOf(Angular2Bundle.message("angular.action.ng-add.install-latest"),
                        Angular2Bundle.message("angular.action.ng-add.install-current"), Messages.getCancelButton()), 0,
                Messages.getQuestionIcon())) {
                0 -> version.set(LATEST)
                1 -> version.set(packageVersion)
                else -> return@invokeLater
              }
            }
            runAndShowConsole(project, cli, packageName + "@" + version.get(), !proposeLatestVersion)
          }, project.disposed)
      }
    }

    private fun runAndShowConsole(
      project: Project, cli: VirtualFile,
      packageSpec: String, proposeLatestVersionIfNeeded: Boolean,
    ) {
      if (project.isDisposed) {
        return
      }
      val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return
      try {
        val modules = ArrayList<CompletionModuleInfo>()
        NodeModuleSearchUtil.findModulesWithName(modules, ANGULAR_CLI_PACKAGE, cli, null)
        if (modules.isEmpty() || modules[0].virtualFile == null) {
          throw ExecutionException(Angular2Bundle.message("angular.action.ng-add.pacakge-not-installed"))
        }
        val module = modules[0]
        ApplicationManager.getApplication().executeOnPooledThread {
          val handler = NpmPackageProjectGenerator.generate(
            interpreter, NodePackage(module.virtualFile!!.path),
            { pkg -> pkg.findBinFilePath(AngularCliProjectGenerator.NG_EXECUTABLE)!!.toString() },
            cli, VfsUtilCore.virtualToIoFile(cli),
            project, { GistManager.getInstance().invalidateData() },
            Angular2Bundle.message("angular.action.ng-add.installing-for", packageSpec, cli.name),
            arrayOf<Filter>(AngularCliFilter(project, cli.path)),
            "add", packageSpec)
          if (proposeLatestVersionIfNeeded) {
            handler.addProcessListener(object : ProcessAdapter() {
              override fun processTerminated(event: ProcessEvent) {
                if (event.exitCode != 0) {
                  installLatestIfFeasible(project, cli, packageSpec)
                }
              }
            })
          }
        }
      }
      catch (e: Exception) {
        LOG.error("Failed to execute `ng add`: " + e.message, e)
      }

    }

    private fun installLatestIfFeasible(
      project: Project, cli: VirtualFile,
      packageSpec: String,
    ) {
      if (project.isDisposed) {
        return
      }
      val packageJson = PackageJsonUtil.findChildPackageJsonFile(cli) ?: return
      val finder = NodeInstalledPackageFinder(project, packageJson)
      val index = packageSpec.lastIndexOf('@')
      val packageName = if (index <= 0) packageSpec else packageSpec.substring(0, index)
      val pkg = finder.findInstalledPackage(packageName) ?: return
      if (!AngularCliSchematicsRegistryService.instance.supportsNgAdd(pkg)) {
        ApplicationManager.getApplication().invokeLater(
          {
            @Suppress("DialogTitleCapitalization")
            if (Messages.OK == Messages.showDialog(
                project,
                Angular2Bundle.message("angular.action.ng-add.not-supported-installed-try-latest"),
                Angular2Bundle.message("angular.action.ng-add.title"),
                arrayOf(Angular2Bundle.message("angular.action.ng-add.install-latest"), Messages.getCancelButton()),
                0, Messages.getQuestionIcon())) {

              runAndShowConsole(project, cli, "$packageName@$LATEST", false)
            }
          }, project.disposed
        )
      }
    }

    private fun chooseCustomPackageAndInstall(
      project: Project,
      cli: VirtualFile,
      existingPackages: Set<String>,
    ) {
      val dialog = SelectCustomPackageDialog(project, existingPackages)
      if (dialog.showAndGet()) {
        runAndShowConsole(project, cli, dialog.`package`, false)
      }
    }

    private fun updateListAsync(
      list: JBList<NodePackageBasicInfo>,
      model: SortedListModel<NodePackageBasicInfo>,
      popup: JBPopup, existingPackages: Set<String>,
    ) {
      list.setPaintBusy(true)
      model.clear()
      ApplicationManager.getApplication().executeOnPooledThread {
        if (popup.isDisposed) {
          return@executeOnPooledThread
        }
        val packages = AngularCliSchematicsRegistryService
          .instance
          .getPackagesSupportingNgAdd(20000)
        ApplicationManager.getApplication().invokeLater(
          {
            packages.forEach { pkg ->
              if (!existingPackages.contains(pkg.name)) {
                model.add(pkg)
              }
            }
            model.add(OTHER)
            list.setPaintBusy(false)
          },
          { popup.isDisposed })
      }
    }
  }
}
