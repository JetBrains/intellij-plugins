package com.intellij.flex.uiDesigner.abc.test;

import flash.swf.*;
import flash.swf.tags.DoABC;
import macromedia.abc.*;

import java.io.*;
import java.util.List;

public class Optimizer {
  public static void main(String[] args) throws IOException, macromedia.abc.DecoderException {
    final long time = System.currentTimeMillis();
    optimize(new FileInputStream(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/library.swf")),
             new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/libraryORI.swf"));
    System.out.print(System.currentTimeMillis() - time);
    //u();
  }

  public static void optimize(InputStream in, File outFile) throws IOException, macromedia.abc.DecoderException {
    Movie movie = new Movie();
    TagDecoder tagDecoder = new TagDecoder(in);
    MovieDecoder movieDecoder = new MovieDecoder(movie);
    tagDecoder.parse(movieDecoder);

    movie.uuid = null;
    movie.enableDebugger = null;
    movie.bgcolor = null;
    movie.topLevelClass = null;
    movie.productInfo = null;
    try {
      // <censored> Flex SDK <censored>
      if (movie.frames.size() > 1) {
        movie.frames.remove(1);
      }
      
      merge(movie.frames.get(0).doABCs, true, true);

    }
    catch (com.intellij.flex.uiDesigner.abc.DecoderException e) {
      throw new IOException(e);
    }

    TagEncoder handler = new TagEncoder();
    System.setProperty("flex.swf.uncompressed", "true");
    MovieEncoder encoder = new MovieEncoder(handler);
    encoder.export(movie);
    final FileOutputStream out = new FileOutputStream(outFile);
    try {
      handler.writeTo(out);
    }
    finally {
      out.close();
    }
  }

  private static void merge(List<DoABC> doABCs, boolean keepDebugOpcodes, boolean runPeephole)
    throws com.intellij.flex.uiDesigner.abc.DecoderException, macromedia.abc.DecoderException {
    int majorVersion = 0, minorVersion = 0, abcSize = doABCs.size(), flag;
    if (abcSize == 0) {
      return;
    }
    else if (abcSize == 1) {
      flag = doABCs.get(0).flag;
    }
    else {
      flag = 1;
    }

    Decoder[] decoders = new Decoder[abcSize];
    ConstantPool[] pools = new ConstantPool[abcSize];

    // create decoders...
    for (int j = 0; j < abcSize; j++) {
      DoABC tag = doABCs.get(j);
      BytecodeBuffer in = new BytecodeBuffer(tag.abc);

      decoders[j] = new Decoder(in);
      majorVersion = decoders[j].majorVersion;
      minorVersion = decoders[j].minorVersion;
      pools[j] = decoders[j].constantPool;
    }

    Encoder encoder = new Encoder(majorVersion, minorVersion);
    // all the constant pools are merged here...
    encoder.addConstantPools(pools);
    if (!keepDebugOpcodes) {
      encoder.disableDebugging();
    }

    // always enable peephole optimization...
    if (runPeephole) {
      encoder.enablePeepHole();
    }

    encoder.configure(decoders);

    Decoder decoder;
    // decode methodInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.MethodInfo methodInfo = decoder.methodInfo;
      for (int k = 0, infoSize = methodInfo.size(); k < infoSize; k++) {
        methodInfo.decode(k, encoder);
      }
    }

    // decode metadataInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.MetaDataInfo metadataInfo = decoder.metadataInfo;
      for (int k = 0, infoSize = metadataInfo.size(); k < infoSize; k++) {
        metadataInfo.decode(k, encoder);
      }
    }

    // decode classInfo...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ClassInfo classInfo = decoder.classInfo;

      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeInstance(k, encoder);
      }
    }

    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ClassInfo classInfo = decoder.classInfo;
      for (int k = 0, infoSize = classInfo.size(); k < infoSize; k++) {
        classInfo.decodeClass(k, 0, encoder);
      }
    }

    // decode scripts...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.ScriptInfo scriptInfo = decoder.scriptInfo;

      for (int k = 0, scriptSize = scriptInfo.size(); k < scriptSize; k++) {
        scriptInfo.decode(k, encoder);
      }
    }

    // decode method bodies...
    for (int j = 0; j < abcSize; j++) {
      decoder = decoders[j];
      encoder.useConstantPool(j);

      Decoder.MethodBodies methodBodies = decoder.methodBodies;
      for (int k = 0, bodySize = methodBodies.size(); k < bodySize; k++) {
        methodBodies.decode(k, 2, encoder);
      }
    }

    DoABC doABC = new DoABC("o", flag);
    doABC.abc = encoder.toABC();
    if (doABC.abc != null) {
      doABCs.clear();
      doABCs.add(doABC);
    }
  }
}
