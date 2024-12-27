package com.jetbrains.plugins.meteor.imports;

import com.intellij.javascript.JSModuleBaseReference;
import com.intellij.json.psi.*;
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference;
import com.intellij.lang.javascript.frameworks.modules.JSPathMappingsUtil;
import com.intellij.lang.javascript.modules.JSModuleDescriptorFactory;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor;
import com.intellij.lang.javascript.modules.imports.JSModuleDescriptor;
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor;
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.ByteArraySequence;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.gist.GistManager;
import com.intellij.util.gist.VirtualFileGist;
import com.intellij.util.io.ByteSequenceDataExternalizer;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.ide.action.MeteorImportPackagesAsExternalLib.CodeType;
import com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.jetbrains.plugins.meteor.ide.action.MeteorSyntheticLibraryProvider.getPackagesFolder;
import static com.jetbrains.plugins.meteor.ide.action.MeteorSyntheticLibraryProvider.getWrappers;

final class MeteorReferenceContributor implements JSModuleReferenceContributor {
  private static final String METEOR_PREFIX = "meteor/";
  private static final int CACHE_VERSION = 2;

  private final VirtualFileGist<ByteArraySequence> myResolveFileGist =
    GistManager.getInstance().newVirtualFileGist("resolver", CACHE_VERSION, new ByteSequenceDataExternalizer(), (project, el) -> {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(el);
      if (!(psiFile instanceof JsonFile)) return ByteArraySequence.EMPTY;

      JsonValue value = ((JsonFile)psiFile).getTopLevelValue();
      if (!(value instanceof JsonObject)) return ByteArraySequence.EMPTY;

      JsonProperty resources = ((JsonObject)value).findProperty("resources");
      if (resources == null) return ByteArraySequence.EMPTY;
      JsonValue resValue = resources.getValue();
      if (!(resValue instanceof JsonArray)) return ByteArraySequence.EMPTY;
      for (JsonValue jsonValue : ((JsonArray)resValue).getValueList()) {
        if (!(jsonValue instanceof JsonObject resource)) continue;
        JsonProperty options = resource.findProperty("fileOptions");
        if (options == null) continue;
        JsonValue optionsValue = options.getValue();
        if (!(optionsValue instanceof JsonObject)) continue;
        JsonProperty mainModule = ((JsonObject)optionsValue).findProperty("mainModule");
        if (mainModule == null) continue;
        JsonValue mainValue = mainModule.getValue();
        if ((mainValue instanceof JsonBooleanLiteral) && ((JsonBooleanLiteral)mainValue).getValue()) {
          JsonProperty file = resource.findProperty("file");
          if (file == null) return ByteArraySequence.EMPTY;
          JsonValue fileValue = file.getValue();
          if (!(fileValue instanceof JsonStringLiteral)) return ByteArraySequence.EMPTY;
          String result = ((JsonStringLiteral)fileValue).getValue();
          return new ByteArraySequence(result.getBytes(StandardCharsets.UTF_8));
        }
      }
      JsonProperty exports = ((JsonObject)value).findProperty("declaredExports");
      if (exports != null) {
        return parseDeclaredExports(exports);
      }


      return ByteArraySequence.EMPTY;
    });

  private static @NotNull ByteArraySequence parseDeclaredExports(@NotNull JsonProperty exports) {
    JsonValue exportsValue = exports.getValue();
    if (!(exportsValue instanceof JsonArray)) return ByteArraySequence.EMPTY;
    SmartList<String> result = new SmartList<>();
    for (JsonValue jsonValue : ((JsonArray)exportsValue).getValueList()) {
      if (jsonValue instanceof JsonObject) {
        JsonProperty nameProperty = ((JsonObject)jsonValue).findProperty("name");
        if (nameProperty == null) continue;
        JsonValue namePropertyValue = nameProperty.getValue();
        if (namePropertyValue instanceof JsonStringLiteral) {
          result.add(((JsonStringLiteral)namePropertyValue).getValue());
        }
      }
    }
    if (!result.isEmpty()) {
      String text = "#" + StringUtil.join(result, ",");
      return new ByteArraySequence(text.getBytes(StandardCharsets.UTF_8));
    }

    return ByteArraySequence.EMPTY;
  }

