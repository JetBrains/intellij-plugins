package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.AbcNameFilter;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilder;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.DataFormatException;

public class SwcDependenciesSorter {
  private Map<CharSequence, Definition> definitionMap;

  private final File rootPath;

  public SwcDependenciesSorter(File rootPath) {
    this.rootPath = rootPath;
  }

  private static Collection<CharSequence> getBadAirsparkClasses() {
    Collection<CharSequence> classes = new ArrayList<CharSequence>(2);
    classes.add("AIRSparkClasses");
    classes.add("spark.components:WindowedApplication");
    return classes;
  }

  public static void main(String[] args) throws IOException, DataFormatException {
    createInjectionAbc("4.1", true);
    createInjectionAbc("4.5", true);
  }

  private static long createInjectionAbc(String flexSdkVersion, boolean force) throws IOException {
    final String rootPath = DebugPathManager.getFudHome() + "/flex-injection/target";
    File abcSource = ComplementSwfBuilder.getSourceFile(rootPath, flexSdkVersion);
    File abc = ComplementSwfBuilder.createAbcFile(rootPath, flexSdkVersion);
    if (!force && abcSource.lastModified() < abc.lastModified()) {
      return abc.lastModified();
    }

    ComplementSwfBuilder.build(rootPath, flexSdkVersion);
    return abc.lastModified();
  }

  public List<Library> sort(final List<OriginalLibrary> libraries, final String postfix, final String flexSdkVersion) throws IOException {
    List<OriginalLibrary> filteredLibraries = new ArrayList<OriginalLibrary>(libraries.size());
    definitionMap = new THashMap<CharSequence, Definition>();

    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap);
    for (OriginalLibrary library : libraries) {
      catalogXmlBuilder.setLibrary(library);
      new XmlBuilderDriver(VfsUtil.loadText(library.getCatalogFile())).build(catalogXmlBuilder);
      if (library.hasDefinitions()) {
        filteredLibraries.add(library);
      }
    }

    analyzeDefinitions();
    definitionMap = null;

    TLinkedList<OriginalLibrary> queue = new TLinkedList<OriginalLibrary>();
    AbcFilter filter = null;
    for (OriginalLibrary library : filteredLibraries) {
      if (!library.hasDefinitions()) {
        continue;
      }

      boolean abcModified = false;
      if (library.isFromSdk()) {
        String path = library.getPath();
        if (path.startsWith("framework")) {
          abcModified = true;
          injectFrameworkSwc(flexSdkVersion, library);
        }
        else if (path.startsWith("airspark")) {
          if (library.hasUnresolvedDefinitions()) {
            library.unresolvedDefinitions.addAll(getBadAirsparkClasses());
          }
          else {
            abcModified = true;
            removeBadClassesFromLibrary(library, getBadAirsparkClasses(), false);
          }
        }
      }

      if (library.mxCoreFlexModuleFactoryClassName != null && !library.hasUnresolvedDefinitions()) {
        Collection<CharSequence> classes = new ArrayList<CharSequence>(1);
        classes.add(library.mxCoreFlexModuleFactoryClassName);
        abcModified = true;
        removeBadClassesFromLibrary(library, classes, true);
      }

      if (library.inDegree == 0) {
        if (library.hasUnresolvedDefinitions()) {
          // if lib has inDegree == 0 and has unresolved defitions, then all library definitions are unresolved
        }
        else {
          queue.add(library);
        }
      }
      else if (!abcModified && !library.unresolvedDefinitions.isEmpty()) {
        if (filter == null) {
          filter = new AbcFilter();
        }
        library.filtered = true;
        if (DebugPathManager.IS_DEV) {
          printCollection(library.unresolvedDefinitions,
                          new FileWriter(new File(rootPath, library.getPath() + "_unresolvedDefinitions.txt")));
        }

        if (library.mxCoreFlexModuleFactoryClassName != null) {
          library.unresolvedDefinitions.add(library.mxCoreFlexModuleFactoryClassName);
        }

        abcModified = true;
        filter.filter(library.getSwfFile(), createSwfOutFile(library, postfix), new AbcNameFilterByNameSet(library.unresolvedDefinitions));
      }

      if (!abcModified) {
        copyLibrarySwf(library);
      }
    }

