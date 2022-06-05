// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.uml;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.diagram.*;
import com.intellij.diagram.settings.DiagramConfiguration;
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
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.graph.services.GraphCanvasLocationService;
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
import com.intellij.uml.core.actions.ShowDiagramBase;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.*;
import org.jdom.input.sax.SAXHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.sax.SAXResult;
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
      builder = DiagramBuilderFactory.getInstance().create(myProject, provider, actualOrigin, null);
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
            builder.createDraggedNode(node, node.getTooltip(),
                                      GraphCanvasLocationService.getInstance().getBestPositionForNode(builder.getGraphBuilder()));
            builder.updateGraph();
          }
        }

        assertModel(expectedPrefix, provider, actualOriginFqn, model);
      }
      finally {
        if (originalCategories == null) {
          configuration.categories.remove(provider.getID());
        }
        else {
          configuration.categories.put(provider.getID(), originalCategories);
        }
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

final class JDOMResult extends SAXResult {

  /**
   * The result of a transformation, as set by Transformer
   * implementations that natively support JDOM, as a JDOM document
   * or a list of JDOM nodes.
   */
  private List<Content> resultlist = null;

  private Document resultdoc = null;

  /**
   * Whether the application queried the result (as a list or a
   * document) since it was last set.
   */
  private boolean queried = false;

  /**
   * The custom JDOM factory to use when building the transformation
   * result or <code>null</code> to use the default JDOM classes.
   */
  private JDOMFactory factory = null;

  /**
   * Public default constructor.
   */
  JDOMResult() {
    // Allocate custom builder object...
    DocumentBuilder builder = new DocumentBuilder();

    // And use it as ContentHandler and LexicalHandler.
    super.setHandler(builder);
    super.setLexicalHandler(builder);
  }

  /**
   * Sets the object(s) produced as result of an XSL Transformation.
   * <p>
   * <strong>Note</strong>: This method shall be used by the
   * {@link javax.xml.transform.Transformer} implementations that
   * natively support JDOM to directly set the transformation
   * result rather than considering this object as a
   * {@link SAXResult}.  Applications should <i>not</i> use this
   * method.</p>
   *
   * @param result the result of a transformation as a
   *               {@link java.util.List list} of JDOM nodes
   *               (Elements, Texts, Comments, PIs...).
   * @see #getResult
   */
  public void setResult(List<Content> result) {
    this.resultlist = result;
    this.queried = false;
  }

  /**
   * Returns the result of an XSL Transformation as a list of JDOM
   * nodes.
   * <p>
   * If the result of the transformation is a JDOM document,
   * this method converts it into a list of JDOM nodes; any
   * subsequent call to {@link #getDocument} will return
   * <code>null</code>.</p>
   *
   * @return the transformation result as a (possibly empty) list of
   * JDOM nodes (Elements, Texts, Comments, PIs...).
   */
  public List<Content> getResult() {
    List<Content> nodes = Collections.emptyList();

    // Retrieve result from the document builder if not set.
    this.retrieveResult();

    if (resultlist != null) {
      nodes = resultlist;
    }
    else {
      if (resultdoc != null && queried == false) {
        List<Content> content = resultdoc.getContent();
        nodes = new ArrayList<>(content.size());

        while (content.size() != 0) {
          Content o = content.remove(0);
          nodes.add(o);
        }
        resultlist = nodes;
        resultdoc = null;
      }
    }
    queried = true;

    return (nodes);
  }

  /**
   * Sets the document produced as result of an XSL Transformation.
   * <p>
   * <strong>Note</strong>: This method shall be used by the
   * {@link javax.xml.transform.Transformer} implementations that
   * natively support JDOM to directly set the transformation
   * result rather than considering this object as a
   * {@link SAXResult}.  Applications should <i>not</i> use this
   * method.</p>
   *
   * @param document the JDOM document result of a transformation.
   * @see #setResult
   * @see #getDocument
   */
  public void setDocument(Document document) {
    this.resultdoc = document;
    this.resultlist = null;
    this.queried = false;
  }

  /**
   * Returns the result of an XSL Transformation as a JDOM document.
   * <p>
   * If the result of the transformation is a list of nodes,
   * this method attempts to convert it into a JDOM document. If
   * successful, any subsequent call to {@link #getResult} will
   * return an empty list.</p>
   * <p>
   * <strong>Warning</strong>: The XSLT 1.0 specification states that
   * the output of an XSL transformation is not a well-formed XML
   * document but a list of nodes. Applications should thus use
   * {@link #getResult} instead of this method or at least expect
   * <code>null</code> documents to be returned.
   *
   * @return the transformation result as a JDOM document or
   * <code>null</code> if the result of the transformation
   * can not be converted into a well-formed document.
   * @see #getResult
   */
  Document getDocument() {
    Document doc = null;

    // Retrieve result from the document builder if not set.
    this.retrieveResult();

    if (resultdoc != null) {
      doc = resultdoc;
    }
    else {
      if (resultlist != null && (queried == false)) {
        // Try to create a document from the result nodes
        try {
          JDOMFactory f = this.getFactory();
          if (f == null) {
            f = new DefaultJDOMFactory();
          }

          doc = f.document(null);
          doc.setContent(resultlist);

          resultdoc = doc;
          resultlist = null;
        }
        catch (RuntimeException ex1) {
          // Some of the result nodes are not valid children of a
          // Document node. => return null.
          return null;
        }
      }
    }
    queried = true;

    return (doc);
  }

  /**
   * Sets a custom JDOMFactory to use when building the
   * transformation result. Use a custom factory to build the tree
   * with your own subclasses of the JDOM classes.
   *
   * @param factory the custom <code>JDOMFactory</code> to use or
   *                <code>null</code> to use the default JDOM
   *                classes.
   * @see #getFactory
   */
  private void setFactory(JDOMFactory factory) {
    this.factory = factory;
  }

  /**
   * Returns the custom JDOMFactory used to build the transformation
   * result.
   *
   * @return the custom <code>JDOMFactory</code> used to build the
   * transformation result or <code>null</code> if the
   * default JDOM classes are being used.
   * @see #setFactory
   */
  private JDOMFactory getFactory() {
    return this.factory;
  }

  /**
   * Checks whether a transformation result has been set and, if not,
   * retrieves the result tree being built by the document builder.
   */
  private void retrieveResult() {
    if (resultlist == null && resultdoc == null) {
      this.setResult(((DocumentBuilder)this.getHandler()).getResult());
    }
  }

  //-------------------------------------------------------------------------
  // SAXResult overwritten methods
  //-------------------------------------------------------------------------

  /**
   * Sets the target to be a SAX2 ContentHandler.
   *
   * @param handler Must be a non-null ContentHandler reference.
   */
  @Override
  public void setHandler(ContentHandler handler) {
    // Do Nothing
  }

  /**
   * Sets the SAX2 LexicalHandler for the output.
   * <p>
   * This is needed to handle XML comments and the like.  If the
   * lexical handler is not set, an attempt should be made by the
   * transformer to cast the ContentHandler to a LexicalHandler.</p>
   *
   * @param handler A non-null LexicalHandler for
   *                handling lexical parse events.
   */
  @Override
  public void setLexicalHandler(LexicalHandler handler) {
    // Ignore.
  }


  //=========================================================================
  // FragmentHandler nested class
  //=========================================================================

  private static class FragmentHandler extends SAXHandler {
    /**
     * A dummy root element required by SAXHandler that can only
     * cope with well-formed documents.
     */
    private final Element dummyRoot = new Element("root", null, null);

    /**
     * Public constructor.
     *
     * @param factory The Factory to use to create content instances
     */
    FragmentHandler(JDOMFactory factory) {
      super(factory);

      // Add a dummy root element to the being-built document as XSL
      // transformation can output node lists instead of well-formed
      // documents.
      this.pushElement(dummyRoot);
    }

    /**
     * Returns the result of an XSL Transformation.
     *
     * @return the transformation result as a (possibly empty) list of
     * JDOM nodes (Elements, Texts, Comments, PIs...).
     */
    public List<Content> getResult() {
      // Flush remaining text content in case the last text segment is
      // outside an element.
      try {
        this.flushCharacters();
      }
      catch (SAXException e) { /* Ignore... */ }
      return FragmentHandler.getDetachedContent(dummyRoot);
    }

    /**
     * Returns the content of a JDOM Element detached from it.
     *
     * @param elt the element to get the content from.
     * @return a (possibly empty) list of JDOM nodes, detached from
     * their parent.
     */
    private static List<Content> getDetachedContent(Element elt) {
      List<Content> content = elt.getContent();
      List<Content> nodes = new ArrayList<>(content.size());

      while (content.size() != 0) {
        Content o = content.remove(0);
        nodes.add(o);
      }
      return (nodes);
    }
  }

  //=========================================================================
  // DocumentBuilder inner class
  //=========================================================================

  private class DocumentBuilder extends XMLFilterImpl
    implements LexicalHandler {
    /**
     * The actual JDOM document builder.
     */
    private FragmentHandler saxHandler = null;

    /**
     * Whether the startDocument event was received. Some XSLT
     * processors such as Oracle's do not fire this event.
     */
    private boolean startDocumentReceived = false;

    /**
     * Public default constructor.
     */
    private DocumentBuilder() { }

    /**
     * Returns the result of an XSL Transformation.
     *
     * @return the transformation result as a (possibly empty) list of
     * JDOM nodes (Elements, Texts, Comments, PIs...) or
     * <code>null</code> if no new transformation occurred
     * since the result of the previous one was returned.
     */
    public List<Content> getResult() {
      List<Content> mresult = null;

      if (this.saxHandler != null) {
        // Retrieve result from SAX content handler.
        mresult = this.saxHandler.getResult();

        // Detach the (non-reusable) SAXHandler instance.
        this.saxHandler = null;

        // And get ready for the next transformation.
        this.startDocumentReceived = false;
      }
      return mresult;
    }

    private void ensureInitialization() throws SAXException {
      // Trigger document initialization if XSLT processor failed to
      // fire the startDocument event.
      if (!this.startDocumentReceived) {
        this.startDocument();
      }
    }

    //-----------------------------------------------------------------------
    // XMLFilterImpl overwritten methods
    //-----------------------------------------------------------------------

    /**
     * <i>[SAX ContentHandler interface support]</i> Processes a
     * start of document event.
     * <p>
     * This implementation creates a new JDOM document builder and
     * marks the current result as "under construction".</p>
     *
     * @throws SAXException if any error occurred while creating
     *                      the document builder.
     */
    @Override
    public void startDocument() throws SAXException {
      this.startDocumentReceived = true;

      // Reset any previously set result.
      setResult(null);

      // Create the actual JDOM document builder and register it as
      // ContentHandler on the superclass (XMLFilterImpl): this
      // implementation will take care of propagating the LexicalHandler
      // events.
      this.saxHandler = new FragmentHandler(getFactory());
      super.setContentHandler(this.saxHandler);

      // And propagate event.
      super.startDocument();
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of the beginning of an element.
     * <p>
     * This implementation ensures that startDocument() has been
     * called prior processing an element.
     *
     * @param nsURI     the Namespace URI, or the empty string if
     *                  the element has no Namespace URI or if
     *                  Namespace processing is not being performed.
     * @param localName the local name (without prefix), or the
     *                  empty string if Namespace processing is
     *                  not being performed.
     * @param qName     the qualified name (with prefix), or the
     *                  empty string if qualified names are not
     *                  available.
     * @param atts      The attributes attached to the element.  If
     *                  there are no attributes, it shall be an
     *                  empty Attributes object.
     * @throws SAXException if any error occurred while creating
     *                      the document builder.
     */
    @Override
    public void startElement(String nsURI, String localName, String qName,
                             Attributes atts) throws SAXException {
      this.ensureInitialization();
      super.startElement(nsURI, localName, qName, atts);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Begins the
     * scope of a prefix-URI Namespace mapping.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
      throws SAXException {
      this.ensureInitialization();
      super.startPrefixMapping(prefix, uri);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of character data.
     */
    @Override
    public void characters(char[] ch, int start, int length)
      throws SAXException {
      this.ensureInitialization();
      super.characters(ch, start, length);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of ignorable whitespace in element content.
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
      this.ensureInitialization();
      super.ignorableWhitespace(ch, start, length);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of a processing instruction.
     */
    @Override
    public void processingInstruction(String target, String data)
      throws SAXException {
      this.ensureInitialization();
      super.processingInstruction(target, data);
    }

    /**
     * <i>[SAX ContentHandler interface support]</i> Receives
     * notification of a skipped entity.
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
      this.ensureInitialization();
      super.skippedEntity(name);
    }

    //-----------------------------------------------------------------------
    // LexicalHandler interface support
    //-----------------------------------------------------------------------

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * start of DTD declarations, if any.
     *
     * @param name     the document type name.
     * @param publicId the declared public identifier for the
     *                 external DTD subset, or <code>null</code>
     *                 if none was declared.
     * @param systemId the declared system identifier for the
     *                 external DTD subset, or <code>null</code>
     *                 if none was declared.
     * @throws SAXException The application may raise an exception.
     */
    @Override
    public void startDTD(String name, String publicId, String systemId)
      throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startDTD(name, publicId, systemId);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of DTD declarations.
     *
     */
    @Override
    public void endDTD() {
      this.saxHandler.endDTD();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * beginning of some internal and external XML entities.
     *
     * @param name the name of the entity.  If it is a parameter
     *             entity, the name will begin with '%', and if it
     *             is the external DTD subset, it will be "[dtd]".
     * @throws SAXException The application may raise an exception.
     */
    @Override
    public void startEntity(String name) throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startEntity(name);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of an entity.
     *
     * @param name the name of the entity that is ending.
     */
    @Override
    public void endEntity(String name) {
      this.saxHandler.endEntity(name);
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the
     * start of a CDATA section.
     *
     * @throws SAXException The application may raise an exception.
     */
    @Override
    public void startCDATA() throws SAXException {
      this.ensureInitialization();
      this.saxHandler.startCDATA();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports the end
     * of a CDATA section.
     *
     * @throws SAXException The application may raise an exception.
     */
    @Override
    public void endCDATA() throws SAXException {
      this.saxHandler.endCDATA();
    }

    /**
     * <i>[SAX LexicalHandler interface support]</i> Reports an XML
     * comment anywhere in the document.
     *
     * @param ch     an array holding the characters in the comment.
     * @param start  the starting position in the array.
     * @param length the number of characters to use from the array.
     * @throws SAXException The application may raise an exception.
     */
    @Override
    public void comment(char[] ch, int start, int length)
      throws SAXException {
      this.ensureInitialization();
      this.saxHandler.comment(ch, start, length);
    }
  }
}