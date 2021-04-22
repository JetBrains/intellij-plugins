// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.uml;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.diagram.*;
import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.diagram.util.DiagramUtils;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.uml.FlashUmlDataModel;
import com.intellij.lang.javascript.uml.FlashUmlDependenciesSettingsOption;
import com.intellij.lang.javascript.uml.FlashUmlProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.SkipInHeadlessEnvironment;
import com.intellij.uml.UmlGraphBuilderFactory;
import com.intellij.uml.core.actions.ShowDiagramBase;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.testFramework.assertions.Assertions.assertThat;

@SkipInHeadlessEnvironment
public class FlashUmlTest extends JavaCodeInsightTestCase {

  private static final String BASE_PATH = "uml/";

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  public Object getData(@NotNull String dataId) {
    if (CommonDataKeys.PSI_ELEMENT.is(dataId) ||
        InjectedDataKeys.PSI_ELEMENT.is(dataId)) {
      return TargetElementUtil.findTargetElement(getEditor(), TargetElementUtil.getInstance().getReferenceSearchFlags());
    }
    if (CommonDataKeys.PSI_FILE.is(dataId)) {
      return getFile();
    }
    return super.getData(dataId);
  }

  private void doTest(String file) throws Exception {
    doTest(new String[]{file}, ArrayUtilRt.EMPTY_STRING_ARRAY, () -> GlobalSearchScope.allScope(myProject), null, null);
  }


  private void doTest(String[] files,
                      String[] additionalClasses,
                      Computable<GlobalSearchScope> scopeProvider,
                      @Nullable EnumSet<FlashUmlDependenciesSettingsOption> dependencies,
                      @Nullable String expectedFileNamePrefix) throws Exception {
    doTestImpl(null, files, additionalClasses, scopeProvider, dependencies, expectedFileNamePrefix);
  }