    List<Library> sortedLibraries = new ArrayList<Library>(filteredLibraries.size());
    while (!queue.isEmpty()) {
      OriginalLibrary library = queue.removeFirst();
      assert library.hasDefinitions();
      sortedLibraries.add(library);

      if (library.defaultsStyle != null) {
        String path = library.getPath();
        String complementName = null;
        if (path.startsWith("spark")) {
          complementName = "flex" + flexSdkVersion;
        }
        else if (path.startsWith("airspark")) {
          complementName = "air4";
        }

        if (complementName != null) {
          sortedLibraries.add(new EmbedLibrary(complementName, library));
        }
      }

      for (OriginalLibrary successor : library.successors) {
        //successor.parents.remove(library);
        if (--successor.inDegree == 0) {
          queue.add(successor);
        }
      }
    }

    return sortedLibraries;
  }

  private File createSwfOutFile(OriginalLibrary library) {
    return new File(rootPath, library.getPath() + ".swf");
  }

  private File createSwfOutFile(OriginalLibrary library, String postfix) {
    return new File(rootPath, library.getPath() + "_" + postfix + ".swf");
  }

  public static void printCollection(Set<CharSequence> set, FileWriter writer) throws IOException {
    for (CharSequence s : set) {
      writer.append(s);
      writer.append('\n');
    }

    writer.flush();
  }

  private void copyLibrarySwf(OriginalLibrary library) throws IOException {
    VirtualFile swfFile = library.getSwfFile();
    File modifiedSwf = createSwfOutFile(library);
    final long timeStamp = swfFile.getTimeStamp();
    if (timeStamp != modifiedSwf.lastModified()) {
      final InputStream inputStream = swfFile.getInputStream();
      try {
        IOUtil.saveStream(inputStream, modifiedSwf);
        //noinspection ResultOfMethodCallIgnored
        modifiedSwf.setLastModified(timeStamp);
      }
      finally {
        inputStream.close();
      }
    }
  }

  private void injectFrameworkSwc(String flexSdkVersion, OriginalLibrary library) throws IOException {
    VirtualFile swfFile = library.getSwfFile();
    File modifiedSwf = createSwfOutFile(library);
    final long timeStamp = swfFile.getTimeStamp();
    
    final long injectionLastModified;
    final URLConnection injectionUrlConnection;
    if (DebugPathManager.IS_DEV) {
      injectionLastModified = createInjectionAbc(flexSdkVersion, false);
      injectionUrlConnection = null;
    }
    else {
      URL url = getClass().getClassLoader().getResource(ComplementSwfBuilder.generateInjectionName(flexSdkVersion));
      injectionUrlConnection = url.openConnection();
      injectionLastModified = injectionUrlConnection.getLastModified();
    }

    if (library.hasUnresolvedDefinitions() ||
        timeStamp > modifiedSwf.lastModified() ||
        injectionLastModified > modifiedSwf.lastModified()) {
      Set<CharSequence> definitions = library.hasUnresolvedDefinitions()
                                      ? library.unresolvedDefinitions
                                      : new THashSet<CharSequence>(5);
      definitions.add("FrameworkClasses");
      definitions.add("mx.managers.systemClasses:MarshallingSupport");
      definitions.add("mx.managers:SystemManagerProxy");

      definitions.add("mx.styles:StyleManager");
      definitions.add(FlexSdkAbcInjector.LAYOUT_MANAGER);
      definitions.add("mx.styles:StyleManagerImpl");
      new FlexSdkAbcInjector(injectionUrlConnection).inject(swfFile, modifiedSwf, flexSdkVersion,
                             new AbcNameFilterByNameSetAndStartsWith(definitions, new String[]{"mx.managers.marshalClasses:"}));
    }
  }

  private void removeBadClassesFromLibrary(OriginalLibrary library, Collection<CharSequence> definitions, boolean replaceMainClass)
    throws IOException {
    VirtualFile swfFile = library.getSwfFile();
    File modifiedSwf = createSwfOutFile(library);
    final long timeStamp = swfFile.getTimeStamp();
    if (timeStamp != modifiedSwf.lastModified()) {
      AbcFilter filter = new AbcFilter();
      filter.replaceMainClass = replaceMainClass;
      filter.filter(swfFile, modifiedSwf, new AbcNameFilterByNameSet(definitions));
      //noinspection ResultOfMethodCallIgnored
      modifiedSwf.setLastModified(timeStamp);
    }
  }

  private void analyzeDefinitions() {
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      final Definition definition = entry.getValue();
      if (definition.dependencies != null && (definition.hasUnresolvedDependencies == UnresolvedState.NO ||
                                              (definition.hasUnresolvedDependencies == UnresolvedState.UNKNOWN &&
                                               !hasUnresolvedDependencies(definition, entry.getKey())))) {
        final OriginalLibrary library = definition.getLibrary();
        for (CharSequence dependencyId : definition.dependencies) {
          final OriginalLibrary dependencyLibrary = definitionMap.get(dependencyId).getLibrary();
          if (library != dependencyLibrary) {
            if (dependencyLibrary.successors.add(library)) {
              library.inDegree++;
            }

            if (dependencyLibrary.parents.contains(library)) {
              throw new Error();
            }
            library.parents.add(dependencyLibrary);
          }
        }
      }
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @TestOnly
  private Map<CharSequence, Definition> getDefinitions(OriginalLibrary library) {
    Map<CharSequence, Definition> definitions = new HashMap<CharSequence, Definition>();
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      if (entry.getValue().getLibrary() == library) {
        definitions.put(entry.getKey(), entry.getValue());
      }
    }

    return definitions;
  }

  private boolean hasUnresolvedDependencies(Definition definition, CharSequence definitionName) {
    // set before to prevent stack overflow for crossed dependencies
    definition.hasUnresolvedDependencies = UnresolvedState.NO;

    for (CharSequence dependencyId : definition.dependencies) {
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null || dependency.hasUnresolvedDependencies == UnresolvedState.YES ||
          (dependency.hasUnresolvedDependencies == UnresolvedState.UNKNOWN && hasUnresolvedDependencies(dependency, dependencyId))) {
        definition.getLibrary().unresolvedDefinitions.add(definitionName);
        definition.hasUnresolvedDependencies = UnresolvedState.YES;
        return true;
      }
    }

    return false;
  }

  private static class CatalogXmlBuilder implements XmlBuilder {
    private boolean processDependencies;

    private Definition definition;
    private final ArrayList<CharSequence> dependencies = new ArrayList<CharSequence>();
    private CharSequence mod;

    private OriginalLibrary library;
    private final Map<CharSequence, Definition> definitionMap;

    public CatalogXmlBuilder(final Map<CharSequence, Definition> definitionMap) {
      this.definitionMap = definitionMap;
    }

    public void setLibrary(OriginalLibrary library) {
      this.library = library;
    }

    @Override
    public void doctype(@Nullable CharSequence publicId, @Nullable CharSequence systemId, int startOffset, int endOffset) {
    }

    @Override
    public ProcessingOrder startTag(CharSequence localName, String namespace, int startoffset, int endoffset, int headerEndOffset) {
      if (localName.equals("script")) {
        return ProcessingOrder.TAGS_AND_ATTRIBUTES;
      }
      else if (localName.equals("def")) {
        definition = new Definition(library);
        return ProcessingOrder.TAGS_AND_ATTRIBUTES;
      }
      else if (definition != null) {
        return ProcessingOrder.TAGS_AND_ATTRIBUTES;
      }
      else {
        return ProcessingOrder.TAGS;
      }
    }

    @Override
    public void endTag(CharSequence localName, String namespace, int startoffset, int endoffset) {
      if (processDependencies && localName.equals("script")) {
        if (!dependencies.isEmpty()) {
          definition.dependencies = dependencies.toArray(new CharSequence[dependencies.size()]);
          dependencies.clear();
        }
        else {
          definition.hasUnresolvedDependencies = UnresolvedState.NO;
        }
        definition = null;
        processDependencies = false;
      }
    }

    @Override
    public void attribute(CharSequence name, CharSequence value, int startoffset, int endoffset) {
      if (name.equals("mod")) {
        mod = value;
      }
      else if (processDependencies) {
        if (name.equals("id") &&
            !(StringUtil.startsWith(value, "flash.") ||
              value.charAt(0) == '_' ||
              !StringUtil.contains(value, 1, value.length() - 1, ':'))) {
          dependencies.add(value);
        }
      }
      else if (definition != null) {
        if (value.charAt(0) != '_') {
          Definition oldDefinition = definitionMap.get(value);
          if (oldDefinition == null) {
            definition.setTimeAsCharSequence(mod);
          }
          else {
            definition.time = Long.parseLong(mod.toString());
            if (definition.time > oldDefinition.getTime()) {
              oldDefinition.getLibrary().definitionCounter--;
              oldDefinition.getLibrary().unresolvedDefinitions.add(value);
            }
            else {
              definition = null;
              // filter library, remove definition abc bytecode from lib swf
              library.unresolvedDefinitions.add(value);
              return;
            }
          }

          definitionMap.put(value, definition);
          library.definitionCounter++;
          processDependencies = true;
        }
        else if (StringUtil.endsWith(value, "_mx_core_FlexModuleFactory")) {
          library.mxCoreFlexModuleFactoryClassName = value;
        }
        else {
          definition = null;
        }
      }
    }

    @Override
    public void textElement(CharSequence display, CharSequence physical, int startoffset, int endoffset) {
    }

    @Override
    public void entityRef(CharSequence ref, int startOffset, int endOffset) {
    }

    @Override
    public void error(String message, int startOffset, int endOffset) {
    }
  }

  private static class UnresolvedState {
    public static int UNKNOWN = 0;
    public static int YES = 1;
    public static int NO = -1;
  }

  private static class Definition {
    private final OriginalLibrary library;

    public CharSequence[] dependencies;
    public int hasUnresolvedDependencies = UnresolvedState.UNKNOWN;

    private CharSequence timeAsCharSequence;
    public long time = -1;

    public void setTimeAsCharSequence(CharSequence timeAsCharSequence) {
      this.timeAsCharSequence = timeAsCharSequence;
    }

    public long getTime() {
      if (time == -1) {
        time = Long.parseLong(timeAsCharSequence.toString());
      }

      return time;
    }

    Definition(final OriginalLibrary library) {
      this.library = library;
      if (library.isFromSdk()) {
        time = Long.MAX_VALUE;
      }
    }

    @NotNull
    public OriginalLibrary getLibrary() {
      return library;
    }
  }
}

