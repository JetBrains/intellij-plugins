package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.impl.JSFileReference;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class InjectedASWriter {
  private static final Logger LOG = Logger.getInstance("#com.intellij.flex.uiDesigner.mxml.MxmlWriter");
  
  private final THashMap<String, ObjectReference> idReferenceMap = new THashMap<String, ObjectReference>();
  private final List<Binding> bindingItems = new ArrayList<Binding>();
  private final BaseWriter writer;
  private ObjectReference lastObjectReference;
  
  private ByteRange declarationsRange;
  
  final static ValueWriter BINDING = new ValueWriter() {
    @Override
    public int write(PrimitiveAmfOutputStream out, boolean isStyle) {
      throw new UnsupportedOperationException();
    }
  }; 

  public InjectedASWriter(BaseWriter writer) {
    this.writer = writer;
  }
  
  public ValueWriter processProperty(XmlElementValueProvider valueProvider, String name, @Nullable String type, boolean isStyle, @Nullable Context context) {
    PsiElement host = valueProvider.getInjectedHost();
    if (host == null) {
      return null;
    }
    
    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(type)) {
      if (checkArray(host, name, isStyle, context) == BINDING) {
        return BINDING;
      }
      else if (valueProvider instanceof XmlAttributeValueProvider) {
        // http://youtrack.jetbrains.net/issue/IDEA-64721
        LOG.warn("unsupported injected AS: " + host.getText());
        return BINDING;
      }
      else {
        return null;
      }
    }
    else {
      return checkObject(host, name, isStyle, context);
    }
  }

  private ValueWriter checkArray(PsiElement host, String name, boolean isStyle, @Nullable Context context) {
    InjectedPsiVisitor visitor = new InjectedPsiVisitor(host, InjectedPsiVisitor.ExpectedType.ARRAY);
    InjectedLanguageUtil.enumerate(host, visitor);
    if (visitor.values != null) {
      bindingItems.add(new ArrayBinding(writer.getObjectOrFactoryId(context), writer.getNameReference(name), visitor.values, isStyle));
      return BINDING;
    }
    else {
      return visitor.isUnsupported();
    }
  }
  
  private ValueWriter checkObject(PsiElement host, String name, boolean isStyle, @Nullable Context context) {
    InjectedPsiVisitor visitor = new InjectedPsiVisitor(host, InjectedPsiVisitor.ExpectedType.OBJECT);
    InjectedLanguageUtil.enumerate(host, visitor);
    if (visitor.values != null) {
      bindingItems.add(new ObjectBinding(writer.getObjectOrFactoryId(context), writer.getNameReference(name), visitor.values[0], isStyle));
      return BINDING;
    }
    else if (visitor.valueWriter != null) {
      return BINDING;
      //return visitor.valueWriter;
    }
    else {
      return visitor.isUnsupported();
    }
  }
  
  public void readDeclarations(MxmlWriter mxmlWriter, XmlTag tag) {
    declarationsRange = writer.getBlockOut().startRange();
    mxmlWriter.processPropertyTagValue(tag, null, true);
    writer.getBlockOut().endRange(declarationsRange);
  }

  public void write() {
    PrimitiveAmfOutputStream out = writer.getOut();
    writeDeclarations();
    writeBinding(out);
    
    reset();
  }
  
  private void writeDeclarations() {
    if (declarationsRange == null) {
      writer.getOut().write(MxmlWriter.EMPTY_CLASS_OR_PROPERTY_NAME);
    }
    else {
      writer.addMarker(declarationsRange);
      declarationsRange = null;
    }
  }

  private void writeBinding(PrimitiveAmfOutputStream out) {
    out.write(bindingItems.size());
    if (bindingItems.isEmpty()) {
      return;
    }

    for (Binding binding : bindingItems) {
      binding.write(out);
    }
  }

  private void writeObjectReference(PrimitiveAmfOutputStream out, String id) {
    ObjectReference reference = idReferenceMap.get(id);
    DeferredInstanceFromObjectReference deferredReference = reference.deferredReference;
    if (deferredReference == null || deferredReference.isWritten()) {
      out.writeUInt29(reference.id << 1);
    }
    else {
      out.writeUInt29((deferredReference.getObjectInstance() << 1) | 1);
      out.writeUInt29(deferredReference.getDeferredParentInstance());
      out.writeUInt29(reference.id);
      
      deferredReference.markAsWritten();
    }
  }

  public void reset() {
    bindingItems.clear();
    idReferenceMap.clear();
    lastObjectReference = null;
  }
  
  void processObjectWithExplicitId(String explicitId, Context context) {
    lastObjectReference = new ObjectReference(writer.getObjectOrFactoryId(context));
    idReferenceMap.put(explicitId, lastObjectReference);
  }

  void setDeferredReferenceForObjectWithExplicitId(DeferredInstanceFromObjectReference deferredReference, int referenceInstance) {
    assert lastObjectReference.id == referenceInstance;
    lastObjectReference.deferredReference = deferredReference;
  }

  private static class InjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
    private final PsiElement host;
    private final ExpectedType expectedType;
    
    private boolean visited;

    private boolean unsupported;
    private String[] values;
    private ValueWriter valueWriter;
    
    enum ExpectedType {
      OBJECT, ARRAY
    }

    public InjectedPsiVisitor(PsiElement host, ExpectedType expectedType) {
      this.host = host;
      this.expectedType = expectedType;
    }
    
    public ValueWriter isUnsupported() {
      return unsupported ? BINDING : null;
    }

    public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
      assert !visited;
      visited = true;

      assert places.size() == 1;
      assert places.get(0).host == host;
      JSFile jsFile = (JSFile) injectedPsi;
      JSSourceElement[] statements = jsFile.getStatements();
      if (statements.length == 0) {
        if (checkEmbed(jsFile)) {
          return;
        }
      }
      
      assert statements.length == 1;
      JSCallExpression expression = (JSCallExpression) ((JSExpressionStatement) statements[0]).getExpression();
      JSExpression[] arguments = expression.getArgumentList().getArguments();
      if (arguments.length == 1) {
        if (arguments[0] instanceof JSArrayLiteralExpression) {
          if (isUnexpected(ExpectedType.ARRAY)) {
            return;
          }
          
          JSExpression[] expressions = ((JSArrayLiteralExpression) arguments[0]).getExpressions();
          
          values = new String[expressions.length];
          for (int i = 0, expressionsLength = expressions.length; i < expressionsLength; i++) {
            JSExpression itemExpression = expressions[i];
            if (itemExpression instanceof JSReferenceExpression && itemExpression.getChildren().length == 0) {
              values[i] = ((JSReferenceExpression) itemExpression).getReferencedName();
            }
            else {
              LOG.warn("unsupported injected AS: " + itemExpression.getText() + " in outer expression " + expression.getText() + " (mxml: " + host.getText() + ")");
              values = null;
              unsupported = true;
              return;
            }
          }
        }
        else if (arguments[0] instanceof JSReferenceExpression && arguments[0].getChildren().length == 0) {
          values = new String[]{((JSReferenceExpression) arguments[0]).getReferencedName()};
        }
        else {
          logUnsupported();
        }
      }
      else {
        logUnsupported();
      }
    }

    private boolean checkEmbed(JSFile jsFile) {
      PsiElement firstChild = jsFile.getFirstChild();
      if (firstChild instanceof LeafPsiElement && ((LeafPsiElement) firstChild).getElementType() == JSTokenTypes.AT) {
        JSAttribute attribute = (JSAttribute) firstChild.getNextSibling();
        assert attribute != null;
        PsiReference[] references = attribute.getValues()[0].getReferences();
        assert references.length == 1;
        JSFileReference fileReference = (JSFileReference) references[0];
        PsiFileSystemItem file = fileReference.resolve();
        assert file != null && !file.isDirectory();
        VirtualFile virtualFile = file.getVirtualFile();
        assert virtualFile != null;
        valueWriter = new BitmapValueWriter(virtualFile, null);  
        return true;
      }
      
      return false;
    }
   
    private boolean isUnexpected(ExpectedType actualType) {
      if (expectedType == actualType) {
        return false;
      }
      else {
        LOG.error("Expected " + expectedType + ", but got " + host.getText());
        unsupported = true;
        return true;
      }
    }

    private void logUnsupported() {
      LOG.warn("unsupported injected AS: " + host.getText());
      unsupported = true;
    }
  }
  
  private static class ObjectReference {
    private final int id;
    DeferredInstanceFromObjectReference deferredReference;

    public ObjectReference(int id) {
      this.id = id;
    }
  }
  
  private abstract static class Binding {
    private final int target;
    private final int propertyName;
    private final boolean isStyle;
    
    protected Binding(int target, int propertyName, boolean isStyle) {
      this.target = target;
      this.propertyName = propertyName;
      this.isStyle = isStyle;
    }

    protected abstract int getType();
    
    void write(PrimitiveAmfOutputStream out) {
      out.writeUInt29(target);
      out.writeUInt29(propertyName);
      out.write(getType());
      out.write(isStyle);
    }
  }
  
  private class ArrayBinding extends Binding {
    private final String[] values;
    
    ArrayBinding(int target, int propertyName, String[] values, boolean isStyle) {
      super(target, propertyName, isStyle);
      this.values = values;
    }

    @Override
    void write(PrimitiveAmfOutputStream out) {
      super.write(out);
      
      out.write(values.length);
      for (String id : values) {
        writeObjectReference(out, id);
      }
    }

    @Override
    protected int getType() {
      return 1;
    }
  }
  
  private class ObjectBinding extends Binding {
    private final String value;
    
    private ObjectBinding(int target, int propertyName, String value, boolean isStyle) {
      super(target, propertyName, isStyle);
      this.value = value;
    }

    @Override
    void write(PrimitiveAmfOutputStream out) {
      super.write(out);
      
      writeObjectReference(out, value);
    }

    @Override
    protected int getType() {
      return 0;
    }
  }
}