  private DiagramBuilder doTestImpl(@Nullable File projectRoot, String[] files,
                                    String[] additionalClasses,
                                    Computable<GlobalSearchScope> scopeProvider,
                                    @Nullable EnumSet<FlashUmlDependenciesSettingsOption> dependencies,
                                    @Nullable String expectedFileNamePrefix) throws Exception {
    List<VirtualFile> vFiles = new ArrayList<>(files.length);
    for (String file : files) {
      vFiles.add(findVirtualFile(BASE_PATH + file));
    }
    ApplicationManager.getApplication().runWriteAction(() -> {
      final ModuleRootManager rootManager = ModuleRootManager.getInstance(myModule);
      final ModifiableRootModel rootModel = rootManager.getModifiableModel();
      ContentEntry[] contentEntries = rootModel.getContentEntries();
      for (ContentEntry contentEntry : contentEntries) {
        rootModel.removeContentEntry(contentEntry);
      }
      rootModel.commit();
    });
    configureByFiles(projectRoot, VfsUtilCore.toVirtualFileArray(vFiles));

    final LinkedHashMap<Integer, String> markers = JSTestUtils.extractPositionMarkers(getProject(), getEditor().getDocument());
    assertFalse(markers.isEmpty());
    DiagramBuilder builder = null;
    for (Map.Entry<Integer, String> marker : markers.entrySet()) {
      getEditor().getCaretModel().moveToOffset(marker.getKey());
      String expectedPrefix = StringUtil.isNotEmpty(marker.getValue()) ? marker.getValue() : expectedFileNamePrefix;

      DataContext dataContext = DataManager.getInstance().getDataContext(null);
      AnActionEvent event = AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataContext);
      List<DiagramProvider<?>> providers = ShowDiagramBase.findProviders(event).collect(Collectors.toList());

      FlashUmlProvider provider = ContainerUtil.findInstance(providers, FlashUmlProvider.class);
      assertNotNull("Flash UML provider not found", provider);

      String actualOriginFqn = provider.getVfsResolver().getQualifiedName(
        provider.getElementManager().findInDataContext(event.getDataContext()));

      Object actualOrigin = provider.getVfsResolver().resolveElementByFQN(actualOriginFqn, getProject());
      builder = UmlGraphBuilderFactory.create(myProject, provider, actualOrigin, null);
      Disposer.register(getTestRootDisposable(), builder);
      DiagramDataModel<?> model = builder.getDataModel();
      DiagramConfiguration configuration = DiagramConfiguration.getInstance();
      String originalCategories = configuration.categories.get(provider.getID());
      if (dependencies != null) {
        model.setShowDependencies(true);
        EnumSet<FlashUmlDependenciesSettingsOption> disabledOptions = EnumSet.complementOf(dependencies);
        configuration.categories
          .put(provider.getID(), StringUtil.join(disabledOptions, option -> option.getDisplayName(), ";"));
      }
      else {
        model.setShowDependencies(false);
      }

      try {
        model.refreshDataModel();

        // first limit elements by scope
        Collection<DiagramNode<?>> nodesToRemove = new ArrayList<>();
        for (DiagramNode<?> node : model.getNodes()) {
          if (node.getIdentifyingElement() instanceof JSClass &&
              !scopeProvider.compute().contains(((JSClass)node.getIdentifyingElement()).getContainingFile().getVirtualFile())) {
            nodesToRemove.add(node);
          }
        }

        for (DiagramNode node : nodesToRemove) {
          model.removeNode(node);
        }
        builder.updateGraph();

        // then add explicitly required classes
        for (String aClass : additionalClasses) {
          JSClass c = JSTestUtils.findClassByQName(aClass, GlobalSearchScope.allScope(myProject));
          @SuppressWarnings("unchecked")
          DiagramNode<?> node = ((DiagramDataModel<Object>)model).addElement(c);
          if (node != null) {
            builder.createDraggedNode(node, node.getTooltip(), DiagramUtils.getBestPositionForNode(builder));
            builder.updateGraph();
          }
        }

        assertModel(expectedPrefix, provider, actualOriginFqn, model);
      }
      finally {
        configuration.categories.put(provider.getID(), originalCategories);
      }
    }
    return builder;
  }

  private void assertModel(String expectedPrefix,
                           DiagramProvider<Object> provider,
                           String actualOriginFqn,
                           DiagramDataModel<?> model) throws Exception {
    String expectedDataFileName =
      getTestName(false) + (StringUtil.isEmpty(expectedPrefix) ? ".expected.xml" : ".expected." + expectedPrefix + ".xml");
    CharSequence expectedText = LoadTextUtil.loadText(findVirtualFile(BASE_PATH + expectedDataFileName));
    final Element expected = JDOMUtil.load(expectedText);
    final String expectedOriginFqn = expected.getAttributeValue("origin");
    assertEquals(expectedDataFileName + ": Invalid origin element", expectedOriginFqn, actualOriginFqn);
    JDOMResult actual = new JDOMResult();
    UmlDataModelDumper.dump(actual, provider, model);
    actual.getDocument().getRootElement().setAttribute("origin", actualOriginFqn);

    assertThat(actual.getDocument().getRootElement()).isEqualTo(expected);
  }

  public void testClasses() throws Exception {
    doTest(getTestName(false) + ".as");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testMxmlClass() throws Exception {
    doTest(getTestName(false) + ".mxml");
  }

  public void testPackage() throws Exception {
    doTest(getTestName(false) + ".as");
  }

  public void testAsDependencies() throws Exception {
    String testName = getTestName(false);
    String filename = testName + ".as";
    doTest(new String[]{filename, testName + "_2.as", testName + "_3.as"},
           new String[]{"Foo", "Bar", "Zz", "Zz2", "Pp", "Oo", "Abc", "Def", "Rt", "UI", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8",
             "A9", "A10", "W", filename + ":Inner1", filename + ":Inner2"}, allScopeProvider(),
           EnumSet.allOf(FlashUmlDependenciesSettingsOption.class), null);
  }

  private Computable<GlobalSearchScope> projectScopeProvider() {
    return () -> GlobalSearchScope.projectScope(myProject);
  }

  private Computable<GlobalSearchScope> allScopeProvider() {
    return () -> GlobalSearchScope.allScope(myProject);
  }

  private Computable<GlobalSearchScope> moduleScopeProvider() {
    return () -> GlobalSearchScope.moduleScope(myModule);
  }

  public void testMxmlDependencies() throws Exception {
    initSdk();
    String testName = getTestName(false);
    doTest(new String[]{testName + ".mxml", testName + "_2.as", testName + "_3.mxml", testName + "_4.mxml"},
           new String[]{"Foo", "Bar", "Hello", "spark.components.Button", testName + "_3", testName + "_4", "com.foo.MyRenderer", "MySkin",
             "com.bar.A"},
           projectScopeProvider(), EnumSet.allOf(FlashUmlDependenciesSettingsOption.class), null);
  }

  private void initSdk() {
    final Sdk sdk45 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk45);
    });
  }

  public void testDependenciesSettings() throws Exception {
    String testName = getTestName(false);
    String filename = testName + ".as";
    String[] files = {testName + ".as"};
    String[] classes = {filename + ":C1", filename + ":C2", filename + ":C3", filename + ":C4", filename + ":C5"};

    doTest(files, classes, projectScopeProvider(), EnumSet.of(FlashUmlDependenciesSettingsOption.ONE_TO_ONE), "OneToOne");
    doTest(files, classes, projectScopeProvider(), EnumSet.of(FlashUmlDependenciesSettingsOption.ONE_TO_MANY), "OneToMany");
    doTest(files, classes, projectScopeProvider(), EnumSet.of(FlashUmlDependenciesSettingsOption.CREATE), "Create");
    doTest(files, classes, projectScopeProvider(), EnumSet.of(FlashUmlDependenciesSettingsOption.USAGES), "Usages");
    doTest(files, classes, projectScopeProvider(), EnumSet.of(FlashUmlDependenciesSettingsOption.SELF), "Self");
    doTest(files, classes, projectScopeProvider(),
           EnumSet.of(FlashUmlDependenciesSettingsOption.SELF, FlashUmlDependenciesSettingsOption.ONE_TO_ONE), "SelfOneToOne");
    doTest(files, classes, projectScopeProvider(),
           EnumSet.of(FlashUmlDependenciesSettingsOption.SELF, FlashUmlDependenciesSettingsOption.ONE_TO_MANY), "SelfOneToMany");
    doTest(files, classes, projectScopeProvider(),
           EnumSet.of(FlashUmlDependenciesSettingsOption.SELF, FlashUmlDependenciesSettingsOption.USAGES), "SelfUsages");
    doTest(files, classes, projectScopeProvider(),
           EnumSet.of(FlashUmlDependenciesSettingsOption.SELF, FlashUmlDependenciesSettingsOption.CREATE), "SelfCreate");
  }

  public void testVector() throws Exception {
    initSdk();
    String fileName = getTestName(false) + ".as";
    doTest(new String[]{fileName},
           new String[]{"Vector", fileName + ":Foo", fileName + ":Bar"},
           allScopeProvider(), EnumSet.allOf(FlashUmlDependenciesSettingsOption.class), null);
  }

  public void testExpandCollapse() throws Exception {
    File projectRoot = new File(findVirtualFile(BASE_PATH + getTestName(false)).getPath());
    String[] files = {getTestName(false) + "/Classes.as",
      getTestName(false) + "/com/test/MyButton.mxml",
      getTestName(false) + "/com/test/MyButton2.mxml"};

    DiagramBuilder builder = doTestImpl(projectRoot, files,
                                        new String[]{"com.test.Bar", "Root", "com.test.MyButton"},
                                        moduleScopeProvider(), EnumSet.allOf(FlashUmlDependenciesSettingsOption.class),
                                        null);
    String originQName = "com.test.Foo";
    DiagramProvider<Object> provider = DiagramProvider.findByID(FlashUmlProvider.ID);
    FlashUmlDataModel model = (FlashUmlDataModel)builder.getDataModel();

    collapseNode(model, JSTestUtils.findClassByQName("com.test.Bar", myModule.getModuleScope()));
    assertModel("2", provider, originQName, model);

    expandNode(model, "com.test");
    assertModel("3", provider, originQName, model);

    collapseNode(model, JSTestUtils.findClassByQName("Root", myModule.getModuleScope()));
    assertModel("3", provider, originQName, model);
  }

  public void testExpandCollapse2() throws Exception {
    File projectRoot = new File(findVirtualFile(BASE_PATH + getTestName(false)).getPath());
    String[] files = {getTestName(false) + "/com/test/MyButton.mxml"};

    DiagramBuilder builder = doTestImpl(projectRoot, files,
                                        ArrayUtilRt.EMPTY_STRING_ARRAY,
                                        moduleScopeProvider(), EnumSet.allOf(FlashUmlDependenciesSettingsOption.class),
                                        null);
    String originQName = "com.test.MyButton";
    DiagramProvider<Object> provider = DiagramProvider.findByID(FlashUmlProvider.ID);
    FlashUmlDataModel model = (FlashUmlDataModel)builder.getDataModel();

    collapseNode(model, JSTestUtils.findClassByQName("com.test.MyButton", myModule.getModuleScope()));
    assertModel("2", provider, originQName, model);
  }

  private static void collapseNode(final FlashUmlDataModel model, final Object element) {
    model.collapseNode(model.findNode(element));
    model.refreshDataModel();
  }

  private static void expandNode(final FlashUmlDataModel model, final Object element) {
    model.expandNode(model.findNode(element));
    model.refreshDataModel();
  }
}