class AbcNameFilterByNameSet implements AbcNameFilter {
  private final Collection<CharSequence> definitions;
  protected final boolean inclusion;

  AbcNameFilterByNameSet(Collection<CharSequence> definitions) {
    this.definitions = definitions;
    inclusion = false;
  }

  AbcNameFilterByNameSet(Collection<CharSequence> definitions, boolean inclusion) {
    this.definitions = definitions;
    this.inclusion = inclusion;
  }

  @Override
  public boolean accept(String name) {
    return definitions.contains(name) == inclusion;
  }
}

class AbcNameFilterByNameSetAndStartsWith extends AbcNameFilterByNameSet {
  private final String[] startsWith;

  AbcNameFilterByNameSetAndStartsWith(Collection<CharSequence> definitions, String[] startsWith) {
    this(definitions, startsWith, false);
  }

  AbcNameFilterByNameSetAndStartsWith(Collection<CharSequence> definitions, String[] startsWith, boolean inclusion) {
    super(definitions, inclusion);
    this.startsWith = startsWith;
  }

  @Override
  public boolean accept(String name) {
    if (inclusion) {
      if (super.accept(name)) {
        return true;
      }
    }
    else if (!super.accept(name)) {
      return false;
    }

    for (String s : startsWith) {
      if (name.startsWith(s)) {
        return inclusion;
      }
    }

    return !inclusion;
  }
}

class AbcNameFilterStartsWith implements AbcNameFilter {
  private final boolean inclusion;
  private final String startsWith;

  public AbcNameFilterStartsWith(String startsWith, boolean inclusion) {
    this.startsWith = startsWith;
    this.inclusion = inclusion;
  }

  @Override
  public boolean accept(String name) {
    if (name.startsWith(startsWith)) {
      return inclusion;
    }

    return !inclusion;
  }
}