package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSet;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSetAndStartsWith;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector.FrameworkAbcInjector;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector.SparkAbcInjector;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses.InjectionClassifier;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.util.text.CharSequenceBackedByArray;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static com.intellij.flex.uiDesigner.libraries.Definition.UnresolvedState;
import static com.intellij.flex.uiDesigner.libraries.LibrarySorter.FlexLibsNames.*;

public class LibrarySorter {
  static final String SWF_EXTENSION = ".swf";

  @SuppressWarnings("unchecked")
  final static Pair<String, String>[] FLEX_LIBS_PATTERNS = new Pair[]{
    new Pair<String, String>(FlexLibsNames.FRAMEWORK, "FrameworkClasses"),
    new Pair<String, String>(FlexLibsNames.AIRFRAMEWORK, "AIRFrameworkClasses"),
    new Pair<String, String>(FlexLibsNames.SPARK, "SparkClasses"),
    new Pair<String, String>(FlexLibsNames.AIRSPARK, "AIRSparkClasses"),

    new Pair<String, String>(FlexLibsNames.MX, "MxClasses"),
    new Pair<String, String>(FlexLibsNames.RPC, "RPCClasses"),
    new Pair<String, String>(FlexLibsNames.MOBILECOMPONENTS, "MobileComponentsClasses")};

  private final File appDir;
  private final Module module;

  private GlobalSearchScope definitionSearchScope;
  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  private static final Map<String,Set<CharSequence>> BAD_FLEX_CLASSES = new THashMap<String, Set<CharSequence>>();
  
  public interface FlexLibsNames {
    String AIRSPARK = "airspark";
    String SPARK = "spark";
    String FRAMEWORK = "framework";
    String AIRFRAMEWORK = "airframework";
    String MX = "mx";
    String RPC = "rpc";
    String MOBILECOMPONENTS = "mobilecomponents";
  }

  static {
    Set<CharSequence> set;
    for (Pair<String, String> pair :   FLEX_LIBS_PATTERNS) {
      if (pair.first.equals(FlexLibsNames.AIRSPARK)) {
        set = createSet(FlexOverloadedClasses.AIR_SPARK_CLASSES.size() + 1);
        set.addAll(FlexOverloadedClasses.AIR_SPARK_CLASSES);
      }
      else {
        set = createSet(1);
      }

      set.add(pair.second);
      BAD_FLEX_CLASSES.put(pair.first, set);
    }
  }

  public LibrarySorter(@NotNull File appDir, @NotNull Module module) {
    this.appDir = appDir;
    this.module = module;
  }

  private static long createInjectionAbc(String flexSdkVersion, InjectionClassifier classifier, boolean force) throws IOException {
    final String rootPath = DebugPathManager.getFudHome() + "/flex-injection/target";
    File abcSource = ComplementSwfBuilder.getSourceFile(rootPath, flexSdkVersion);
    File abc = ComplementSwfBuilder.createAbcFile(rootPath, flexSdkVersion, classifier);
    if (!force && abcSource.lastModified() < abc.lastModified()) {
      return abc.lastModified();
    }

    ComplementSwfBuilder.build(rootPath, flexSdkVersion, classifier);
    return abc.lastModified();
  }

  private List<LibrarySetItem> collectItems(final List<Library> libraries, final boolean isFromSdk) throws IOException {
    final List<LibrarySetItem> unsortedItems = new ArrayList<LibrarySetItem>(libraries.size());
    final THashMap<CharSequence, Definition> definitionMap = new THashMap<CharSequence, Definition>(unsortedItems.size() * 128);
    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap);
    for (Library library : libraries) {
      LibrarySetItem filteredLibrary = new LibrarySetItem(library);
      catalogXmlBuilder.setLibrary(filteredLibrary);
      new XmlBuilderDriver(IOUtil.getCharSequence(library.getCatalogFile())).build(catalogXmlBuilder);
      if (filteredLibrary.hasDefinitions() || library.hasResourceBundles()) {
        unsortedItems.add(filteredLibrary);
        filteredLibrary.finalizeProcessCatalog();
      }
    }

    if (isFromSdk) {
      analyzeDefinitions(definitionMap);
    }
    else {
      final AccessToken token = ReadAction.start();
      try {
        analyzeDefinitions(definitionMap);
      }
      finally {
        token.finish();
      }
    }

