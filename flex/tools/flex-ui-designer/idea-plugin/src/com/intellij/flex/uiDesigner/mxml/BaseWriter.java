package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.CssPropertyType;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.xml.util.ColorSampleLookupValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class BaseWriter {
  int ARRAY = -1;
  
  private final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();

  private int startPosition;
  
  private BlockDataOutputStream blockOut;
  private PrimitiveAmfOutputStream out;
  
  private final Scope rootScope = new Scope();
  private int preallocatedId = -1;
  
  public Scope getRootScope() {
    return rootScope;
  }
  
  public PrimitiveAmfOutputStream getOut() {
    return out;
  }

  public void setOutput(@NotNull PrimitiveAmfOutputStream out) {
    this.out = out;
    blockOut = out.getBlockOut();
  }
  
  public BlockDataOutputStream getBlockOut() {
    return blockOut;
  }
  
  public int getPreallocatedId() {
    return preallocatedId;
  }

  /* мы не можем знать, есть ли у объекта атрибуты includeIn/excludeFrom, поэтому мы для первого (1: title.A="dd" 2: title.B="dfsdfsdf") ничего не ставим, а для второго и последующих
  аллоцируем reference от рута — если это static object в root scope, то он и возьмет этот id, а если это static object в dynamic object scope — то мы этот id используем для DeferredInstanceFromObjectReference,
  id которого всегда в root scope */
  private int preallocateIdIfNeed() {
    if (!isIdPreallocated()) {
      preallocatedId = rootScope.referenceCounter++;
    }
    
    return preallocatedId;
  }
  
  public boolean isIdPreallocated() {
    return preallocatedId != -1;
  }
  
  public void resetPreallocatedId() {
    preallocatedId = -1;
  }
  
  public StaticObjectContext createStaticContext(Context parentContext, int referencePosition) {
    if (parentContext == null || parentContext.getBackSibling() == null) {
      return new StaticObjectContext(referencePosition, out, preallocatedId, rootScope);
    }
    else {
      return parentContext.getBackSibling().reinitialize(referencePosition, preallocatedId);
    }
  }

  public DynamicObjectContext createDynamicObjectStateContext() {
    return new DynamicObjectContext(preallocatedId, rootScope);
  }

  public void reset() {
    resetAfterMessage();
    
    out = null;
    blockOut = null;
    ARRAY = -1;
  }

  private void initNames() {
    ARRAY = getNameReference("array");
  }
  
  public void resetAfterMessage() {
    stringWriter.finishChange();
  }
  
  public void addMarker(ByteRange dataRange) {
    blockOut.addMarker(new ByteRangeMarker(blockOut.size(), dataRange));
  }

  public void beginMessage() {
    stringWriter.startChange();
    if (ARRAY == -1) {
      initNames();
    }

    assert blockOut.getNextMarkerIndex() == 0;
    startPosition = out.size();
  }
  
  public int getObjectId(Context context) {
    if (context.getId() == -1) {
      context.setId(context.getParentScope().referenceCounter++);
      context.referenceInitialized();
    }
    
    return context.getId();
  }
  
  public int getObjectOrFactoryId(@Nullable Context context) {
    return context == null ? preallocateIdIfNeed() : getObjectId(context);
  }

  public void endMessage() throws IOException {
    int stringTableSize = stringWriter.size();
    blockOut.beginWritePrepended(stringTableSize + (rootScope.referenceCounter < 0x80 ? 1 : 2), startPosition);
    blockOut.writePrepended(stringWriter.getCounter(), stringWriter.getByteArrayOut());
    blockOut.writePrepended(rootScope.referenceCounter);
    blockOut.endWritePrepended(startPosition);
    
    rootScope.referenceCounter = 0;
  }
  
  public int getNameReference(String classOrPropertyName) {
    return stringWriter.getReference(classOrPropertyName);
  }

  public void write(String classOrPropertyName) {
    stringWriter.write(classOrPropertyName, out);
  }
  
  public void writeNullable(String classOrPropertyName) {
    stringWriter.writeNullable(classOrPropertyName, out);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void writeProperty(String propertyName, String value) {
    writeProperty(getNameReference(propertyName), value);
  }
  
  public void writeProperty(int propertyNameReference, String value) {
    out.writeUInt29(propertyNameReference);
    out.write(PropertyClassifier.PROPERTY);
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(value, false);
  }
  
  public void writeProperty(String propertyName, int value) {
    stringWriter.writeNullable(propertyName, out);
    out.write(PropertyClassifier.PROPERTY);
    out.writeAmfInt(value);
  }

  public void writeIdProperty(String value) {
    write(FlexMxmlLanguageAttributeNames.ID);
    out.write(PropertyClassifier.ID);
    out.writeAmfUtf(value, false);
  }
  
  public void writeStringReference(String propertyName, String reference) {
    writeStringReference(getNameReference(propertyName), getNameReference(reference));
  }
  
  public void writeStringReference(int propertyName, String reference) {
    writeStringReference(propertyName, getNameReference(reference));
  }
  
  public void writeStringReference(int propertyName, int reference) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    out.writeUInt29(reference);
  }
  
  public void writeStringReference(String reference) {
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    stringWriter.write(reference, out);
  }
  
  public void writeString(CharSequence value) {
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(value, false);
  }
  
  public void writeObjectReference(String propertyName, int reference) {
    writeObjectReference(getNameReference(propertyName), reference);
  }
  
  public void writeObjectReference(int reference) {
    out.write(AmfExtendedTypes.OBJECT_REFERENCE);
    out.writeUInt29(reference);
  }
  
  public void writeObjectReference(int propertyName, int reference) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    writeObjectReference(reference);
  }
  
  public void writeObjectReference(int propertyName, Context context) {
    writeObjectReference(propertyName, getObjectId(context));
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void writeArrayHeader(int propertyName) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    out.write(Amf3Types.ARRAY);
  }

  public void writeFixedArrayHeader(int propertyName, int size) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.FIXED_ARRAY);
    out.write(size);
  }
  
  @SuppressWarnings({"UnusedDeclaration"})
  public void writeObjectHeader(int propertyName, String className) {
    writeObjectHeader(propertyName, getNameReference(className));
  }
  
  public void writeObjectHeader(int propertyName, int className) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className);
  }
  
  public void writeObjectHeader(String className) {
    writeObjectHeader(getNameReference(className));
  }
  
  public void writeObjectHeader(String className, int reference) {
    write(className);
    out.writeShort(reference + 1);
  }
  
  public void writeObjectHeader(int className) {
    out.writeUInt29(className);
    out.getByteOut().allocate(2);
  }
  
  public void writeDocumentFactoryReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE);
    out.writeUInt29(reference);
  }
  
  public void writeClass(String className) {
    out.write(AmfExtendedTypes.CLASS);
    write(className);
  }

  public void writeColor(String value, boolean isPrimitiveStyle) {
    out.write(AmfExtendedTypes.COLOR_STYLE_MARKER);
    if (value.charAt(0) == '#') {
      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_INT);
      }
      value = value.substring(1);
    }
    else if (value.charAt(0) == '0') {
      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_INT);
      }
      value = value.substring(2);
    }
    else {
      final String colorName = value.toLowerCase();
      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_STRING);
        stringWriter.writeNullable(colorName, out);
      }
      value = ColorSampleLookupValue.getHexCodeForColorName(colorName).substring(1);
    }

    out.writeAmfUInt(Integer.parseInt(value, 16));
  }

  public void writeDeferredInstanceFromArray() {
    writeConstructorHeader("com.intellij.flex.uiDesigner.flex.DeferredInstanceFromArray");
    out.write(Amf3Types.ARRAY);
  }
  
  public void writeConstructorHeader(String className) {
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className);
    write("1");
  }
  
  public void writeConstructorHeader(String className, int reference) {
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className, reference);
    write("1");
  }
  
  public void writeConstructorHeader(int propertyName, String className, int constructorArgType) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    writeConstructorHeader(className);
    out.write(constructorArgType);
  }
}
