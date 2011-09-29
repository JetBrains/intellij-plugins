package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSet;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSetAndStartsWith;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import com.intellij.psi.search.GlobalSearchScope;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.DataFormatException;

public class SwcDependenciesSorter {
  static final String SWF_EXTENSION = ".swf";

  private THashMap<CharSequence, Definition> definitionMap;

  private final File appDir;
  private final Module module;

  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  private List<LibrarySetItem> items;
  private List<LibrarySetItem> resourceBundleOnlyitems;
  private List<LibrarySetEmbedItem> embedItems;

  private static final Map<String,Set<CharSequence>> badClasses = new THashMap<String, Set<CharSequence>>();

  public static final Set<CharSequence> OVERLOADED_AIR_SPARK_CLASSES = new THashSet<CharSequence>(2, AbcFilter.HASHING_STRATEGY);
  public static final Set<CharSequence> OVERLOADED_MX_CLASSES = new THashSet<CharSequence>(6, AbcFilter.HASHING_STRATEGY);

  static {
    OVERLOADED_AIR_SPARK_CLASSES.add("spark.components:Window");
    OVERLOADED_AIR_SPARK_CLASSES.add("spark.components:WindowedApplication");

    OVERLOADED_MX_CLASSES.add("mx.managers:LayoutManager");
    OVERLOADED_MX_CLASSES.add("mx.managers:CursorManager");
    OVERLOADED_MX_CLASSES.add("mx.managers:CursorManagerImpl");
    OVERLOADED_MX_CLASSES.add("mx.resources:ResourceManager");
    OVERLOADED_MX_CLASSES.add("mx.resources:ResourceManagerImpl");
    OVERLOADED_MX_CLASSES.add("mx.styles:StyleManager");
    OVERLOADED_MX_CLASSES.add("mx.styles:StyleManagerImpl");

    THashSet<CharSequence> set = new THashSet<CharSequence>(2, AbcFilter.HASHING_STRATEGY);
    set.add("AIRSparkClasses");
    set.addAll(OVERLOADED_AIR_SPARK_CLASSES);
    badClasses.put("airspark", set);

    set = new THashSet<CharSequence>(1, AbcFilter.HASHING_STRATEGY);
    set.add("SparkClasses");
    badClasses.put("spark", set);

    set = new THashSet<CharSequence>(1, AbcFilter.HASHING_STRATEGY);
    set.add("MobileComponentsClasses");
    badClasses.put("mobilecomponents", set);

    set = new THashSet<CharSequence>(1, AbcFilter.HASHING_STRATEGY);
    set.add("RPCClasses");
    badClasses.put("rpc", set);

    set = new THashSet<CharSequence>(1, AbcFilter.HASHING_STRATEGY);
    set.add("MxClasses");
    badClasses.put("mx", set);

    set = new THashSet<CharSequence>(1, AbcFilter.HASHING_STRATEGY);
    set.add("AIRFrameworkClasses");
    badClasses.put("airframework", set);
  }

  public SwcDependenciesSorter(@NotNull File appDir, @NotNull Module module) {
    this.appDir = appDir;
    this.module = module;
  }

  public List<LibrarySetItem> getItems() {
    return items;
  }

  public List<LibrarySetEmbedItem> getEmbedItems() {
    return embedItems;
  }

