package com.intellij.javascript.flex.mxml.schema;

import com.intellij.javascript.flex.FlexResolveHelper;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.*;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlSchemaProvider;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Maxim.Mossienko
 */
public class FlexSchemaHandler extends XmlSchemaProvider implements DumbAware {
  private static final Pattern prefixPattern = Pattern.compile("[a-z_][a-z_0-9]*");

  @Nullable
  public XmlFile getSchema(@NotNull @NonNls final String url, final Module module, @NotNull final PsiFile baseFile) {
    return url.length() > 0 && JavaScriptSupportLoader.isFlexMxmFile(baseFile) ? getFakeSchemaReference(url, module) : null;
  }

  private static final Key<Map<String, ParameterizedCachedValue<XmlFile, Module>>> DESCRIPTORS_MAP_IN_MODULE = Key.create("FLEX_DESCRIPTORS_MAP_IN_MODULE");
  @Nullable
  private static synchronized XmlFile getFakeSchemaReference(final String uri, @Nullable final Module module) {
    if (module == null) {
      return null;
    }

    final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();

    if ((ModuleType.get(module) == FlexModuleType.getInstance() || (sdk != null && sdk.getSdkType() instanceof IFlexSdkType)) ||
        !CodeContext.isStdNamespace(uri)) {

      Map<String, ParameterizedCachedValue<XmlFile, Module>> descriptors = module.getUserData(DESCRIPTORS_MAP_IN_MODULE);
      if (descriptors == null) {
        descriptors = new THashMap<String, ParameterizedCachedValue<XmlFile, Module>>();
        module.putUserData(DESCRIPTORS_MAP_IN_MODULE, descriptors);
      }

      ParameterizedCachedValue<XmlFile, Module> reference = descriptors.get(uri);
      if (reference == null) {
        reference = CachedValuesManager.getManager(module.getProject()).createParameterizedCachedValue(new ParameterizedCachedValueProvider<XmlFile, Module>() {
          @Override
          public CachedValueProvider.Result<XmlFile> compute(Module module) {
            final URL resource = FlexSchemaHandler.class.getResource("z.xsd");
            final VirtualFile fileByURL = VfsUtil.findFileByURL(resource);

              XmlFile result = (XmlFile)PsiManager.getInstance(module.getProject()).findFile(fileByURL).copy();
              result.putUserData(FlexMxmlNSDescriptor.NS_KEY, uri);
              result.putUserData(FlexMxmlNSDescriptor.MODULE_KEY, module);

              return new CachedValueProvider.Result<XmlFile>(result, PsiModificationTracker.MODIFICATION_COUNT);
            }
        }, false);

        descriptors.put(uri, reference);
      }
      assert !module.getProject().isDisposed() : module.getProject() + " already disposed";
      return reference.getValue(module);
    }
    return null;
  }

  @Override
  public boolean isAvailable(final @NotNull XmlFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }

  @NotNull
  @Override
  public Set<String> getAvailableNamespaces(@NotNull XmlFile _file, @NonNls final String tagName) {
    PsiFile originalFile = _file.getOriginalFile();
    if (originalFile instanceof XmlFile) _file = (XmlFile)originalFile;

    final XmlFile file = _file;
    final Project project = file.getProject();
    final Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file.getVirtualFile());
    final Collection<String> illegalNamespaces = getIllegalNamespaces(file);

    final Set<String> result = new THashSet<String>();

    if (tagName == null) {
      for (final String namespace : CodeContextHolder.getInstance(project).getNamespaces(module)) {
        if (!illegalNamespaces.contains(namespace)) {
          result.add(namespace);
        }
      }

      if (!illegalNamespaces.contains(JavaScriptSupportLoader.MXML_URI)) {
        result.add(JavaScriptSupportLoader.MXML_URI);
      }
    }

    if (XmlBackedJSClassImpl.SCRIPT_TAG_NAME.equals(tagName) || "Style".equals(tagName)) return result;

    if (DumbService.isDumb(project)) return result;

    final JavaScriptIndex index = JavaScriptIndex.getInstance(project);

