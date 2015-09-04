package org.jetbrains.plugins.ruby.motion.stubs;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.*;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Class;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Enum;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.namesValidator.RubyNamesValidator;
import org.jetbrains.plugins.ruby.utils.NamingConventions;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class MotionStubsGenerator {
  public static void generateStubs(Framework framework, File location) {
    final StringBuilder builder = new StringBuilder();
    builder.append("# This is a machine generated stub for: ").append(framework.getName()).append("\n");
    builder.append("# Created on: ").append(DateFormatUtil.formatDateTime(new Date())).append("\n\n");
    for (Class aClass : framework.getClasses()) {
      generateClassStub(builder, framework, aClass);
    }
    for (Function function : framework.getFunctions()) {
      generateFunctionStub(builder, function, "");
    }
    for (Constant constant : framework.getConstants()) {
      generateConstantStub(builder, constant);
    }
    for (Map.Entry<String, String> entry : framework.getFunctionAliases().entrySet()) {
      generateAlias(builder, entry);
    }
    try {
      FileUtil.writeToFile(location, builder.toString());
    } catch (Throwable ignored) {}
  }

  private static void generateAlias(StringBuilder builder, Map.Entry<String, String> entry) {
    builder.append("alias ").append(entry.getKey()).append(" ").append(entry.getValue());
  }

  private static void generateConstantStub(StringBuilder builder, Constant constant) {
    final String name = StringUtil.capitalize(constant.getName());
    builder.append(name).append(" = ");
    if (constant instanceof Enum) {
      builder.append(((Enum)constant).getValue());
    } else if (constant instanceof StringConstant) {
      builder.append("\'").append(((StringConstant)constant).getValue()).append("\'");
    } else {
      builder.append("nil");
    }
    builder.append("\n\n");
  }

  private static void generateClassStub(StringBuilder builder, Framework framework, Class aClass) {
    builder.append("class ").append(aClass.getName());
    final String parent = InheritanceInfoHolder.getInstance().getInheritance(aClass.getName(), framework.getVersion());
    if (parent != null) {
      builder.append(" < ").append(parent);
    }
    builder.append("\n");
    for (Function function : aClass.getFunctions()) {
      generateFunctionStub(builder, function, "  ");
    }
    builder.append("end\n\n");
  }

  private static void generateFunctionStub(StringBuilder builder, Function function, String offset) {
    builder.append(offset).append("# ").append(function.getName()).append("\n");
    for (Pair<String, String> pair : function.getArguments()) {
      builder.append(offset).append("# @param ").append(fixParameterName(pair));
      builder.append(" [").append(pair.second).append("]\n");
    }
    builder.append(offset).append("# @return [").append(function.getReturnValue()).append("]\n");
    builder.append(offset).append("def ").append(MotionSymbolUtil.getSelectorNames(function).get(0)).append("(");
    List<Pair<String, String>> arguments = function.getArguments();
    final String[] namedArguments = function.getName().split(":");
    for (int i = 0; i < arguments.size(); i++) {
      final Pair<String, String> pair = arguments.get(i);
      if (i > 0) {
        builder.append(", ");
        if (namedArguments.length > i) builder.append(namedArguments[i]).append(":");
      }
      builder.append(fixParameterName(pair));
    }
    builder.append(")\n");
    builder.append(offset).append("  # This is a stub, used for indexing\n");
    builder.append(offset).append("end\n\n");
  }

  private static String fixParameterName(Pair<String, String> pair) {
    final String name = NamingConventions.toUnderscoreCase(pair.first);
    return RubyNamesValidator.isKeyword(name) ? "_" + name : name;
  }
}