  public List<LibrarySetItem> getResourceBundleOnlyitems() {
    return resourceBundleOnlyitems;
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

  public void sort(final List<Library> libraries, final String postfix, final String flexSdkVersion, final boolean isFromSdk) throws IOException {
    useIndexForFindDefinitions = !isFromSdk;

    List<LibrarySetItem> unsortedItems = new ArrayList<LibrarySetItem>(libraries.size());
    definitionMap = new THashMap<CharSequence, Definition>(1024);

    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap);
    for (Library library : libraries) {
      LibrarySetItem filteredLibrary = new LibrarySetItem(library);
      catalogXmlBuilder.setLibrary(filteredLibrary);
      new XmlBuilderDriver(VfsUtil.loadText(library.getCatalogFile())).build(catalogXmlBuilder);
      if (filteredLibrary.hasDefinitions() || library.hasResourceBundles()) {
        unsortedItems.add(filteredLibrary);
        if (filteredLibrary.hasUnresolvedDefinitions()) {
          filteredLibrary.unresolvedDefinitionPolicy = filteredLibrary.unresolvedDefinitions.size();
        }
      }
    }

    if (isFromSdk) {
      analyzeDefinitions();
    }
    else {
      AccessToken token = ReadAction.start();
      try {
        analyzeDefinitions();
      }
      finally {
        token.finish();
      }
    }

    definitionMap = null;

    final TLinkedList<LibrarySetItem> queue = new TLinkedList<LibrarySetItem>();
    AbcFilter filter = null;
    for (LibrarySetItem item : unsortedItems) {
      if (!item.hasDefinitions()) {
        if (item.library.hasResourceBundles()) {
          queue.add(item);
        }
        continue;
      }

      if (item.inDegree == 0) {
        if (item.hasUnresolvedDefinitions()) {
          // if lib has inDegree == 0 and has unresolved defitions, then all item definitions are unresolved
        }
        else {
          queue.add(item);
        }
      }

      Collection<CharSequence> filteredDefinitions = null;
      if (isFromSdk) {
        String path = item.library.getPath();
        if (path.startsWith("framework")) {
          injectFrameworkSwc(flexSdkVersion, item);
          continue;
        }
        else {
          int firstDotIndex = path.indexOf('.');
          if (firstDotIndex != -1) {
            filteredDefinitions = badClasses.get(path.substring(0, firstDotIndex));
            if (filteredDefinitions != null && !item.hasMissedDefinitions() && item.unresolvedDefinitionPolicy != 0) {
              item.unresolvedDefinitions.addAll(filteredDefinitions);
              filteredDefinitions = item.unresolvedDefinitions;
            }
          }
        }
      }

      if (item.hasUnresolvedDefinitions()) {
        if (item.hasMissedDefinitions()) {
          item.filtered = true;
          if (DebugPathManager.IS_DEV) {
            printCollection(item, postfix);
          }
        }

        filteredDefinitions = item.unresolvedDefinitions;
      }

      final VirtualFile swfFile = item.library.getSwfFile();
      final File modifiedSwf = item.filtered ? createSwfOutFile(item.library, postfix) : createSwfOutFile(item.library);
      final long timeStamp = swfFile.getTimeStamp();
      if (timeStamp != modifiedSwf.lastModified()) {
        if (filter == null) {
          filter = new AbcFilter(isFromSdk ? flexSdkVersion : null);
        }
        filter.filter(swfFile, modifiedSwf, filteredDefinitions == null ? null : new AbcNameFilterByNameSet(filteredDefinitions));
        //noinspection ResultOfMethodCallIgnored
        modifiedSwf.setLastModified(timeStamp);
      }
    }

    if (isFromSdk) {
      embedItems = new ArrayList<LibrarySetEmbedItem>(2);
    }

    items = new ArrayList<LibrarySetItem>(unsortedItems.size());
    while (!queue.isEmpty()) {
      LibrarySetItem item = queue.removeFirst();
      assert item.hasDefinitions() || item.library.hasResourceBundles();
      if (item.library.hasResourceBundles() && !item.hasDefinitions()) {
        if (resourceBundleOnlyitems == null) {
          resourceBundleOnlyitems = new ArrayList<LibrarySetItem>();
        }
        resourceBundleOnlyitems.add(item);
      }
      else {
        items.add(item);
      }

      if (isFromSdk && item.library.defaultsStyle != null) {
        final String path = item.library.getPath();
        String complementName = null;
        if (path.startsWith("spark")) {
          complementName = "flex" + flexSdkVersion;
        }
        else if (path.startsWith("airspark")) {
          complementName = "air4";
        }

        if (complementName != null) {
          embedItems.add(new LibrarySetEmbedItem(complementName, item));
        }
      }

      for (LibrarySetItem successor : item.successors) {
        if (--successor.inDegree == 0) {
          queue.add(successor);
        }
      }
    }
  }

  private File createSwfOutFile(Library library) {
    return new File(appDir, library.getPath() + SWF_EXTENSION);
  }