    index.processAllSymbols(new JavaScriptSymbolProcessor.DefaultSymbolProcessor() {

      protected boolean process(final PsiElement namedElement, final JSNamespace namespace) {
        if (namedElement instanceof JSNamedElementProxy) {
          final JSNamedElementIndexItem proxy = ((JSNamedElementProxy)namedElement).getIndexItem();

          if (proxy.getType() == JSNamedElementProxy.NamedItemType.Clazz && proxy.getAccessType() == JSAttributeList.AccessType.PUBLIC) {
            final @NonNls String packageName = proxy.getNamespace().getQualifiedName();
            result.add(getNamespaceForClass(module, packageName, tagName, illegalNamespaces));
          }
        }
        return true;
      }

      public PsiFile getBaseFile() {
        return file;
      }

      public int getRequiredNameId() {
        return index.getIndexOf(tagName);
      }
    });

    final GlobalSearchScope scope = module != null ? GlobalSearchScope.moduleWithDependenciesScope(module) : GlobalSearchScope.projectScope(
      project);
    FlexResolveHelper.processAllMxmlAndFxgFiles(scope, project,
                                                new FlexResolveHelper.MxmlAndFxgFilesProcessor() {
                                                  public void addDependency(final PsiDirectory directory) {
                                                  }

                                                  public boolean processFile(final VirtualFile file, final VirtualFile root) {
                                                    final String packageName = VfsUtilCore.getRelativePath(file.getParent(), root, '.');
                                                    if (packageName != null) {
                                                      result.add(getNamespaceForClass(module, packageName, tagName, illegalNamespaces));
                                                    }
                                                    return true;
                                                  }
                                                }, tagName);

    return result;
  }

  private static Collection<String> getIllegalNamespaces(final XmlFile file) {
    final XmlDocument document = file.getDocument();
    final XmlTag rootTag = document == null ? null : document.getRootTag();
    final String[] knownNamespaces = rootTag == null ? null : rootTag.knownNamespaces();
    final Collection<String> illegalNamespaces = new ArrayList<String>();
    if (knownNamespaces != null) {
      if (ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI, knownNamespaces)) {
        ContainerUtil.addAll(illegalNamespaces, JavaScriptSupportLoader.FLEX_4_NAMESPACES);
      }
      else if (ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, knownNamespaces)) {
        illegalNamespaces.add(JavaScriptSupportLoader.MXML_URI);
      }
    }
    return illegalNamespaces;
  }

  private static String getNamespaceForClass(final Module module,
                                             final String packageName,
                                             final String className,
                                             final Collection<String> illegalNamespaces) {
    if (className != null) {
      final String qName = StringUtil.isEmpty(packageName) ? className : packageName + "." + className;

      for (final String standardNamespace : JavaScriptSupportLoader.MXML_URIS) {
        if (!illegalNamespaces.contains(standardNamespace)) {
          final CodeContext codeContext = CodeContext.getContext(standardNamespace, module);
          if (codeContext.getElementDescriptor(className, qName) != null) {
            return standardNamespace;
          }
        }
      }
    }

    return StringUtil.isEmpty(packageName) ? "*" : packageName + ".*";
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
    if (JavaScriptSupportLoader.MXML_URI4.equals(namespace)) return "s";
    if (JavaScriptSupportLoader.MXML_URI5.equals(namespace)) return "h";
    if (JavaScriptSupportLoader.MXML_URI6.equals(namespace)) return "mx";
    if ("*".equals(namespace)) return "local";

    namespace = FileUtil.toSystemIndependentName(namespace.toLowerCase());
    String prefix = namespace;

    if (namespace.endsWith(".*") && namespace.length() > 2) {
      final String pack = namespace.substring(0, namespace.length() - 2);
      prefix = pack.substring(pack.lastIndexOf('.') + 1);
    }
    else {
      final String schemaMarker = "://";
      int schemaEndIndex = namespace.indexOf(schemaMarker);
      if (schemaEndIndex > 0) {
        String path = namespace.substring(schemaEndIndex + schemaMarker.length());
        if (path.startsWith("www.")) {
          path = path.substring(4);
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

    if (prefix != null && prefixPattern.matcher(prefix).matches()) {
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
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }
}
