package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSet;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSetAndStartsWith;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector.FrameworkAbcInjector;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector.SparkAbcInjector;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses.InjectionClassifier;
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

public class SwcDependenciesSorter {
  static final String SWF_EXTENSION = ".swf";

  private THashMap<CharSequence, Definition> definitionMap;

  private final File appDir;
  private final Module module;

  private GlobalSearchScope definitionSearchScope;
  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  private static final Map<String,Set<CharSequence>> BAD_CLASSES = new THashMap<String, Set<CharSequence>>();

  static {
    THashSet<CharSequence> set = createSet(FlexOverloadedClasses.AIR_SPARK_CLASSES.size() + 1);
    set.add("AIRSparkClasses");
    set.addAll(FlexOverloadedClasses.AIR_SPARK_CLASSES);
    BAD_CLASSES.put("airspark", set);

    set = createSet(1);
    set.add("MobileComponentsClasses");
    BAD_CLASSES.put("mobilecomponents", set);

    set = createSet(1);
    set.add("RPCClasses");
    BAD_CLASSES.put("rpc", set);

    set = createSet(1);
    set.add("MxClasses");
    BAD_CLASSES.put("mx", set);

    set = createSet(1);
    set.add("AIRFrameworkClasses");
    BAD_CLASSES.put("airframework", set);
  }

  public SwcDependenciesSorter(@NotNull File appDir, @NotNull Module module) {
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

  public SortResult sort(final List<Library> libraries, final String postfix, final String flexSdkVersion, final boolean isFromSdk) throws IOException {
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

    LibrarySetItem sparkLib = null;
    if (isFromSdk) {
      analyzeDefinitions();
      final Definition definition = definitionMap.get("spark.components.supportClasses:SkinnableComponent");
      if (definition != null) {
        sparkLib = definition.getLibrary();
      }
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
        queue.add(item);
      }

      Collection<CharSequence> filteredDefinitions = null;
      if (isFromSdk) {
        final String path = item.library.getPath();
        if (path.startsWith("framework") || item == sparkLib) {
          injectFrameworkSwc(flexSdkVersion, item, item == sparkLib);
          continue;
        }
        else {
          final int namePostfixIndex = path.indexOf(LibraryManager.NAME_POSTFIX);
          if (namePostfixIndex != -1) {
            filteredDefinitions = BAD_CLASSES.get(path.substring(0, namePostfixIndex));
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
    
    return new SortResult(items, embedItems, resourceBundleOnlyitems);
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

    if (library.hasUnresolvedDefinitions() || timeStamp > modifiedSwf.lastModified() ||
        injectionLastModified > modifiedSwf.lastModified()) {
      final Set<CharSequence> definitions = library.hasUnresolvedDefinitions()
                                            ? library.unresolvedDefinitions
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

  private boolean hasUnresolvedDependencies(Definition definition, CharSequence definitionName) {
    // set before to prevent stack overflow for crossed dependencies
    definition.hasUnresolvedDependencies = Definition.UnresolvedState.NO;

    CharSequence[] dependencies = definition.dependencies;
    for (int i = 0, dependenciesLength = dependencies.length; i < dependenciesLength; i++) {
      CharSequence dependencyId = dependencies[i];
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null && useIndexForFindDefinitions) {
        final int length = dependencyId.length();
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