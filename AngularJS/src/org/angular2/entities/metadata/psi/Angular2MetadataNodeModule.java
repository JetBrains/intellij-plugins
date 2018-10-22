// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.util.Pair.create;

public class Angular2MetadataNodeModule extends Angular2MetadataElement<Angular2MetadataNodeModuleStub> {

  public Angular2MetadataNodeModule(@NotNull Angular2MetadataNodeModuleStub element) {
    super(element);
  }

  @NotNull
  public Pair<PsiFile, TypeScriptClass> locateFileAndClass(String className) {
    VirtualFile sourceFile = getContainingFile().getViewProvider().getVirtualFile();
    VirtualFile parentDir = sourceFile.getParent();
    String sourcePath = getStub().getMemberOrigin(className);
    if (sourcePath == null) {
      sourcePath = StringUtil.trimEnd(sourceFile.getName(), ".metadata.json");
    }
    sourcePath = StringUtil.trimEnd(sourcePath, ".");
    if (!sourcePath.endsWith(".d.ts")) {
      sourcePath += ".d.ts";
    }
    VirtualFile definitionFile = parentDir != null ? parentDir.findFileByRelativePath(sourcePath) : null;
    PsiFile definitionPsi = definitionFile != null ? getManager().findFile(definitionFile) : null;
    if (definitionPsi instanceof JSFile) {
      ResolveResultSink sink = new ResolveResultSink(definitionPsi, className);
      ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));
      if (sink.getResult() instanceof TypeScriptClass) {
        return create(definitionPsi, (TypeScriptClass)sink.getResult());
      }
    }
    return create(definitionPsi, null);
  }

}
