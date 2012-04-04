package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

final class BaseWriter extends PrimitiveWriter {
  private static final int EMPTY_CLASS_OR_PROPERTY_NAME = 0;
  
  final int ARRAY;
  final int P_FUD_RANGE_ID;

  private boolean stringWriterFinished;

  private final int startPosition;

  private final BlockDataOutputStream blockOut;

  private final Scope rootScope = new Scope();
  final NullContext nullContext;

  private final AssetCounter assetCounter;

  public BaseWriter(PrimitiveAmfOutputStream out, AssetCounter assetCounter) {
    super(out, new StringRegistry.StringWriter());

    blockOut = out.getBlockOut();
    this.assetCounter = assetCounter;

    ARRAY = getNameReference("array");
    P_FUD_RANGE_ID = getNameReference("$fud_r");

    assert blockOut.getLastMarker() == null;
    startPosition = out.size();

    nullContext = new NullContext(rootScope);
  }

  public AssetCounter getAssetCounter() {
    return assetCounter;
  }

  public PrimitiveAmfOutputStream getOut() {
    return out;
  }

  public BlockDataOutputStream getBlockOut() {
    return blockOut;
  }

  public int getPreallocatedId() {
    return nullContext.id;
  }

  public boolean isIdPreallocated() {
    return nullContext.getId() != -1;
  }

  public StaticObjectContext createStaticContext(@Nullable Context parentContext, int referencePosition) {
    if (parentContext == null || parentContext.getBackSibling() == null) {
      return new StaticObjectContext(referencePosition, out, nullContext.getId(), rootScope);
    }
    else {
      return parentContext.getBackSibling().reinitialize(referencePosition, nullContext.getId());
    }
  }

  public void resetAfterMessage() {
    if (!stringWriterFinished) {
      stringWriter.finishChange();
    }
  }

  public void endObject() {
    out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
  }

  public int allocateAbsoluteStaticObjectId() {
    return rootScope.referenceCounter++;
  }

  public void writeMessageHeader(ProjectComponentReferenceCounter projectComponentReferenceCounter) throws IOException {
    final ByteRange range = blockOut.startRange();

    if (projectComponentReferenceCounter.total.isEmpty()) {
      out.write(Amf3Types.NULL);
    }
    else {
      out.write(projectComponentReferenceCounter.total);
    }

    stringWriter.writeTo(out);
    stringWriterFinished = true;

    out.writeShort(rootScope.referenceCounter);

    blockOut.endRange(range);
    blockOut.insert(startPosition, range);
  }

  public void prepend(ByteRange range) {
    blockOut.insert(startPosition, range);
  }

  public int getNameReference(String classOrPropertyName) {
    return stringWriter.getReference(classOrPropertyName);
  }

  public void classOrPropertyName(String classOrPropertyName) {
    stringWriter.write(classOrPropertyName, out);
  }

  public void vectorHeader(String elementType) {
    out.write(Amf3Types.VECTOR_OBJECT);
    stringWriter.write(elementType, out);
  }

  public void idMxmlProperty(String value) {
    classOrPropertyName(FlexMxmlLanguageAttributeNames.ID);
    out.write(AmfExtendedTypes.ID);
    out.writeAmfUtf(value, false);
  }

  public void objectReference(int reference) {
    out.write(AmfExtendedTypes.OBJECT_REFERENCE);
    out.writeUInt29(reference);
  }

  public void objectReference(Context context) {
    objectReference(context.getOrAllocateId());
  }

  public void objectHeader(int className) {
    out.write(AmfExtendedTypes.OBJECT);
    out.writeUInt29(className);
  }

  public void objectHeader(String className) {
    out.write(AmfExtendedTypes.OBJECT);
    stringWriter.write(className, out);
  }

  public void mxmlObjectHeader(String className) {
    stringWriter.write(className, out);
    out.allocateClearShort();
  }

  public void documentFactoryReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE);
    out.writeUInt29(reference);
  }

  public void documentReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_REFERENCE);
    out.writeUInt29(reference);
  }

  public void projectClassReference(int reference) {
    out.write(AmfExtendedTypes.PROJECT_CLASS_REFERENCE);
    out.writeUInt29(reference);

    assetCounter.viewCount++;
  }

  public void classReference(String className) {
    out.write(AmfExtendedTypes.CLASS_REFERENCE);
    classOrPropertyName(className);
  }

  public void arrayHeader(int length) {
    out.write(Amf3Types.ARRAY);
    out.writeShort(length);
  }

  public int arrayHeader() {
    out.write(Amf3Types.ARRAY);
    return out.allocateClearShort();
  }

  public BaseWriter newInstance(String className, int argumentsLength, boolean rollbackable) {
    out.write(ExpressionMessageTypes.NEW);
    classOrPropertyName(className);
    out.write((argumentsLength << 1) | (rollbackable ? 1 : 0));
    return this;
  }

  public BaseWriter referableHeader(int reference) {
    out.write(AmfExtendedTypes.REFERABLE);
    out.writeShort(reference + 1);
    return this;
  }

  public int componentFactory(int reference) {
    out.write(AmfExtendedTypes.COMPONENT_FACTORY);
    out.writeUInt29(reference + 1);
    int sizePosition = out.allocateShort();
    out.allocateShort(); // object table size
    return sizePosition;
  }

  public int referableHeader() {
    out.write(AmfExtendedTypes.REFERABLE);
    return out.allocateClearShort();
  }

  public BaseWriter property(int propertyName) {
    out.writeUInt29(propertyName);
    return this;
  }

  public BaseWriter property(String propertyName) {
    classOrPropertyName(propertyName);
    return this;
  }

  public void typeMarker(int typeMarker) {
    out.write(typeMarker);
  }
}