  private static final class MeteorGlobalReference extends PsiReferenceBase<PsiElement> implements JSModuleBaseReference {

    private static @NotNull String buildFileWithExportsText(@NotNull Collection<String> exports) {
      StringBuilder text = new StringBuilder();
      for (String s : exports) {
        if (StringUtil.isJavaIdentifier(s)) {
          text.append("export const ").append(s).append(" = global.").append(s).append(";");
        }
      }
      return text.toString();
    }

    private final @Nullable PsiFile myFromText;

    private MeteorGlobalReference(@NotNull PsiElement element, TextRange rangeInElement, @NotNull Collection<String> exports) {
      super(element, rangeInElement, true);

      String text = buildFileWithExportsText(exports);

      myFromText = PsiFileFactory.getInstance(element.getProject())
        .createFileFromText("module.js", JavaScriptSupportLoader.ECMA_SCRIPT_6, text, false, true);
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      return getElement();
    }

    @Override
    public @Nullable PsiElement resolve() {
      return myFromText;
    }
  }

  @Override
  public PsiReference @NotNull [] getAllReferences(@NotNull String unquotedEscapedText,
                                                   @NotNull PsiElement host,
                                                   int offset,
                                                   @Nullable PsiReferenceProvider provider) {
    if (!unquotedEscapedText.startsWith(METEOR_PREFIX)) return PsiReference.EMPTY_ARRAY;
    String importText = unquotedEscapedText.substring(METEOR_PREFIX.length());
    if (StringUtil.isEmpty(importText)) return PsiReference.EMPTY_ARRAY;

    final Collection<CodeType> codes = MeteorPackagesUtil.getCodes(host.getProject());
    if (codes.isEmpty()) return PsiReference.EMPTY_ARRAY;
    List<MeteorPackagesUtil.PackageWrapper> wrappers = getWrappers(host.getProject());
    for (MeteorPackagesUtil.PackageWrapper wrapper : wrappers) {
      String name = wrapper.getOriginalName();
      if (importText.equals(name)) {
        return resolveFor(unquotedEscapedText, host, offset, wrapper, codes);
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private PsiReference[] resolveFor(@NotNull String unquotedEscapedText,
                                    @NotNull PsiElement host,
                                    int offset,
                                    @NotNull MeteorPackagesUtil.PackageWrapper wrapper,
                                    @NotNull Collection<CodeType> codes) {
    VirtualFile packagesFolder = getPackagesFolder(host.getProject());
    VirtualFile versionedPackage = MeteorPackagesUtil.getVersionPackage(packagesFolder, wrapper);
    if (versionedPackage == null) return PsiReference.EMPTY_ARRAY;

    List<String> simpleLinks = new SmartList<>();
    List<String> indirectLinks = new SmartList<>();
    String name = wrapper.getName();
    String shortName = getShortName(wrapper);
    SmartList<PsiReference> result = new SmartList<>();
    Collection<String> globalNames = new HashSet<>();

    TextRange range = TextRange.create(offset, offset + unquotedEscapedText.length());
    for (CodeType code : codes) {
      String prefix = code.getFolder();
      VirtualFile jsonFile = versionedPackage.findChild(prefix + ".json");
      boolean added = false;
      if (jsonFile != null) {
        ByteArraySequence data = myResolveFileGist.getFileData(host.getProject(), jsonFile);
        String path = data == ByteArraySequence.EMPTY ? "" : new String(data.toBytes(), StandardCharsets.UTF_8);
        if (!StringUtil.isEmpty(path)) {
          if (path.startsWith("#")) {
            globalNames.addAll(StringUtil.split(StringUtil.trimStart(path, "#"), ","));
          }
          else {
            simpleLinks.add(JSPathMappingsUtil.getStringPathRelativeBaseUrlOrSelfIfAbsolute(versionedPackage, path));
          }
          added = true;
        }
      }
      if (!added) {
        VirtualFile directory = versionedPackage.findChild(prefix);
        if (directory != null) {
          VirtualFile candidate = getChildWithName(name, directory);
          if (candidate == null && shortName != null) {
            candidate = getChildWithName(shortName, directory);
          }
          if (candidate != null) {
            indirectLinks.add(candidate.getPath());
          }
        }
      }
    }

    if (!globalNames.isEmpty()) {
      result.add(new MeteorGlobalReference(host, range, globalNames));
    }

    if (simpleLinks.isEmpty()) {
      simpleLinks.addAll(indirectLinks);
    }

    result.add(new JSExactFileReference(host, range, simpleLinks, MeteorPackagesUtil.EXTENSIONS) {

      @Override
      protected boolean acceptAll() {
        return true;
      }

      @Override
      public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return element;
      }
    });

    return result.toArray(PsiReference.EMPTY_ARRAY);
  }

  private static @Nullable String getShortName(@NotNull MeteorPackagesUtil.PackageWrapper wrapper) {
    String originalName = wrapper.getOriginalName();
    int sepIndex = originalName.indexOf(":");
    if (sepIndex > 0) {
      return originalName.substring(sepIndex + 1);
    }

    return null;
  }

  private static @Nullable VirtualFile getChildWithName(@NotNull String name, @NotNull VirtualFile parent) {
    for (String extension : MeteorPackagesUtil.EXTENSIONS) {
      VirtualFile candidate = parent.findChild(name + extension);
      if (candidate != null) {
        return candidate;
      }
    }

    return null;
  }

  @Override
  public boolean isApplicable(@NotNull PsiElement host) {
    Project project = host.getProject();
    return !DumbService.isDumb(project) && MeteorFacade.getInstance().isMeteorProject(project) && DialectDetector.isJavaScript(host);
  }

  @Override
  public @NotNull List<JSImportDescriptor> getAdditionalDescriptors(@NotNull JSImportPathConfiguration configuration,
                                                                    @NotNull JSImportDescriptor baseDescriptor) {
    JSModuleDescriptor module = baseDescriptor.getModuleDescriptor();
    if (!(module instanceof JSModuleNameInfo)) return Collections.emptyList();
    VirtualFile resolvedModuleFile = ((JSModuleNameInfo)module).getResolvedFile();
    Project project = configuration.getProject();
    PsiElement place = configuration.getPlace();
    ProjectFileIndex index = ProjectFileIndex.getInstance(project);
    if (!index.isInLibrary(resolvedModuleFile)) {
      return Collections.emptyList();
    }
    VirtualFile packagesFolder = getPackagesFolder(project);
    if (packagesFolder == null) return Collections.emptyList();

    if (!VfsUtilCore.isAncestor(packagesFolder, resolvedModuleFile, true)) return Collections.emptyList();

    VirtualFile prev = null;
    VirtualFile parent = resolvedModuleFile.getParent();
    while (parent != null) {
      if (parent.getName().equals("packages")) break;
      prev = parent;
      parent = parent.getParent();
    }
    if (parent == null || prev == null) return Collections.emptyList();

    String name = prev.getName();
    if (!name.contains("_")) {
      return Collections
        .singletonList(
          new JSSimpleImportDescriptor(JSModuleDescriptorFactory.createModuleDescriptor(METEOR_PREFIX + name, prev, place), baseDescriptor));
    }

    List<MeteorPackagesUtil.PackageWrapper> wrappers = getWrappers(project);
    for (MeteorPackagesUtil.PackageWrapper wrapper : wrappers) {
      if (wrapper.getName().equals(name)) {
        return Collections.singletonList(
          new JSSimpleImportDescriptor(JSModuleDescriptorFactory.createModuleDescriptor(METEOR_PREFIX + wrapper.getOriginalName(), prev, place),
                                       baseDescriptor));
      }
    }

    return Collections.emptyList();
  }

  @Override
  public @NotNull Collection<?> getDependencies(@NotNull String unquoted, @NotNull PsiElement host) {
    //should be enough ProjectRootManager.getInstance(host.getProject()) from getAllDependencies
    return JSModuleReferenceContributor.super.getDependencies(unquoted, host);
  }
}
