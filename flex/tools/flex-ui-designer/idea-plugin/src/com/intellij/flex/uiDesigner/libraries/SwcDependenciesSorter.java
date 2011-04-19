package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkedList;
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
      if (definition.dependencies != null && (definition.hasUnresolvedDependencies == Definition.UnresolvedState.NO ||
                                              (definition.hasUnresolvedDependencies == Definition.UnresolvedState.UNKNOWN &&
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
    definition.hasUnresolvedDependencies = Definition.UnresolvedState.NO;

    for (CharSequence dependencyId : definition.dependencies) {
      final Definition dependency = definitionMap.get(dependencyId);
      if (dependency == null || dependency.hasUnresolvedDependencies == Definition.UnresolvedState.YES ||
          (dependency.hasUnresolvedDependencies == Definition.UnresolvedState.UNKNOWN && hasUnresolvedDependencies(dependency, dependencyId))) {
        definition.getLibrary().unresolvedDefinitions.add(definitionName);
        definition.hasUnresolvedDependencies = Definition.UnresolvedState.YES;
        return true;
      }
    }

    return false;
  }
}