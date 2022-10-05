// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.webSymbols.PsiSourcedWebSymbol;
import com.intellij.lang.documentation.DocumentationTarget;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.entities.impl.TypeScriptElementDocumentationTarget;
import org.angular2.web.Angular2Symbol;
import org.angular2.web.Angular2SymbolDelegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.angular2.entities.Angular2DirectiveProperty.hasNonPrivateDocComment;
import static org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX;
import static org.angular2.web.Angular2WebSymbolsRegistryExtension.KIND_NG_DIRECTIVE_IN_OUTS;

public class Angular2DirectiveProperties {

  private final Collection<? extends Angular2DirectiveProperty> myInputs;
  private final Collection<? extends Angular2DirectiveProperty> myOutputs;
  private final NotNullLazyValue<List<? extends Angular2Symbol>> myInOuts =
    NotNullLazyValue.createValue(() -> {
      Collection<? extends Angular2DirectiveProperty> outputs = getOutputs();
      Collection<? extends Angular2DirectiveProperty> inputs = getInputs();

      if (inputs.isEmpty() || outputs.isEmpty()) {
        return Collections.emptyList();
      }
      Map<String, Angular2DirectiveProperty> inputMap = new HashMap<>();
      for (Angular2DirectiveProperty p : inputs) {
        inputMap.putIfAbsent(p.getName(), p);
      }
      List<Angular2Symbol> result = new ArrayList<>();
      for (Angular2DirectiveProperty output : outputs) {
        String name = output.getName();
        if (output.getName().endsWith(OUTPUT_CHANGE_SUFFIX)) {
          Angular2DirectiveProperty input = inputMap.get(
            name.substring(0, name.length() - OUTPUT_CHANGE_SUFFIX.length()));
          if (input != null) {
            result.add(new InOutDirectiveProperty(input, output));
          }
        }
      }
      return result;
    });

  public Angular2DirectiveProperties(Collection<? extends Angular2DirectiveProperty> inputs,
                                     Collection<? extends Angular2DirectiveProperty> outputs) {
    myInputs = Collections.unmodifiableCollection(inputs);
    myOutputs = Collections.unmodifiableCollection(outputs);
  }

  public @NotNull Collection<? extends Angular2DirectiveProperty> getInputs() {
    return myInputs;
  }

  public @NotNull Collection<? extends Angular2DirectiveProperty> getOutputs() {
    return myOutputs;
  }

  public List<? extends Angular2Symbol> getInOuts() {
    return myInOuts.getValue();
  }

  private static class InOutDirectiveProperty extends Angular2SymbolDelegate<Angular2DirectiveProperty>
    implements PsiSourcedWebSymbol {

    private final Angular2DirectiveProperty myOutput;

    private InOutDirectiveProperty(@NotNull Angular2DirectiveProperty input, @NotNull Angular2DirectiveProperty output) {
      super(input);
      myOutput = output;
    }

    @Nullable
    @Override
    public PsiElement getSource() {
      return myOutput.getSource();
    }

    @NotNull
    @Override
    public Pointer<? extends Angular2SymbolDelegate<Angular2DirectiveProperty>> createPointer() {
      var input = getDelegate().createPointer();
      var output = myOutput.createPointer();
      return () -> {
        var newInput = input.dereference();
        var newOutput = output.dereference();
        return newInput != null && newOutput != null ? new InOutDirectiveProperty(newInput, newOutput) : null;
      };
    }

    @NotNull
    @Override
    public String getNamespace() {
      return NAMESPACE_JS;
    }

    @NotNull
    @Override
    public String getKind() {
      return KIND_NG_DIRECTIVE_IN_OUTS;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      InOutDirectiveProperty property = (InOutDirectiveProperty)o;
      return myOutput.equals(property.myOutput)
        && getDelegate().equals(property.getDelegate());
    }

    @Override
    public int hashCode() {
      return Objects.hash(myOutput, getDelegate());
    }

    @Override
    public String toString() {
      return "<" + getDelegate() + "," + myOutput + ">";
    }

    @NotNull
    @Override
    public DocumentationTarget getDocumentationTarget() {
      if (hasNonPrivateDocComment(getDelegate().getSourceElement())){
        return new TypeScriptElementDocumentationTarget(getName(), getDelegate().getSourceElement());
      }
      if (hasNonPrivateDocComment(myOutput.getSourceElement())){
        return new TypeScriptElementDocumentationTarget(getName(), myOutput.getSourceElement());
      }
      var clazz = PsiTreeUtil.getContextOfType(getSource(), TypeScriptClass.class);
      if (clazz != null) {
        return new TypeScriptElementDocumentationTarget(getName(), clazz);
      }
      return super.getDocumentationTarget();
    }
  }

}
