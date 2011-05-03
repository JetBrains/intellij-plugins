package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.RequiredAssetsInfo;
import com.intellij.flex.uiDesigner.abc.AbcFilter;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSet;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSetAndStartsWith;
import com.intellij.flex.uiDesigner.abc.FlexSdkAbcInjector;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import com.intellij.psi.search.GlobalSearchScope;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkedList;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.DataFormatException;

public class SwcDependenciesSorter {
  private THashMap<CharSequence, Definition> definitionMap;

  private final File rootPath;
  private final Module module;

  private boolean useIndexForFindDefinitions;
  private char[] fqnBuffer;

  public SwcDependenciesSorter(File rootPath, Module module) {
    this.rootPath = rootPath;
    this.module = module;
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

  public List<Library> sort(final List<OriginalLibrary> libraries, final String postfix, final String flexSdkVersion, final boolean isFromSdk) throws IOException {
    useIndexForFindDefinitions = !isFromSdk;

    List<OriginalLibrary> filteredLibraries = new ArrayList<OriginalLibrary>(libraries.size());
    definitionMap = new THashMap<CharSequence, Definition>(1024);

    final CatalogXmlBuilder catalogXmlBuilder = new CatalogXmlBuilder(definitionMap);
    for (OriginalLibrary library : libraries) {
      catalogXmlBuilder.setLibrary(library);
      new XmlBuilderDriver(VfsUtil.loadText(library.getCatalogFile())).build(catalogXmlBuilder);
      if (library.hasDefinitions() || library.hasResourceBundles()) {
        filteredLibraries.add(library);
        if (library.hasUnresolvedDefinitions()) {
          library.unresolvedDefinitionPolicy = library.unresolvedDefinitions.size();
        }
      }
    }

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        analyzeDefinitions();
      }
    });
    
    definitionMap = null;

    final TLinkedList<OriginalLibrary> queue = new TLinkedList<OriginalLibrary>();
    AbcFilter filter = null;
    for (OriginalLibrary library : filteredLibraries) {
      if (!library.hasDefinitions()) {
        if (library.hasResourceBundles()) {
          queue.add(library);
        }
        continue;
      }

      if (library.inDegree == 0) {
        if (library.hasUnresolvedDefinitions()) {
          // if lib has inDegree == 0 and has unresolved defitions, then all library definitions are unresolved
        }
        else {
          queue.add(library);
        }
      }

      Collection<CharSequence> filteredDefinitions = null;
      if (isFromSdk) {
        String path = library.getPath();
        if (path.startsWith("framework")) {
          injectFrameworkSwc(flexSdkVersion, library, libraries);
          continue;
        }
        else if (path.startsWith("airspark")) {
          filteredDefinitions = getBadAirsparkClasses();
          if (!library.hasMissedDefinitions() && library.unresolvedDefinitionPolicy != 0) {
            library.unresolvedDefinitions.addAll(filteredDefinitions);
            filteredDefinitions = library.unresolvedDefinitions;
          }
        }
      }

      if (library.hasUnresolvedDefinitions()) {
        if (library.hasMissedDefinitions()) {
          library.filtered = true;
          if (DebugPathManager.IS_DEV) {
            printCollection(library, postfix);
          }
        }

        filteredDefinitions = library.unresolvedDefinitions;
      }

      final VirtualFile swfFile = library.getSwfFile();
      final File modifiedSwf = library.filtered ? createSwfOutFile(library, postfix) : createSwfOutFile(library);
      final long timeStamp = swfFile.getTimeStamp();
      if (timeStamp != modifiedSwf.lastModified()) {
        if (filter == null) {
          filter = new AbcFilter();
        }
        filter.filter(swfFile, modifiedSwf, filteredDefinitions == null ? null : new AbcNameFilterByNameSet(filteredDefinitions));
        //noinspection ResultOfMethodCallIgnored
        modifiedSwf.setLastModified(timeStamp);
      }
    }

    List<Library> sortedLibraries = new ArrayList<Library>(filteredLibraries.size());
    while (!queue.isEmpty()) {
      OriginalLibrary library = queue.removeFirst();
      assert library.hasDefinitions() || library.hasResourceBundles();
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

  public void printCollection(OriginalLibrary library, String postfix) throws IOException {
    FileWriter writer = new FileWriter(new File(rootPath, library.getPath() + '_' + postfix + "_unresolvedDefinitions.txt"));
    for (CharSequence s : library.unresolvedDefinitions) {
      writer.append(s);
      writer.append('\n');
    }

    writer.flush();
  }

  private void injectFrameworkSwc(String flexSdkVersion, OriginalLibrary library, List<OriginalLibrary> libraries) throws IOException {
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
      Set<CharSequence> definitions = library.hasUnresolvedDefinitions() ? library.unresolvedDefinitions : new THashSet<CharSequence>(8, AbcFilter.HASHING_STRATEGY);
      definitions.add("FrameworkClasses");
      definitions.add("mx.managers.systemClasses:MarshallingSupport");
      definitions.add("mx.managers:SystemManagerProxy");

      definitions.add("mx.styles:StyleManager");
      definitions.add(FlexSdkAbcInjector.LAYOUT_MANAGER);
      definitions.add(FlexSdkAbcInjector.RESOURCE_MANAGER);
      definitions.add(FlexSdkAbcInjector.RESOURCE_MANAGER + "Impl");
      definitions.add("mx.styles:StyleManagerImpl");

      RequiredAssetsInfo requiredAssetsInfo = new RequiredAssetsInfo();
      for (OriginalLibrary originalLibrary : libraries) {
        if (originalLibrary.requiredAssetsInfo != null) {
          requiredAssetsInfo.append(originalLibrary.requiredAssetsInfo);
        }
      }

      new FlexSdkAbcInjector(flexSdkVersion, injectionUrlConnection, requiredAssetsInfo).filter(swfFile, modifiedSwf,
                             new AbcNameFilterByNameSetAndStartsWith(definitions, new String[]{"mx.managers.marshalClasses:"}));
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
          if (dependencyId == null) {
            continue;
          }

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