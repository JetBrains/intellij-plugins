package com.intellij.flex.uiDesigner.abc;

import com.adobe.fxg.FXGParserFactory;
import com.adobe.fxg.swf.FXG2SWFTranscoder;
import com.adobe.internal.fxg.dom.GraphicNode;
import flash.swf.CompressionLevel;
import flash.swf.SwfEncoder;
import flash.swf.Tag;
import flash.swf.TagEncoder;
import flash.swf.tags.*;
import flash.swf.types.ImportRecord;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.Set;

public class FxgTranscoder extends SymbolTranscoderBase {
  private GraphicNode node;

  @Override
  protected void readSource(InputStream in, long inputLength) throws IOException {
    node = (GraphicNode)FXGParserFactory.createDefaultParser().parse(in);
    buffer = ByteBuffer.allocate(8 * 1024).order(ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  protected void transcode(boolean writeBounds) throws IOException {
    if (writeBounds) {
      bounds = new Rectangle(0, 0, 200, 200);
      writeMovieBounds();
    }

    fileLength = SYMBOL_CLASS_TAG_FULL_LENGTH + SwfUtil.getWrapLength();

    FXG2SWFTranscoder transcoder = new FXG2SWFTranscoder();
    DefineSprite spriteDefinition = (DefineSprite)transcoder.transcode(node);
    MyTagEncoder tagEncoder = new MyTagEncoder();
    define(spriteDefinition, new THashSet<Tag>(), tagEncoder);
    fileLength += tagEncoder.getWriter().getPos();

    final byte[] symbolOwnClassAbc = getSymbolOwnClassAbc((short)1);
    fileLength += symbolOwnClassAbc.length;

    SwfUtil.header(fileLength, out);
    tagEncoder.getWriter().writeTo(out, CompressionLevel.BestSpeed);

    out.write(symbolOwnClassAbc);
    writeSymbolClass(tagEncoder.getDictionary().getId(spriteDefinition));
    SwfUtil.footer(out);
  }

  private static final class MyTagEncoder extends TagEncoder {
    private MyTagEncoder() {
      tagw = createEncoder(getSwfVersion());
      writer = createEncoder(getSwfVersion());
    }

    @Override
    public CompressionLevel getCompressionLevel() {
      return CompressionLevel.BestSpeed;
    }

    public SwfEncoder getWriter() {
      return writer;
    }

    @Override
    protected int getSwfVersion() {
      return 11;
    }
  }

  private static void define(Tag tag, Set<Tag> defined, MyTagEncoder tagVisitor) {
    if (!defined.add(tag)) {
      return;
    }

    for (Iterator i = tag.getReferences(); i.hasNext(); ) {
      define((Tag)i.next(), defined, tagVisitor);
    }

    if (tag instanceof ImportRecord) {
      return;
    }

    tag.visit(tagVisitor);

    Tag visitAfter;
    if (tag instanceof DefineSprite) {
      visitAfter = ((DefineSprite)tag).scalingGrid;
    }
    else if (tag instanceof DefineButton) {
      visitAfter = ((DefineButton)tag).scalingGrid;
    }
    else if (tag instanceof DefineShape) {
      visitAfter = ((DefineShape)tag).scalingGrid;
    }
    else if (tag instanceof DefineFont3) {
      visitAfter = ((DefineFont3)tag).zones;
    }
    else if (tag instanceof DefineEditText) {
      visitAfter = ((DefineEditText)tag).csmTextSettings;
    }
    else if (tag instanceof DefineText) {
      visitAfter = ((DefineText)tag).csmTextSettings;
    }
    else {
      return;
    }

    visitAfter(visitAfter, defined, tagVisitor);
    if (tag instanceof DefineFont) {
      visitAfter(((DefineFont)tag).license, defined, tagVisitor);
    }
  }

  private static void visitAfter(@Nullable Tag visitAfter, Set<Tag> defined, MyTagEncoder tagVisitor) {
    if (defined.add(visitAfter) && visitAfter != null) {
      visitAfter.visit(tagVisitor);
    }
  }
}