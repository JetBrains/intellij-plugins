package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
import java.util.List;

public class DartPreFormatProcessor implements PreFormatProcessor {
  public static Key<Boolean> FORMAT_MARK = new Key<Boolean>("FORMAT_MARK");
  public static Boolean FORMAT_MARKER = Boolean.TRUE;

  private CodeStyleSettings mySettings;
  private PsiFile myFile;
  private Project myProject;
  private int selectionOffset;

  @NotNull
  @Override
  public TextRange process(@NotNull ASTNode element, @NotNull TextRange range) {
    // TODO return range if not in a paste operation
    final PsiElement psiElement = element.getPsi();
    if (psiElement == null) return range;

    if (!psiElement.getLanguage().is(DartLanguage.INSTANCE)) return range;

    if (!psiElement.isValid()) return range;
    myFile = psiElement.getContainingFile();
    if (myFile == null || !myFile.isWritable()) return range;

    myProject = psiElement.getProject();
    mySettings = CodeStyleSettingsManager.getSettings(myProject);
    final FileViewProvider viewProvider = myFile.getViewProvider();
    final Document document = viewProvider.getDocument();
    if (document == null) return range;

    final String code = reformat(document.getText(), range);
    if (code == null) return range;
    final RangeMarker marker = document.createRangeMarker(range.getStartOffset(), range.getEndOffset(), true);
    document.replaceString(0, document.getTextLength(), code);

    element.putUserData(FORMAT_MARK, FORMAT_MARKER);
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(myProject);
    // Without this commit, an error is thrown by some tests.
    // With it, some files get garbled.
    documentManager.commitDocument(document);
    return TextRange.create(marker.getStartOffset(), marker.getEndOffset());
  }

  public String reformat(String code, TextRange range) {
    final DartAnalysisServerService.FormatResult formatResult;
    try {
      // While running a paste operation the contents of the VFS are not in sync with the document.
      // Put the document content in a temporary file to be reformatted.
      String parentPath = parentFilePath();
      if (parentPath == null) return null;
      final File tempFile = FileUtil.createTempFile(new File(parentPath), "fmt", ".dart");
      try {
        final Writer str = new BufferedWriter(new FileWriter(tempFile));
        try {
          str.write(code);
          str.flush();
        } finally {
          str.close();
        }
        final String path = FileUtil.toSystemDependentName(tempFile.getPath()); //FileUtil.toSystemDependentName(myFile.getVirtualFile().getPath());
        final int lineLength = mySettings.getCommonSettings(DartLanguage.INSTANCE).RIGHT_MARGIN;
        final DartAnalysisServerService das = DartAnalysisServerService.getInstance();
        formatResult = das.edit_format(path, 0, 0, lineLength);
      } finally {
        FileUtil.delete(tempFile);
      }
    } catch (IOException ex) {
      return null;
    }
    if (formatResult == null) return null;
    final List<SourceEdit> edits = formatResult.getEdits();
    if (edits == null || edits.size() != 1) return null;
    selectionOffset = formatResult.getOffset();
    return edits.get(0).getReplacement(); // possibly null
  }

  private String parentFilePath() {
    final VirtualFile vfile = myFile.getVirtualFile();
    if (vfile == null) {
      return rootFilePath();
    } else {
      VirtualFile parent = vfile.getParent();
      if (parent == null) {
        return rootFilePath();
      } else {
        return vfile.getParent().getCanonicalPath();
      }
    }
  }

  private String rootFilePath() {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) return null;
    // The analysis server has an artificial constraint that files to be formatted must be under an analysis root.
    Collection<Module> modules =  DartSdkGlobalLibUtil.getModulesWithDartSdkGlobalLibAttached(myProject, sdk.getGlobalLibName());
    for (Module module : modules) {
      for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
        return FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntry.getUrl()));
      }
    }
    return null;
  }
}
