// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.metadata;

import com.intellij.json.psi.JsonFile;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableCollection;

public abstract class AngularDirectiveMetadata {

  @NotNull
  public static AngularDirectiveMetadata create(@NotNull JSImplicitElement declaration) {
    final JSClass context = PsiTreeUtil.getContextOfType(declaration, JSClass.class);
    if (context != null) {
      return new SourceDirectiveMetadata(context);
    } else if (declaration.getContainingFile() instanceof JsonFile) {
      return new CompiledDirectiveMetadata(declaration);
    } else {
      return new EmptyDirectiveMetadata();
    }
  }

  private Collection<PropertyInfo> myInputs;
  private Collection<PropertyInfo> myOutputs;

  @Nullable
  public abstract JSClass getDirectiveClass();

  @NotNull
  public Collection<PropertyInfo> getInputs() {
    readMetadataIfNeeded();
    return myInputs;
  }

  @NotNull
  public Collection<PropertyInfo> getOutputs() {
    readMetadataIfNeeded();
    return myOutputs;
  }

  protected final void readMetadataIfNeeded() {
    if (myInputs == null) {
      myInputs = new ArrayList<>();
      myOutputs = new ArrayList<>();
      readMetadata(myInputs, myOutputs);
      myInputs = unmodifiableCollection(myInputs);
      myOutputs = unmodifiableCollection(myOutputs);
    }
  }

  protected abstract void readMetadata(@NotNull Collection<PropertyInfo> inputs,
                                       @NotNull Collection<PropertyInfo> outputs);

  public static class PropertyInfo {
    @Nullable
    public final JSRecordType.PropertySignature signature;
    @NotNull
    public final PsiElement source;
    @NotNull
    public final String name;

    public PropertyInfo(@Nullable JSRecordType.PropertySignature signature, @NotNull PsiElement source, @NotNull String name) {
      this.signature = signature;
      this.source = source;
      this.name = name;
    }
  }

  private static class EmptyDirectiveMetadata extends AngularDirectiveMetadata {

    @Nullable
    @Override
    public JSClass getDirectiveClass() {
      return null;
    }

    @Override
    protected void readMetadata(@NotNull Collection<PropertyInfo> inputs, @NotNull Collection<PropertyInfo> outputs) {
    }

  }

  private static class SourceDirectiveMetadata extends AngularDirectiveMetadata {

    @NotNull
    private final JSClass myDirectiveClass;

    private SourceDirectiveMetadata(@NotNull JSClass aClass) {
      myDirectiveClass = aClass;
    }

    @NotNull
    @Override
    public JSClass getDirectiveClass() {
      return myDirectiveClass;
    }

    @Override
    protected void readMetadata(@NotNull Collection<PropertyInfo> inputs,
                                @NotNull Collection<PropertyInfo> outputs) {
      TypeScriptTypeParser
        .buildTypeFromClass(myDirectiveClass, false)
        .getProperties()
        .forEach(prop -> {
          if (prop.getMemberSource().getSingleElement() instanceof JSAttributeListOwner) {
            readDecorator(prop, (JSAttributeListOwner)prop.getMemberSource().getSingleElement(), "Input", inputs);
            readDecorator(prop, (JSAttributeListOwner)prop.getMemberSource().getSingleElement(), "Output", outputs);
          }
        });
    }

    private static void readDecorator(@NotNull JSRecordType.PropertySignature prop, @NotNull JSAttributeListOwner element, @NotNull String decoratorName, @NotNull Collection<PropertyInfo> destination) {
      String decoratedName = getDecoratedName(element, decoratorName);
      if (decoratedName != null) {
        destination.add(new PropertyInfo(prop, element, decoratedName));
      }
    }

    private static String getDecoratedName(@NotNull JSAttributeListOwner field, @NotNull String name) {
      final JSAttributeList list = field.getAttributeList();
      if (list != null) {
        for (PsiElement candidate : list.getChildren()) {
          if (candidate instanceof ES6Decorator) {
            final PsiElement child = candidate.getLastChild();
            if (child instanceof JSCallExpression) {
              final JSExpression expression = ((JSCallExpression)child).getMethodExpression();
              if (expression instanceof JSReferenceExpression &&
                  JSSymbolUtil.isAccurateReferenceExpressionName((JSReferenceExpression)expression, name)) {
                JSExpression[] arguments = ((JSCallExpression)child).getArguments();
                if (arguments.length > 0 && arguments[0] instanceof JSLiteralExpression) {
                  String value = ((JSLiteralExpression)arguments[0]).getStringValue();
                  if (value != null) return value;
                }
                return field.getName();
              }
            }
          }
        }
      }
      return null;
    }

  }

  private static class CompiledDirectiveMetadata extends AngularDirectiveMetadata {

    @NotNull
    private final JSImplicitElement myDeclaration;
    private JSClass myDirectiveClass;
    private JSRecordType myDirectiveClassType;

    private CompiledDirectiveMetadata(@NotNull JSImplicitElement declaration) {
      myDeclaration = declaration;
    }

    @Nullable
    @Override
    public JSClass getDirectiveClass() {
      readMetadataIfNeeded();
      return myDirectiveClass;
    }

    @Override
    protected void readMetadata(@NotNull Collection<PropertyInfo> inputs,
                                @NotNull Collection<PropertyInfo> outputs) {
      VirtualFile metadataJson = myDeclaration.getContainingFile().getVirtualFile();
      AngularMetadata metadata = AngularMetadataLoader.INSTANCE.load(metadataJson);

      String directiveName = StringUtil.trimStart(myDeclaration.getName(), "*");
      List<AngularClass> directives= metadata.findDirectives(directiveName);
      if (directives.isEmpty()) {
        return;
      }
      AngularClass directive = directives.get(0);
      VirtualFile parentDir = metadataJson.getParent();
      String sourcePath = directive.getSourcePath();
      if (!sourcePath.endsWith(".d.ts")) {
        sourcePath += ".d.ts";
      }
      VirtualFile definitionFile = parentDir.findFileByRelativePath(sourcePath);
      PsiFile definitionPsi = definitionFile != null ? myDeclaration.getManager().findFile(definitionFile) : null;
      if (definitionPsi instanceof JSFile) {
        ResolveResultSink sink = new ResolveResultSink(definitionPsi, directive.getName());
        ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));
        if (sink.getResult() instanceof  JSClass) {
          myDirectiveClass = (JSClass)sink.getResult();
          myDirectiveClassType = TypeScriptTypeParser
            .buildTypeFromClass(myDirectiveClass, false);
        }
      }
      stream(directive.getInputs()).map(this::locateField).forEach(inputs::add);
      stream(directive.getOutputs()).map(this::locateField).forEach(outputs::add);
    }

    private PropertyInfo locateField(AngularField field) {
      if (myDirectiveClassType != null) {
        JSRecordType.PropertySignature sig = myDirectiveClassType.findPropertySignature(field.getName());
        if (sig != null) {
          PsiElement source = sig.getMemberSource().getSingleElement();
          return new PropertyInfo(sig, source != null ? source : myDeclaration, field.getName());
        }
      }
      return new PropertyInfo(null, myDeclaration, field.getName());
    }
  }

}
