package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class InjectedASWriter {
  private static final Logger LOG = Logger.getInstance(InjectedASWriter.class.getName());

  private final THashMap<String, ObjectReference> idReferenceMap = new THashMap<String, ObjectReference>();
  private final List<Binding> bindingItems = new ArrayList<Binding>();
  private final BaseWriter writer;
  private ObjectReference lastObjectReference;

  private ProblemsHolder problemsHolder;

  private ByteRange declarationsRange;

  final static ValueWriter IGNORE = new ValueWriter() {
    @Override
    public int write(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) {
      throw new UnsupportedOperationException();
    }
  };

  public InjectedASWriter(BaseWriter writer) {
    this.writer = writer;
  }

  void setProblemsHolder(@Nullable ProblemsHolder problemsHolder) {
    this.problemsHolder = problemsHolder;
  }

  public ValueWriter processProperty(XmlElementValueProvider valueProvider, String name, @Nullable String type, boolean isStyle,
                                     @Nullable Context context) {
    PsiElement host = valueProvider.getInjectedHost();
    if (host == null) {
      return null;
    }

    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(type)) {
      return checkArray(host, name, isStyle, context);
    }
    else {
      return checkObject(host, name, isStyle, context, type);
    }
  }

  private ValueWriter checkArray(PsiElement host, String name, boolean isStyle, @Nullable Context context) {
    InjectedPsiVisitor visitor = new InjectedPsiVisitor(host, JSCommonTypeNames.ARRAY_CLASS_NAME, problemsHolder);
    InjectedLanguageUtil.enumerate(host, visitor);
    if (visitor.values != null) {
      bindingItems.add(new ArrayBinding(writer.getObjectOrFactoryId(context), writer.getNameReference(name), visitor.values, isStyle));
      return IGNORE;
    }
    else {
      return visitor.getValueWriter();
    }
  }

  private ValueWriter checkObject(PsiElement host, String name, boolean isStyle, @Nullable Context context, @Nullable String type) {
    InjectedPsiVisitor visitor = new InjectedPsiVisitor(host, type, problemsHolder);
    InjectedLanguageUtil.enumerate(host, visitor);
    if (visitor.values != null) {
      bindingItems.add(new ObjectBinding(writer.getObjectOrFactoryId(context), writer.getNameReference(name), visitor.values[0],
                                         isStyle));
      return IGNORE;
    }
    else {
      return visitor.getValueWriter();
    }
  }

  public void readDeclarations(MxmlWriter mxmlWriter, XmlTag tag) {
    declarationsRange = writer.getBlockOut().startRange();
    mxmlWriter.processDeclarations(tag);
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
    if (bindingItems.isEmpty()) {
      out.writeShort(0);
      return;
    }
    
    final int bindingSizePosition = out.getBlockOut().allocate(2);
    int size = bindingItems.size();
    
    for (Binding binding : bindingItems) {
      int beforePosition = out.size();
      try {
        binding.write(out);
        continue;
      }
      catch (Throwable e) {
        problemsHolder.add(e);
      }
      
      size--;
      writer.getBlockOut().setPosition(beforePosition);
    }

    out.putShort(size, bindingSizePosition);
  }

  private void writeObjectReference(PrimitiveAmfOutputStream out, String id) throws InvalidPropertyException {
    ObjectReference reference = idReferenceMap.get(id);
    if (reference == null) {
      throw new InvalidPropertyException("error.unresolved.variable", id);
    }
    
    StaticInstanceReferenceInDeferredParentInstance staticReferenceInDeferredParentInstance = reference.staticReferenceInDeferredParentInstance;
    if (staticReferenceInDeferredParentInstance == null || staticReferenceInDeferredParentInstance.isWritten()) {
      out.writeUInt29(reference.id << 1);
    }
    else {
      out.writeUInt29((staticReferenceInDeferredParentInstance.getObjectInstance() << 1) | 1);
      out.writeUInt29(staticReferenceInDeferredParentInstance.getDeferredParentInstance());
      out.writeUInt29(reference.id);

      staticReferenceInDeferredParentInstance.markAsWritten();
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

  void setDeferredReferenceForObjectWithExplicitId(StaticInstanceReferenceInDeferredParentInstance staticReferenceInDeferredParentInstance, int referenceInstance) {
    assert lastObjectReference.id == referenceInstance;
    lastObjectReference.staticReferenceInDeferredParentInstance = staticReferenceInDeferredParentInstance;
  }

  private static class InjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
    private final PsiElement host;
    
    private final @Nullable String expectedType;

    private boolean visited;

    private boolean unsupported;
    private String[] values;
    private ValueWriter valueWriter;

    private final ProblemsHolder problemsHolder;

    public InjectedPsiVisitor(PsiElement host, String expectedType, ProblemsHolder problemsHolder) {
      this.host = host;
      this.expectedType = expectedType;
      this.problemsHolder = problemsHolder;
    }
    
    public ValueWriter getValueWriter() {
      return unsupported ? IGNORE : valueWriter;
    }

    public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
      // todo <s:Label text="{demandSelectedTO.journalist.nom} {demandSelectedTO.journalist.prenom}"/>
      // will be called 2 — first for {demandSelectedTO.journalist.nom} and second for {demandSelectedTO.journalist.prenom}
      if (visited) {
        return;
      }

      visited = true;

      assert places.size() == 1;
      assert places.get(0).host == host;
      JSFile jsFile = (JSFile)injectedPsi;
      JSSourceElement[] statements = jsFile.getStatements();
      if (statements.length == 0) {
        if ((valueWriter = checkEmbed(jsFile)) != null) {
          return;
        }
      }

      assert statements.length == 1;
      JSCallExpression expression = (JSCallExpression)((JSExpressionStatement)statements[0]).getExpression();
      JSExpression[] arguments = expression.getArgumentList().getArguments();
      if (arguments.length == 1) {
        if (arguments[0] instanceof JSArrayLiteralExpression) {
          if (isUnexpected(JSCommonTypeNames.ARRAY_CLASS_NAME)) {
            return;
          }

          JSExpression[] expressions = ((JSArrayLiteralExpression)arguments[0]).getExpressions();
          if (expressions.length == 0) {
            valueWriter = IGNORE;
            return;
          }
          
          // todo mixed is not supported
          int arrayOfPrimitives = -1;
          for (JSExpression itemExpression : expressions) {
            if (itemExpression instanceof JSReferenceExpression) {
              // must not have children
              if (itemExpression.getChildren().length == 0) {
                if (arrayOfPrimitives == 0) {
                  continue;
                }
                else if (arrayOfPrimitives == -1) {
                  arrayOfPrimitives = 0;
                  continue;
                }
              }
            }
            else if (itemExpression instanceof JSLiteralExpression) {
              if (arrayOfPrimitives == 1) {
                continue;
              }
              else if (arrayOfPrimitives == -1) {
                arrayOfPrimitives = 1;
                continue;
              }
            }

            warnUnsupported(expression, itemExpression);
            return;
          }
          
          assert arrayOfPrimitives != -1;
          if (arrayOfPrimitives == 0) {
            values = new String[expressions.length];
            for (int i = 0, expressionsLength = expressions.length; i < expressionsLength; i++) {
              JSReferenceExpression referenceExpression = (JSReferenceExpression)expressions[i];
              PsiElement element = referenceExpression.resolve();
              if (element instanceof JSVariable) {
                values = null;
                logUnsupported();
                return;
              }
              else {
                values[i] = referenceExpression.getReferencedName();
              }
            }
          }
          else {
            valueWriter = new InjectedArrayOfPrimitivesWriter(expressions);
          }
        }
        else if (arguments[0] instanceof JSReferenceExpression && arguments[0].getChildren().length == 0) {
          // if propertyName="{CustomSkin}", so, write class, otherwise, it is binding
          JSReferenceExpression referenceExpression = (JSReferenceExpression)arguments[0];
          PsiElement element = referenceExpression.resolve();
          if (isExpectedObjectOrAnyType() || AsCommonTypeNames.CLASS.equals(expectedType)) {
            if (element instanceof JSClass) {
              valueWriter = new ClassValueWriter(((JSClass)element));
              return;
            }
          }

          // variable in Script block or super class — skip it
          if (element instanceof JSVariable) {
            logUnsupported();
          }
          else {
            values = new String[]{referenceExpression.getReferencedName()};
          }
        }
        else {
          logUnsupported();
        }
      }
      else {
        logUnsupported();
      }
    }

    private void warnUnsupported(JSCallExpression expression, JSExpression itemExpression) {
      unsupported = true;
      LOG.warn("unsupported injected AS: " + itemExpression.getText() + " in outer expression " + expression.getText() +
               " (mxml: " + host.getText() + ")");
    }

    private ValueWriter checkEmbed(JSFile jsFile) {
      PsiElement firstChild = jsFile.getFirstChild();
      if (firstChild instanceof LeafPsiElement && ((LeafPsiElement)firstChild).getElementType() == JSTokenTypes.AT) {
        JSAttribute attribute = (JSAttribute)firstChild.getNextSibling();
        assert attribute != null;
        VirtualFile source = null;
        String mimeType = null;
        String symbol = null;
        for (JSAttributeNameValuePair p : attribute.getValues()) {
          final String name = p.getName();
          if (name == null || name.equals("source")) {
            try {
              source = InjectionUtil.getReferencedFile(p, true);
            }
            catch (InvalidPropertyException e) {
              problemsHolder.add(e);
              return IGNORE;
            }
          }
          else if (name.equals("mimeType")) {
            mimeType = p.getSimpleValue();
          }
          else if (name.equals("symbol")) {
            symbol = p.getSimpleValue();
          }
        }

        if (source == null) {
          problemsHolder.add(host, FlexUIDesignerBundle.message("error.embed.source.not.specified", host.getText()));
          return IGNORE;
        }

        if (InjectionUtil.isSwf(source, mimeType)) {
          return new SwfValueWriter(source, symbol);
        }
        else {
          if (symbol != null) {
            problemsHolder.add(host, FlexUIDesignerBundle.message("error.embed.symbol.unneeded", host.getText()));
          }

          return new ImageValueWriter(source, mimeType);
        }
      }

      return null;
    }

    private boolean isUnexpected(String actualType) {
      if (actualType.equals(expectedType) || expectedType == null || isExpectedObjectOrAnyType()) {
        return false;
      }
      else {
        problemsHolder.add(host, "Expected " + expectedType + ", but got " + host.getText());
        unsupported = true;
        return true;
      }
    }

    private boolean isExpectedObjectOrAnyType() {
      return JSCommonTypeNames.OBJECT_CLASS_NAME.equals(expectedType) || JSCommonTypeNames.ANY_TYPE.equals(expectedType);
    }

    private void logUnsupported() {
      LOG.warn("unsupported injected AS: " + host.getText());
      unsupported = true;
    }
  }

  private static class ObjectReference {
    private final int id;
    StaticInstanceReferenceInDeferredParentInstance staticReferenceInDeferredParentInstance;

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

    void write(PrimitiveAmfOutputStream out) throws InvalidPropertyException {
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
    void write(PrimitiveAmfOutputStream out) throws InvalidPropertyException {
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
    void write(PrimitiveAmfOutputStream out) throws InvalidPropertyException {
      super.write(out);

      writeObjectReference(out, value);
    }

    @Override
    protected int getType() {
      return 0;
    }
  }
}