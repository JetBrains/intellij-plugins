// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.Stub;
import com.intellij.util.indexing.FileContent;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetadataJsonStubBuilder implements BinaryFileStubBuilder {

  @Override
  public boolean acceptsFile(@NotNull VirtualFile file) {
    return file.getFileType() == MetadataJsonFileType.INSTANCE;
  }

  @Nullable
  @Override
  public Stub buildStubTree(@NotNull FileContent fileContent) {
    PsiFile psiFile = fileContent.getPsiFile();
    if (!(psiFile instanceof JsonFileImpl)) return null;

    Document document = FileDocumentManager.getInstance().getCachedDocument(fileContent.getFile());
    Project project = fileContent.getProject();
    if (project == null) {
      project = psiFile.getProject();
    }
    if (document != null) {
      PsiFile existingPsi = PsiDocumentManager.getInstance(project).getPsiFile(document);
      if (existingPsi instanceof JsonFileImpl) {
        psiFile = existingPsi;
      }
    }

    JsonFileImpl jsonFile = (JsonFileImpl)psiFile;
    for (Language language: jsonFile.getViewProvider().getLanguages()) {
      if (language instanceof MetadataLanguage) {
        MetadataFileImpl metadataFile = (MetadataFileImpl)jsonFile.getViewProvider().getPsi(language);
        MetadataFileStubImpl result = new MetadataFileStubImpl(metadataFile, ((MetadataLanguage)language).getFileElementType());
        if (jsonFile.getTopLevelValue() != null) {
          ((MetadataLanguage)language).createRootStub(result, jsonFile.getTopLevelValue());
        }
        return result;
      }
    }
    return null;

  }

  @Override
  public int getStubVersion() {
    return 0;
  }
}
