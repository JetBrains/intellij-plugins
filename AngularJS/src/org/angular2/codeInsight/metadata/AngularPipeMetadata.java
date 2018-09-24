// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.metadata;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AngularPipeMetadata {

  @NotNull
  public static AngularPipeMetadata create(@NotNull JSImplicitElement declaration) {
    final JSClass context = PsiTreeUtil.getContextOfType(declaration, JSClass.class);
    if (context != null) {
      return new SourcePipeMetadata(context);
    }
    else if (declaration.getContainingFile() instanceof JsonFile) {
      return new CompiledPipeMetadata(declaration);
    }
    else {
      return new EmptyPipeMetadata();
    }
  }

  private boolean metadataLoaded;
  @Nullable
  protected JSClass myPipeClass;
  @Nullable
  protected JSRecordType.PropertySignature myTransformMethod;

  @Nullable
  public final JSClass getPipeClass() {
    readMetadataIfNeeded();
    return myPipeClass;
  }

  @Nullable
  public final JSRecordType.PropertySignature getTransformMethod() {
    readMetadataIfNeeded();
    readTransformMethodIfNeeded();
    return myTransformMethod;
  }

  protected final void readMetadataIfNeeded() {
    if (!metadataLoaded) {
      metadataLoaded = true;
      readMetadata();
    }
  }

  protected final void readTransformMethodIfNeeded() {
    if (myTransformMethod == null && myPipeClass != null) {
      myTransformMethod = TypeScriptTypeParser
        .buildTypeFromClass(myPipeClass, false)
        .getProperties()
        .stream()
        .filter(prop -> "transform".equals(prop.getMemberName())
                        && prop.getMemberSource().getSingleElement() instanceof TypeScriptFunction)
        .findFirst()
        .orElse(null);
    }
  }

  protected abstract void readMetadata();

  private static class EmptyPipeMetadata extends AngularPipeMetadata {

    @Override
    protected void readMetadata() {
    }
  }

  private static class SourcePipeMetadata extends AngularPipeMetadata {

    private SourcePipeMetadata(@NotNull JSClass aClass) {
      myPipeClass = aClass;
    }

    @Override
    protected void readMetadata() {
    }
  }

  private static class CompiledPipeMetadata extends AngularPipeMetadata {

    @NotNull
    private final JSImplicitElement myDeclaration;

    private CompiledPipeMetadata(@NotNull JSImplicitElement declaration) {
      myDeclaration = declaration;
    }

    @Override
    protected void readMetadata() {
      String className = getClassName();
      if (className == null) {
        return;
      }
      className = StringUtil.trimStart(className, "Deprecated");
      VirtualFile metadataJson = myDeclaration.getContainingFile().getVirtualFile();
      AngularMetadata metadata = AngularMetadataLoader.INSTANCE.load(metadataJson);
      AngularClass pipe = metadata.findClass(className);
      if (pipe == null) {
        return;
      }
      VirtualFile parentDir = metadataJson.getParent();
      String sourcePath = pipe.getSourcePath();
      sourcePath = StringUtil.trimEnd(sourcePath, ".");
      if (!sourcePath.endsWith(".d.ts")) {
        sourcePath += ".d.ts";
      }
      VirtualFile definitionFile = parentDir.findFileByRelativePath(sourcePath);
      PsiFile definitionPsi = definitionFile != null ? myDeclaration.getManager().findFile(definitionFile) : null;
      if (definitionPsi instanceof JSFile) {
        ResolveResultSink sink = new ResolveResultSink(definitionPsi, pipe.getName());
        ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));
        if (sink.getResult() instanceof JSClass) {
          myPipeClass = (JSClass)sink.getResult();
        }
      }
    }

    @Nullable
    private String getClassName() {
      PsiFile metadataPsi = myDeclaration.getContainingFile();
      PsiElement element = metadataPsi.findElementAt(myDeclaration.getTextOffset());
      JsonProperty decorators = (JsonProperty)PsiTreeUtil.findFirstParent(element, el ->
        el instanceof JsonProperty && "decorators".equals(((JsonProperty)el).getName())
      );
      if (decorators == null || !(decorators.getParent() instanceof JsonObject)) {
        return null;
      }
      JsonProperty classDef = ObjectUtils.tryCast(decorators.getParent().getParent(), JsonProperty.class);
      return classDef != null ? classDef.getName() : null;
    }
  }
}
