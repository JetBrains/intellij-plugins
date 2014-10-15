package com.jetbrains.lang.dart.psi;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeInfo;
import com.intellij.psi.impl.include.FileIncludeProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.IndexingDataKeys;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

/**
 * @author Dennis.Ushakov
 */
public class DartFileReferenceHelper extends FileReferenceHelper {
  @NotNull
  @Override
  public Collection<PsiFileSystemItem> getContexts(final Project project, @NotNull VirtualFile file) {
    final DartUrlResolver resolver = DartUrlResolver.getInstance(project, file);
    final PsiManager psiManager = PsiManager.getInstance(project);

    final Set<PsiFileSystemItem> result = new LinkedHashSet<PsiFileSystemItem>(ContainerUtil.mapNotNull(resolver.getPackageRoots(), new NullableFunction<VirtualFile, PsiFileSystemItem>() {
        @Override
        public PsiFileSystemItem fun(VirtualFile file) {
          return file.isValid() ? psiManager.findDirectory(file) : null;
        }
      }));

    final VirtualFile pubspec = resolver.getPubspecYamlFile();
    final VirtualFile projectRoot = pubspec != null ? pubspec.getParent() : null;
    if (projectRoot != null) {
      final VirtualFile packages = projectRoot.findChild(PACKAGES_FOLDER_NAME);
      ContainerUtil.addIfNotNull(result, packages != null ? psiManager.findDirectory(packages) : null);
    }

    return result;
  }

  @Override
  public boolean isMine(Project project, @NotNull VirtualFile file) {
    return hasDart(project, file);
  }

  public static boolean hasDart(final Project project, final VirtualFile file) {
    DartUrlResolver dartResolver = DartUrlResolver.getInstance(project, file);
    return dartResolver.getPubspecYamlFile() != null || dartResolver.getPackageRoots().length > 0;
  }

  public static class DartPackageAwareFileIncludeProvider extends FileIncludeProvider {
    @NotNull
    @Override
    public String getId() {
      return "dart";
    }

    @Override
    public boolean acceptFile(VirtualFile file) {
      return HtmlUtil.isHtmlFile(file);
    }

    @Override
    public void registerFileTypesUsedForIndexing(@NotNull Consumer<FileType> fileTypeSink) {
      fileTypeSink.consume(StdFileTypes.HTML);
      fileTypeSink.consume(StdFileTypes.XHTML);
    }

    @NotNull
    @Override
    public FileIncludeInfo[] getIncludeInfos(FileContent content) {
      PsiFile psiFile = content.getPsiFile();
      if (psiFile instanceof XmlFile) {
        return getIncludeInfos((XmlFile)psiFile);
      }
      return FileIncludeInfo.EMPTY;
    }

    private static FileIncludeInfo[] getIncludeInfos(final XmlFile xmlFile) {
      final VirtualFile file = xmlFile.getUserData(IndexingDataKeys.VIRTUAL_FILE);
      if (!hasDart(xmlFile.getProject(), file != null ? file : xmlFile.getVirtualFile())) return FileIncludeInfo.EMPTY;

      final List<FileIncludeInfo> result = new ArrayList<FileIncludeInfo>();
      xmlFile.acceptChildren(new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlTag(XmlTag tag) {
          final String url;
          url = "link".equalsIgnoreCase(tag.getName()) ? cleanupPackage(tag.getAttributeValue("href")) :
                "script".equalsIgnoreCase(tag.getName()) ? cleanupPackage(tag.getAttributeValue("src")) : null;
          if (!StringUtil.isEmpty(url)) {
            result.add(new FileIncludeInfo(url));
          }

          super.visitXmlTag(tag);
        }

        @Override
        public void visitElement(PsiElement element) {
          if (element.getLanguage() instanceof XMLLanguage) {
            super.visitElement(element);
          }
        }
      });
      return ContainerUtil.toArray(result, FileIncludeInfo.EMPTY);
    }

    private static String cleanupPackage(String href) {
      if (href == null) return null;
      final String[] path = href.split("(/|\\\\)");
      for (int i = 0; i < path.length; i++) {
        String dir = path[i];
        if (PACKAGES_FOLDER_NAME.equals(dir) && i < path.length - 1) {
          return StringUtil.join(path, i + 1, path.length, "/");
        }
      }
      return null;
    }
  }
}
