package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.ProblemsHolder;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class InjectedASWriter implements ValueReferenceResolver {
  private final THashMap<String, MxmlObjectReference> idReferenceMap = new THashMap<String, MxmlObjectReference>();
  private final THashMap<JSVariable, VariableReference> variableReferenceMap = new THashMap<JSVariable, VariableReference>();

  private final List<Binding> bindingItems = new ArrayList<Binding>();
  private final BaseWriter writer;

  private final ProblemsHolder problemsHolder;

  private ByteRange declarationsRange;

  final static ValueWriter IGNORE = new ValueWriter() {
    @Override
    public PropertyProcessor.PropertyKind write(AnnotationBackedDescriptor descriptor, XmlElementValueProvider valueProvider,
                                                PrimitiveAmfOutputStream out, BaseWriter writer,
                                                boolean isStyle, Context parentContext) {
      throw new UnsupportedOperationException();
    }
  };

  public InjectedASWriter(BaseWriter writer, ProblemsHolder problemsHolder) {
    this.writer = writer;
    this.problemsHolder = problemsHolder;
  }

  @NotNull
  @Override
  public VariableReference getValueReference(JSVariable jsVariable) {
    return variableReferenceMap.get(jsVariable);
  }

  @Override
  public VariableReference getNullableValueReference(JSVariable jsVariable) {
    return variableReferenceMap.get(jsVariable);
  }

  @NotNull
  @Override
  public MxmlObjectReference getValueReference(String id) throws InvalidPropertyException {
    return idReferenceMap.get(id);
  }

  public ValueWriter processProperty(XmlElementValueProvider valueProvider, String name, @Nullable String type, boolean isStyle,
                                       @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider) throws InvalidPropertyException {
    final XmlElement host = valueProvider.getInjectedHost();
    return host == null ? null : processProperty(host, name, type, isStyle, mxmlObjectReferenceProvider);
  }

  public ValueWriter processProperty(PsiElement host, String name, @Nullable String type, boolean isStyle, @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider)
    throws InvalidPropertyException {
    final InjectedPsiVisitor visitor = new InjectedPsiVisitor(host, type, problemsHolder);
    InjectedLanguageUtil.enumerate(host, visitor);

    //noinspection ThrowableResultOfMethodCallIgnored
    if (visitor.getInvalidPropertyException() != null) {
      throw visitor.getInvalidPropertyException();
    }
    else {
      final Binding binding = visitor.getBinding();
      if (binding != null) {
        if (binding instanceof VariableBinding) {
          JSVariable variable = ((VariableBinding)binding).variable;
          VariableReference variableReference = variableReferenceMap.get(variable);
          if (variableReference == null) {
            variableReferenceMap.put(variable, new VariableReference(variable));
          }
          else {
            variableReference.markAsMultipleReferred();
          }
        }

        binding.setTarget(mxmlObjectReferenceProvider.getMxmlObjectReference(), writer.getNameReference(name), isStyle);
        bindingItems.add(binding);
        return IGNORE;
      }
      else {
        return visitor.getValueWriter();
      }
    }
  }

  public void readDeclarations(MxmlWriter mxmlWriter, XmlTag tag) {
    declarationsRange = writer.getBlockOut().startRange();
    mxmlWriter.processDeclarations(tag);
    writer.getBlockOut().endRange(declarationsRange);
  }

  public void write() {
    writeDeclarations();
    writeBinding(writer.getOut());
  }

  private void writeDeclarations() {
    if (declarationsRange == null) {
      writer.getOut().writeShort(0);
    }
    else {
      writer.addMarker(declarationsRange);
      declarationsRange = null;
    }
  }

  private void writeBinding(PrimitiveAmfOutputStream out) {
    if (bindingItems.isEmpty()) {
      out.writeShort(0);
      return;
    }
    
    final int bindingSizePosition = out.allocateShort();
    int size = bindingItems.size();
    
    for (Binding binding : bindingItems) {
      int beforePosition = out.size();
      try {
        binding.write(out, writer, this);
        continue;
      }
      catch (UnsupportedOperationException e) {
        MxmlWriter.LOG.warn("unsupported injected AS: " + e.getMessage());
      }
      catch (Throwable e) {
        problemsHolder.add(e);
      }
      
      size--;
      out.getByteOut().setPosition(beforePosition);
    }

    out.putShort(size, bindingSizePosition);
  }

  void putMxmlObjectReference(@NotNull String explicitId, @NotNull MxmlObjectReference mxmlObjectReference) {
    idReferenceMap.put(explicitId, mxmlObjectReference);
  }
}