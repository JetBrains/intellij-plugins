// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.FlexResolveHelper;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JSPackageIndex;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public final class FlexSchemaHandler extends XmlSchemaProvider implements DumbAware {
  private static final Pattern prefixPattern = Pattern.compile("[a-z_][a-z_0-9]*");

  @Override
  @Nullable
  public XmlFile getSchema(@NotNull @NonNls final String url, final Module module, @NotNull final PsiFile baseFile) {
    return url.length() > 0 && JavaScriptSupportLoader.isFlexMxmFile(baseFile)
           ? getFakeSchemaReference(url, module, baseFile.getResolveScope())
           : null;
  }

  private static final Key<Map<String, ParameterizedCachedValue<XmlFile, Pair<Module, GlobalSearchScope>>>> DESCRIPTORS_MAP_IN_MODULE =
    Key.create("FLEX_DESCRIPTORS_MAP_IN_MODULE");

  @Nullable
  private static synchronized XmlFile getFakeSchemaReference(String uri, @Nullable Module module, @NotNull GlobalSearchScope scope) {
    if (module == null) {
      return null;
    }

    if (ModuleType.get(module) == FlexModuleType.getInstance() || !CodeContext.isStdNamespace(uri)) {
      Map<String, ParameterizedCachedValue<XmlFile, Pair<Module, GlobalSearchScope>>> descriptors = module.getUserData(DESCRIPTORS_MAP_IN_MODULE);
      if (descriptors == null) {
        descriptors = new HashMap<>();
        module.putUserData(DESCRIPTORS_MAP_IN_MODULE, descriptors);
      }

      ParameterizedCachedValue<XmlFile, Pair<Module, GlobalSearchScope>> reference = descriptors.get(uri);
      if (reference == null) {
        reference = CachedValuesManager.getManager(module.getProject())
          .createParameterizedCachedValue(pair -> {
            final URL resource = FlexSchemaHandler.class.getResource("z.xsd");
            final VirtualFile fileByURL = VfsUtil.findFileByURL(resource);

            XmlFile result = (XmlFile)PsiManager.getInstance(pair.first.getProject()).findFile(fileByURL).copy();
            result.putUserData(FlexMxmlNSDescriptor.NS_KEY, uri);
            result.putUserData(FlexMxmlNSDescriptor.MODULE_KEY, pair.first);
            result.putUserData(FlexMxmlNSDescriptor.SCOPE_KEY, pair.second);

            return new CachedValueProvider.Result<>(result, PsiModificationTracker.MODIFICATION_COUNT);
          }, false);

        descriptors.put(uri, reference);
      }
      assert !module.getProject().isDisposed() : module.getProject() + " already disposed";
      return reference.getValue(Pair.create(module, scope));
    }
    return null;
  }

  @Override
  public boolean isAvailable(final @NotNull XmlFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }

  @NotNull
  @Override
  public Set<String> getAvailableNamespaces(@NotNull XmlFile _file, @Nullable @NonNls final String tagName) {
    // tagName == null => tag name completion
    // tagName != null => guess namespace of unresolved tag

    PsiFile originalFile = _file.getOriginalFile();
    if (originalFile instanceof XmlFile) _file = (XmlFile)originalFile;

    final XmlFile file = _file;
    final Project project = file.getProject();
    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file.getVirtualFile());
    final Collection<String> illegalNamespaces = getIllegalNamespaces(file);

    final Set<String> result = new HashSet<>();
    final Set<String> componentsThatHaveNotPackageBackedNamespace = new HashSet<>();

    for (final String namespace : CodeContextHolder.getInstance(project).getNamespaces(module, _file.getResolveScope())) {
      if (!CodeContext.isPackageBackedNamespace(namespace) && !illegalNamespaces.contains(namespace)) {
        // package backed namespaces will be added later from JSPackageIndex
        if (tagName == null) {
          result.add(namespace);
        }
        else {
          final XmlElementDescriptor descriptor = CodeContext.getContext(namespace, module).getElementDescriptor(tagName, (XmlTag)null);
          if (descriptor != null) {
            result.add(namespace);
            componentsThatHaveNotPackageBackedNamespace.add(descriptor.getQualifiedName());
          }
        }
      }
    }

    if (tagName == null && !illegalNamespaces.contains(JavaScriptSupportLoader.MXML_URI)) {
      result.add(JavaScriptSupportLoader.MXML_URI);
    }

    if (XmlBackedJSClassImpl.SCRIPT_TAG_NAME.equals(tagName) || "Style".equals(tagName)) return result;

    if (DumbService.isDumb(project)) return result;

    if (tagName == null) {
      FileBasedIndex.getInstance().processAllKeys(JSPackageIndex.INDEX_ID, packageName -> {
        // packages that don't contain suitable classes will be filtered out
        // in DefultXmlExtension.getAvailableTagNames -> TagNameReference.getTagNameVariants()
        result.add(StringUtil.isEmpty(packageName) ? "*" : packageName + ".*");
        return true;
      }, project);
    }
    else {
      final GlobalSearchScope scopeWithLibs = module != null
                                              ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
                                              : GlobalSearchScope.allScope(project);
      for (JSQualifiedNamedElement element : JSResolveUtil.findElementsByName(tagName, project, scopeWithLibs, false)) {
        if (element instanceof JSClass && CodeContext.hasDefaultConstructor((JSClass)element)) {
          final String packageName = StringUtil.getPackageName(element.getQualifiedName());
          if (!componentsThatHaveNotPackageBackedNamespace.contains(StringUtil.getQualifiedName(packageName, tagName))) {
            result.add(StringUtil.isEmpty(packageName) ? "*" : packageName + ".*");
          }
        }
      }
    }

    final GlobalSearchScope scopeWithoutLibs = module != null
                                               ? GlobalSearchScope.moduleWithDependenciesScope(module)
                                               : GlobalSearchScope.allScope(project);

    // packages that contain *.mxml files and do not contain *.as are not retrieved from JSPackageIndex
    FlexResolveHelper
      .processAllMxmlAndFxgFiles(scopeWithoutLibs, project,
                                 new FlexResolveHelper.MxmlAndFxgFilesProcessor() {
                                   @Override
                                   public void addDependency(final PsiDirectory directory) {
                                   }

                                   @Override
                                   public boolean processFile(final VirtualFile file, final VirtualFile root) {
                                     if (tagName == null || tagName.equals(file.getNameWithoutExtension())) {
                                       final String packageName = VfsUtilCore.getRelativePath(file.getParent(), root, '.');
                                       if (packageName != null &&
                                           (tagName == null || !componentsThatHaveNotPackageBackedNamespace
                                             .contains(StringUtil.getQualifiedName(packageName, tagName)))) {
                                         result.add(StringUtil.isEmpty(packageName) ? "*" : packageName + ".*");
                                       }
                                     }
                                     return true;
                                   }
                                 },
                                 tagName);

    return result;
  }

  private static Collection<String> getIllegalNamespaces(final XmlFile file) {
    final XmlDocument document = file.getDocument();
    final XmlTag rootTag = document == null ? null : document.getRootTag();
    final String[] knownNamespaces = rootTag == null ? null : rootTag.knownNamespaces();
    final Collection<String> illegalNamespaces = new ArrayList<>();
    if (knownNamespaces != null) {
      if (ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI, knownNamespaces)) {
        ContainerUtil.addAll(illegalNamespaces, MxmlJSClass.FLEX_4_NAMESPACES);
      }
      else if (ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, knownNamespaces)) {
        illegalNamespaces.add(JavaScriptSupportLoader.MXML_URI);
      }
    }
    return illegalNamespaces;
  }

  @Override
  public String getDefaultPrefix(@NotNull @NonNls final String namespace, @NotNull final XmlFile context) {
    return getUniquePrefix(namespace, context);
  }

  static String getUniquePrefix(final String namespace, final XmlFile xmlFile) {
    String prefix = getDefaultPrefix(namespace);

    final XmlDocument document = xmlFile.getDocument();
    final XmlTag tag = document == null ? null : document.getRootTag();
    final String[] knownPrefixes = getKnownPrefixes(tag);

    if (ArrayUtil.contains(prefix, knownPrefixes)) {
      for (int i = 2; ; i++) {
        final String newPrefix = prefix + i;
        if (!ArrayUtil.contains(newPrefix, knownPrefixes)) {
          prefix = newPrefix;
          break;
        }
      }
    }

    return prefix;
  }

  public static String getDefaultPrefix(@NotNull @NonNls String namespace) {
    if (JavaScriptSupportLoader.MXML_URI.equals(namespace)) return "mx";
    if (JavaScriptSupportLoader.MXML_URI3.equals(namespace)) return "fx";
    if (MxmlJSClass.MXML_URI4.equals(namespace)) return "s";
    if (MxmlJSClass.MXML_URI5.equals(namespace)) return "h";
    if (MxmlJSClass.MXML_URI6.equals(namespace)) return "mx";
    if ("*".equals(namespace)) return "local";

    namespace = FileUtil.toSystemIndependentName(StringUtil.toLowerCase(namespace));
    String prefix = namespace;

    if (namespace.endsWith(".*") && namespace.length() > 2) {
      final String pack = namespace.substring(0, namespace.length() - 2);
      prefix = pack.substring(pack.lastIndexOf('.') + 1);
    }
    else {
      final String schemaMarker = "://";
      int schemaMarkerIndex = namespace.indexOf(schemaMarker);
      if (schemaMarkerIndex > 0) {
        String path = namespace.substring(schemaMarkerIndex + schemaMarker.length());
        path = StringUtil.trimStart(path, "www.");
        path = StringUtil.trimEnd(path, "/");

        final String lastSegment = path.substring(path.lastIndexOf('/') + 1);
        if (prefixPattern.matcher(lastSegment).matches()) {
          return lastSegment;
        }

        final int dotIndex = path.indexOf('.');
        final int slashIndex = path.indexOf('/');
        final int endIndex = (dotIndex == -1)
                             ? (slashIndex == -1 ? path.length() : slashIndex)
                             : (slashIndex == -1 ? dotIndex : Math.min(dotIndex, slashIndex));
        if (path.length() > 0 && endIndex > 0) {
          prefix = path.substring(0, endIndex);
        }
      }
    }

    if (prefixPattern.matcher(prefix).matches()) {
      return prefix;
    }

    return "undefined";
  }

  private static String[] getKnownPrefixes(final XmlTag tag) {
    final String[] namespaces = tag == null ? null : tag.knownNamespaces();
    if (namespaces != null && namespaces.length > 0) {
      final String[] knownPrefixes = new String[namespaces.length];
      for (int i = 0; i < namespaces.length; i++) {
        knownPrefixes[i] = tag.getPrefixByNamespace(namespaces[i]);
      }
      return knownPrefixes;
    }
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }
}