    return unsortedItems;
  }

  public SortResult sort(final List<Library> libraries, final String flexSdkVersion, final boolean isFromSdk) throws IOException {
    useIndexForFindDefinitions = !isFromSdk;

    final List<LibrarySetItem> unsortedItems = collectItems(libraries, isFromSdk);
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
        queue.add(item);
      }

      Collection<CharSequence> filteredDefinitions = null;
      item.filtered = item.hasUnresolvedDefinitions();
      if (isFromSdk) {
        final String name = item.library.getName();
        final boolean isSparkLib = name.equals(SPARK);
        if (isSparkLib || name.equals(FRAMEWORK)) {
          // never can be filtered
          item.filtered = false;
          injectFrameworkSwc(flexSdkVersion, item, isSparkLib);
          continue;
        }
        else {
          filteredDefinitions = BAD_FLEX_CLASSES.get(name);
          if (filteredDefinitions != null && item.hasFilteredDefinitions()) {
            // http://youtrack.jetbrains.net/issue/AS-198
            if (name.equals(AIRFRAMEWORK)) {
              if (item.filteredDefinitions.contains("mx.controls:FlexNativeMenu")) {
                item.filteredDefinitions.remove("mx.controls:FlexNativeMenu");
                if (item.filtered && item.filteredDefinitions.isEmpty()) {
                  item.filtered = false;
                }
              }
            }
            else if (name.equals(AIRSPARK)) {
              if (item.filteredDefinitions.contains(FlexOverloadedClasses.SPARK_WINDOW) && item.filtered &&
                  item.filteredDefinitions.size() == 1) {
                item.filtered = false;
              }
            }

            item.filteredDefinitions.addAll(filteredDefinitions);
            filteredDefinitions = item.filteredDefinitions;
          }
        }
      }
      
      if (filteredDefinitions == null) {
        filteredDefinitions = item.filteredDefinitions;
      }

      final VirtualFile swfFile = item.library.getSwfFile();
      final File modifiedSwf = createSwfOutFile(item.library);
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

    final ArrayList<LibrarySetEmbedItem> embedItems = isFromSdk ? new ArrayList<LibrarySetEmbedItem>(2) : null;
    final ArrayList<LibrarySetItem> items = new ArrayList<LibrarySetItem>(unsortedItems.size());
    ArrayList<LibrarySetItem> resourceBundleOnlyitems = null;
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
        final String name = item.library.getName();
        String complementName = null;
        if (name.equals(SPARK)) {
          complementName = "flex" + flexSdkVersion;
        }
        else if (name.equals(AIRSPARK)) {
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
    
    return new SortResult(items, embedItems, resourceBundleOnlyitems);
  }

  private File createSwfOutFile(Library library) {
    return new File(appDir, library.getPath() + SWF_EXTENSION);
  }

  private File createSwfOutFile(Library library, String postfix) {
    return new File(appDir, library.getPath() + "_" + postfix + SWF_EXTENSION);
  }

  private void printCollection(LibrarySetItem library, String postfix) throws IOException {
    final Writer writer = new BufferedWriter(new FileWriter(new File(appDir, library.library.getPath() + '_' + postfix + "_unresolvedDefinitions.txt")));
    try {
      for (CharSequence s : library.filteredDefinitions) {
        writer.append(s);
        writer.append('\n');
      }
    }
    finally {
      writer.close();
    }
  }

  private void injectFrameworkSwc(String flexSdkVersion, LibrarySetItem library, boolean isSpark) throws IOException {
    final VirtualFile swfFile = library.library.getSwfFile();
    final File modifiedSwf = createSwfOutFile(library.library);
    final long timeStamp = swfFile.getTimeStamp();
    
    final long injectionLastModified;
    final URLConnection injectionUrlConnection;
    final InjectionClassifier classifier = isSpark ? InjectionClassifier.spark : InjectionClassifier.framework;
    if (DebugPathManager.IS_DEV) {
      injectionLastModified = createInjectionAbc(flexSdkVersion, classifier, false);
      injectionUrlConnection = null;
    }
    else {
      URL url = getClass().getClassLoader().getResource(ComplementSwfBuilder.generateInjectionName(flexSdkVersion, classifier));
      injectionUrlConnection = url.openConnection();
      injectionLastModified = injectionUrlConnection.getLastModified();
    }

    if (library.hasFilteredDefinitions() || timeStamp > modifiedSwf.lastModified() ||
        injectionLastModified > modifiedSwf.lastModified()) {
      final Set<CharSequence> definitions = library.hasFilteredDefinitions()
                                            ? library.filteredDefinitions
                                            : createSet((isSpark ? 0 : FlexOverloadedClasses.MX_CLASSES.size()) + 3);
      if (isSpark) {
        definitions.add("SparkClasses");
        new SparkAbcInjector(flexSdkVersion, injectionUrlConnection).filter(swfFile, modifiedSwf, new AbcNameFilterByNameSet(definitions));
      }
      else {
        definitions.add("FrameworkClasses");
        definitions.add("mx.managers.systemClasses:MarshallingSupport");
        definitions.add("mx.managers:SystemManagerProxy");

        definitions.addAll(FlexOverloadedClasses.MX_CLASSES);

        new FrameworkAbcInjector(flexSdkVersion, injectionUrlConnection)
          .filter(swfFile, modifiedSwf, new AbcNameFilterByNameSetAndStartsWith(definitions, new String[]{"mx.managers.marshalClasses:"}));
      }
    }
  }

  private static THashSet<CharSequence> createSet(int size) {
    return new THashSet<CharSequence>(size, AbcFilter.HASHING_STRATEGY);
  }

  private void analyzeDefinitions(THashMap<CharSequence, Definition> definitionMap) {
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      final Definition definition = entry.getValue();
      if (definition.dependencies != null && (definition.hasUnresolvedDependencies == UnresolvedState.NO ||
                                              (definition.hasUnresolvedDependencies == UnresolvedState.UNKNOWN &&
                                               !hasUnresolvedDependencies(definition, entry.getKey(), definitionMap)))) {
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
              throw new IllegalStateException(library + " cannot depends on " + dependencyLibrary + ", because " +  dependencyLibrary + " has " + library + " as parent");
            }
            library.parents.add(dependencyLibrary);
          }
        }
      }
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @TestOnly
  private static Map<CharSequence, Definition> getDefinitions(LibrarySetItem library, THashMap<CharSequence, Definition> definitionMap) {
    Map<CharSequence, Definition> definitions = new HashMap<CharSequence, Definition>();
    for (Map.Entry<CharSequence, Definition> entry : definitionMap.entrySet()) {
      if (entry.getValue().getLibrary() == library) {
        definitions.put(entry.getKey(), entry.getValue());
      }
    }

    return definitions;
  }

  private boolean hasUnresolvedDependencies(Definition definition, CharSequence definitionName,
                                            THashMap<CharSequence, Definition> definitionMap) {
    // set before to prevent stack overflow for crossed dependencies
    definition.hasUnresolvedDependencies = UnresolvedState.NO;

    final CharSequence[] dependencies = definition.dependencies;
    for (int i = 0, dependenciesLength = dependencies.length; i < dependenciesLength; i++) {
      final CharSequence dependencyId = dependencies[i];
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null && useIndexForFindDefinitions) {
        final int length = dependencyId.length();
        if (definitionSearchScope == null) {
          definitionSearchScope = module.getModuleWithDependenciesAndLibrariesScope(false);
          fqnBuffer = new char[Math.max(length, 512)];
        }
        else if (fqnBuffer.length < length) {
          fqnBuffer = new char[length];
        }

        if (dependencyId instanceof CharSequenceBackedByArray) {
          ((CharSequenceBackedByArray)dependencyId).getChars(fqnBuffer, 0);
        }
        else {
          ((String)dependencyId).getChars(0, length, fqnBuffer, 0);
        }

        fqnBuffer[CharArrayUtil.lastIndexOf(fqnBuffer, ':', 1, length - 1)] = '.';
        if (JSResolveUtil.findClassByQName(new String(fqnBuffer, 0, length), definitionSearchScope) != null) {
          dependencies[i] = null;
          continue;
        }
      }

      if (dependency == null || dependency.hasUnresolvedDependencies == UnresolvedState.YES ||
          (dependency.hasUnresolvedDependencies == UnresolvedState.UNKNOWN &&
           hasUnresolvedDependencies(dependency, dependencyId, definitionMap))) {
        definition.markAsUnresolved(definitionName);
        definition.hasUnresolvedDependencies = UnresolvedState.YES;
        return true;
      }
    }

    return false;
  }

  public static class SortResult {
    List<LibrarySetItem> items;
    List<LibrarySetItem> resourceBundleOnlyitems;
    final List<LibrarySetEmbedItem> embedItems;

    private SortResult(List<LibrarySetItem> items, List<LibrarySetEmbedItem> embedItems, List<LibrarySetItem> resourceBundleOnlyitems) {
      this.items = items;
      this.embedItems = embedItems;
      this.resourceBundleOnlyitems = resourceBundleOnlyitems;
    }
  }
}