  private File createSwfOutFile(Library library, String postfix) {
    return new File(appDir, library.getPath() + "_" + postfix + SWF_EXTENSION);
  }

  public void printCollection(LibrarySetItem library, String postfix) throws IOException {
    FileWriter writer = new FileWriter(new File(appDir, library.library.getPath() + '_' + postfix + "_unresolvedDefinitions.txt"));
    for (CharSequence s : library.unresolvedDefinitions) {
      writer.append(s);
      writer.append('\n');
    }

    writer.flush();
  }

  private void injectFrameworkSwc(String flexSdkVersion, LibrarySetItem library) throws IOException {
    VirtualFile swfFile = library.library.getSwfFile();
    File modifiedSwf = createSwfOutFile(library.library);
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

    if (library.hasUnresolvedDefinitions() || timeStamp > modifiedSwf.lastModified() ||
        injectionLastModified > modifiedSwf.lastModified()) {
      Set<CharSequence> definitions = library.hasUnresolvedDefinitions() ? library.unresolvedDefinitions : new THashSet<CharSequence>(8, AbcFilter.HASHING_STRATEGY);
      definitions.add("FrameworkClasses");
      definitions.add("mx.managers.systemClasses:MarshallingSupport");
      definitions.add("mx.managers:SystemManagerProxy");

      definitions.addAll(OVERLOADED_MX_CLASSES);

      new FlexSdkAbcInjector(flexSdkVersion, injectionUrlConnection).filter(swfFile, modifiedSwf,
                             new AbcNameFilterByNameSetAndStartsWith(definitions, new String[]{"mx.managers.marshalClasses:"}));
    }
  }

  private void analyzeDefinitions() {
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      final Definition definition = entry.getValue();
      if (definition.dependencies != null && (definition.hasUnresolvedDependencies == Definition.UnresolvedState.NO ||
                                              (definition.hasUnresolvedDependencies == Definition.UnresolvedState.UNKNOWN &&
                                               !hasUnresolvedDependencies(definition, entry.getKey())))) {
        final LibrarySetItem library = definition.getLibrary();
        for (CharSequence dependencyId : definition.dependencies) {
          if (dependencyId == null) {
            continue;
          }

          final LibrarySetItem dependencyLibrary = definitionMap.get(dependencyId).getLibrary();
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
  private Map<CharSequence, Definition> getDefinitions(LibrarySetItem library) {
    Map<CharSequence, Definition> definitions = new HashMap<CharSequence, Definition>();
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      if (entry.getValue().getLibrary() == library) {
        definitions.put(entry.getKey(), entry.getValue());
      }
    }

    return definitions;
  }

  private GlobalSearchScope definitionSearchScope;

  private boolean hasUnresolvedDependencies(Definition definition, CharSequence definitionName) {
    // set before to prevent stack overflow for crossed dependencies
    definition.hasUnresolvedDependencies = Definition.UnresolvedState.NO;

    CharSequence[] dependencies = definition.dependencies;
    for (int i = 0, dependenciesLength = dependencies.length; i < dependenciesLength; i++) {
      CharSequence dependencyId = dependencies[i];
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null && useIndexForFindDefinitions) {
        int length = dependencyId.length();
        if (definitionSearchScope == null) {
          definitionSearchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
          fqnBuffer = new char[Math.max(length, 512)];
        }
        else if (fqnBuffer.length < length) {
          fqnBuffer = new char[length];
        }

        String fqn = (String)dependencyId;
        fqn.getChars(0, length, fqnBuffer, 0);
        fqnBuffer[fqn.lastIndexOf(':')] = '.';
        if (JSResolveUtil.findClassByQName(new String(fqnBuffer, 0, length), definitionSearchScope) != null) {
          dependencies[i] = null;
          continue;
        }
      }

      if (dependency == null || dependency.hasUnresolvedDependencies == Definition.UnresolvedState.YES ||
          (dependency.hasUnresolvedDependencies == Definition.UnresolvedState.UNKNOWN &&
           hasUnresolvedDependencies(dependency, dependencyId))) {
        definition.markAsUnresolved(definitionName);
        definition.hasUnresolvedDependencies = Definition.UnresolvedState.YES;
        return true;
      }
    }

    return false;
  }
}