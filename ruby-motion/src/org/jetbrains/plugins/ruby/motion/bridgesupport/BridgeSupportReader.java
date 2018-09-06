/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.signature.SignatureReader;
import org.jetbrains.org.objectweb.asm.signature.SignatureVisitor;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RNameUtilCore;

import java.io.InputStream;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class BridgeSupportReader {
  private static final Logger LOG = Logger.getInstance(BridgeSupportReader.class);

  private static final String DECLARED_TYPE = "declared_type";
  private static final String DECLARED_TYPE64 = "declared_type64";
  private static final String NAME = "name";

  public static Framework read(final String name, final String version, final InputStream text, final boolean osx) {
    final Framework framework = new Framework(name, version, osx);
    try {
      final Element root = new SAXBuilder().build(text).getRootElement();
      readFramework(root, framework);
      framework.mergeClasses();
    } catch (Exception e) {
      LOG.error("Can't load framework", e, name, version, osx ? "osx" : "");
    }
    finally {
      StreamUtil.closeStream(text);
    }
    framework.seal();
    return framework;
  }

  private static void readFramework(Element root, Framework framework) {

    final List children = root.getChildren();
    for (Object child : children) {
      final Element e = (Element)child;
      final String name = e.getName();
      if ("class".equals(name) || "interface".equals(name)) {
        framework.addClass(readClass(e));
      } else if ("informal_protocol".equals(name)) {
        framework.addProtocol(readClass(e));
      } else if ("constant".equals(name)) {
        readConstant(framework, e);
      } else if ("string_constant".equals(name)) {
        readStringConstant(framework, e);
      } else if ("enum".equals(name)) {
        readEnum(framework, e);
      } else if ("function".equals(name)) {
        readFunction(framework, e);
      } else if ("function_alias".equals(name)) {
        readFunctionAlias(framework, e);
      } else if ("struct".equals(name)) {
        readStruct(framework, e);
      }
    }
  }

  private static void readStruct(Framework framework, Element e) {
    final Struct struct = new Struct(e.getAttributeValue(NAME));
    for (Object o : e.getChildren()) {
      final Element child = (Element)o;
      if ("field".equals(child.getName())) {
        struct.addField(child.getAttributeValue(NAME), getDeclaredType(child));
      }
    }
    struct.seal();
    framework.addStruct(struct);
  }

  private static void readFunctionAlias(Framework framework, Element e) {
    framework.addFunctionAlias(e.getAttributeValue("name"), e.getAttributeValue("original"));
  }

  private static void readFunction(FunctionHolder holder, Element e) {
    String name = e.getAttributeValue("selector");
    name = name == null ? e.getAttributeValue(NAME) : name;
    final Function function = new Function(name, "true".equals(e.getAttributeValue("variadic")),
                                           "true".equals(e.getAttributeValue("class_method")));
    final String type = e.getAttributeValue("type");
    for (Object o : e.getChildren()) {
      final Element child = (Element)o;
      if ("arg".equals(child.getName())) {
        function.addArgument(child.getAttributeValue(NAME), getDeclaredType(child));
      } else if ("retval".equals(child.getName())) {
        function.setReturnValue(getDeclaredType(child));
      }
    }
    if (function.getReturnValue() == null && type != null) {
      readAndroidTypeAndArguments(function, type);
    }
    holder.addFunction(function);
  }

  private static void readAndroidTypeAndArguments(final Function function, String argsAndType) {
    final SignatureReader reader = new SignatureReader(argsAndType);
    final AndroidSignatureVisitor visitor = new AndroidSignatureVisitor(function);
    reader.accept(visitor);
    function.setReturnValue(visitor.getReturnType());
  }

  private static void readConstant(Framework framework, Element e) {
    framework.addConstant(new Constant(e.getAttributeValue(NAME), getDeclaredType(e)));
  }

  private static void readStringConstant(Framework framework, Element e) {
    final String nsstring = e.getAttributeValue("nsstring");
    framework.addConstant(new StringConstant(e.getAttributeValue(NAME), e.getAttributeValue("value"), "true".equals(nsstring)));
  }

  private static void readEnum(Framework framework, Element e) {
    framework.addConstant(new Enum(e.getAttributeValue(NAME), e.getAttributeValue("value")));
  }

  private static Class readClass(Element e) {
    final String name = buildClassName(e.getAttributeValue(NAME));
    final Class clazz = new Class(name);
    for (Object o : e.getChildren()) {
      final Element child = (Element)o;
      if ("method".equals(child.getName())) {
        readFunction(clazz, child);
      }
    }
    clazz.seal();
    return clazz;
  }

  private static String buildClassName(final String name) {
    final String[] components = name.split("(/|\\$)");
    for (int i = 0; i < components.length; i++) {
      components[i] = StringUtil.capitalize(components[i]);
    }
    return StringUtil.join(components, RNameUtilCore.SYMBOL_DELIMITER);
  }

  private static String getDeclaredType(Element e) {
    String declaredType = e.getAttributeValue(DECLARED_TYPE);
    declaredType = declaredType == null ? e.getAttributeValue(DECLARED_TYPE64) : declaredType;
    if (declaredType == null) {
      LOG.warn("No declared type for " + ((Element)e.getParent()).getAttributeValue(NAME));
      return "void";
    }
    declaredType = StringUtil.trimEnd(declaredType, " _Nullable");
    declaredType = StringUtil.trimEnd(declaredType, " _Nonnull");
    return declaredType.intern();
  }

  private static class AndroidSignatureVisitor extends SignatureVisitor {
    private final Function myFunction;
    private final StringBuilder typeBuilder;
    private int arrayLevel;

    AndroidSignatureVisitor(Function function) {
      super(Opcodes.API_VERSION);
      myFunction = function;
      typeBuilder = new StringBuilder();
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
      return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
      return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
      return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
      return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
      addArgument();
      return this;
    }

    private void addArgument() {
      if (typeBuilder.length() > 0) {
        finishArrayType();
        myFunction.addArgument(null, typeBuilder.toString());
        typeBuilder.setLength(0);
      }
    }

    @Override
    public SignatureVisitor visitReturnType() {
      addArgument();
      return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
      return this;
    }

    @Override
    public void visitBaseType(char descriptor) {
      final String type;
      switch (descriptor) {
        case 'Z':
          type = "bool";
          break;
        case 'B':
          type = "Byte";
          break;
        case 'C':
          type = "char";
          break;
        case 'D':
          type = "double";
          break;
        case 'F':
          type = "float";
          break;
        case 'I':
          type = "int";
          break;
        case 'J':
          type = "long";
          break;
        default:
          type = "void";
      }
      typeBuilder.append(type);
    }

    @Override
    public void visitTypeVariable(String name) {

    }

    @Override
    public SignatureVisitor visitArrayType() {
      typeBuilder.append("Array<");
      arrayLevel++;
      return this;
    }

    @Override
    public void visitClassType(String name) {
      typeBuilder.append(buildClassName(name));
    }

    @Override
    public void visitInnerClassType(String name) {

    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
      return this;
    }

    @Override
    public void visitEnd() {
    }

    public String getReturnType() {
      finishArrayType();
      return typeBuilder.toString();
    }

    private void finishArrayType() {
      for (; arrayLevel > 0; arrayLevel--) {
        typeBuilder.append(">");
      }
    }
  }
}
