package com.jetbrains.lang.dart.psi;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.impl.include.FileIncludeInfo;
import com.intellij.psi.impl.include.FileIncludeProvider;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileContent;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

// pretty much like HtmlFileIncludeProvider, but we do not inherit from it so that Dart provider works in Community Edition
public class DartPackageAwareFileIncludeProvider extends FileIncludeProvider {
  @NotNull
  @Override
  public String getId() {
    return "dart";
  }

  @Override
  public boolean acceptFile(@NotNull final VirtualFile file) {
    return HtmlUtil.isHtmlFile(file);
  }

  @Override
  public void registerFileTypesUsedForIndexing(@NotNull final Consumer<FileType> fileTypeSink) {
    fileTypeSink.consume(StdFileTypes.HTML);
    fileTypeSink.consume(StdFileTypes.XHTML);
  }

  @NotNull
  @Override
  public FileIncludeInfo[] getIncludeInfos(@NotNull final FileContent content) {
    if (PubspecYamlUtil.findPubspecYamlFile(content.getProject(), content.getFile()) == null) return FileIncludeInfo.EMPTY;

    final PsiFile psiFile = content.getPsiFile();
    return psiFile instanceof XmlFile ? getIncludeInfos((XmlFile)psiFile) : FileIncludeInfo.EMPTY;
  }

  private static FileIncludeInfo[] getIncludeInfos(@NotNull final XmlFile xmlFile) {
    final List<FileIncludeInfo> result = new ArrayList<FileIncludeInfo>();

    xmlFile.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override
      public void visitXmlTag(XmlTag tag) {
        final String path = "link".equalsIgnoreCase(tag.getName()) ? getPathRelativeToPackageRoot(tag.getAttributeValue("href")) :
                            "script".equalsIgnoreCase(tag.getName()) ? getPathRelativeToPackageRoot(tag.getAttributeValue("src")) : null;
        if (!StringUtil.isEmptyOrSpaces(path)) {
          result.add(new FileIncludeInfo(path));
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

  @Nullable
  @Override
  public PsiFileSystemItem resolveIncludedFile(@NotNull final FileIncludeInfo info, @NotNull final PsiFile context) {
    final VirtualFile contextFile = DartResolveUtil.getRealVirtualFile(context);
    final VirtualFile pubspecYamlFile = contextFile == null ? null : PubspecYamlUtil.findPubspecYamlFile(context.getProject(), contextFile);
    if (pubspecYamlFile == null) return null;

    VirtualFile targetFile = null;

    final int slashIndex = info.path.indexOf('/');
    if (slashIndex > 0) {
      final String packageName = info.path.substring(0, slashIndex);
      final String relPath = info.path.substring(slashIndex + 1);
      final DartUrlResolver urlResolver = DartUrlResolver.getInstance(context.getProject(), contextFile);
      final VirtualFile packageDir = urlResolver.getPackageDirIfLivePackageOrFromPubListPackageDirs(packageName, relPath);
      if (packageDir != null) {
        targetFile = packageDir.findFileByRelativePath(relPath);
      }
    }

    if (targetFile == null) {
      targetFile = VfsUtilCore.findRelativeFile(PACKAGES_FOLDER_NAME + "/" + info.path, pubspecYamlFile);
    }

    if (targetFile != null) {
      return targetFile.isDirectory() ? context.getManager().findDirectory(targetFile) : context.getManager().findFile(targetFile);
    }

    return null;
  }

  @Nullable
  private static String getPathRelativeToPackageRoot(@Nullable final String path) {
    if (path == null) return null;

    if (path.startsWith(PACKAGES_FOLDER_NAME + "/")) {
      return path.substring(PACKAGES_FOLDER_NAME.length() + 1);
    }

    int index = path.indexOf("/" + PACKAGES_FOLDER_NAME + "/");
    if (index > 0) {
      return path.substring(index + PACKAGES_FOLDER_NAME.length() + 2);
    }

    return null;
  }
}
