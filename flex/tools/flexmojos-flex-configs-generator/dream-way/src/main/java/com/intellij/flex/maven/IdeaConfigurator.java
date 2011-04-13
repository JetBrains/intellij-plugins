package com.intellij.flex.maven;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.compiler.*;
import org.sonatype.flexmojos.generator.iface.StringUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IdeaConfigurator {
  private OutputStreamWriter out;

  private static final Map<String, String> childTagNameMap = new HashMap<String, String>(9);

  static {
    childTagNameMap.put("keep-as3-metadata", "name");
    childTagNameMap.put("include-namespaces", "uri");
    childTagNameMap.put("include-classes", "class");
    childTagNameMap.put("include-libraries", "library");
    childTagNameMap.put("locale", "locale-element");
    childTagNameMap.put("managers", "manager-class");
    childTagNameMap.put("externs", "symbol");
    childTagNameMap.put("includes", "symbol");
    childTagNameMap.put("extensions", "extension");
    childTagNameMap.put("include-resource-bundles", "bundle");
    childTagNameMap.put("theme", "filename");
  }

  public void buildConfiguration(ICommandLineConfiguration configuration, File sourceFile) throws Exception {
    build(configuration, ICommandLineConfiguration.class, "\n\t", null);
    out.append("\n\t<file-specs>\n\t\t<path-element>").append(sourceFile.getAbsolutePath()).append("</path-element>\n\t</file-specs>");
    close();
  }

  public void buildConfiguration(ICompcConfiguration configuration) throws Exception {
    build(configuration, ICompcConfiguration.class, "\n\t", null);
    close();
  }

  public void init(MavenProject project, String classifier) throws IOException {
    Build build = project.getBuild();
    StringBuilder pathBuilder = new StringBuilder(build.getDirectory()).append(File.separatorChar).append(build.getFinalName());
    if (classifier != null) {
      pathBuilder.append('-').append(classifier);
    }
//    pathBuilder.append("-config-report.xml");
    pathBuilder.append("-configs.xml");

    File configFile = new File(pathBuilder.toString());
    //noinspection ResultOfMethodCallIgnored
    configFile.getParentFile().mkdirs();
    out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(configFile)), "utf-8");
    out.write("<flex-config xmlns=\"http://www.adobe.com/2006/flex-config\">");
  }

  private void close() throws IOException {
    out.write("\n</flex-config>");
    out.close();
  }

  @SuppressWarnings({"ConstantConditions"})
  private <E> void build(E configuration, Class<? extends E> configClass, String indent, String configurationName) throws Exception {
    boolean parentTagWritten = configurationName == null;

    for (Method method : configClass.getDeclaredMethods()) {
      if (method.getParameterTypes().length != 0 || !Modifier.isPublic(method.getModifiers())) {
        continue;
      }

      String methodName = method.getName();
      if (methodName.equals("getLoadConfig") || methodName.equals("getDumpConfig")) {
        continue;
      }

      Object value = method.invoke(configuration);
      if (value == null) {
        continue;
      }

      if (methodName.equals("getFixedLiteralVector") && !((Boolean)value)) {
        continue;
      }

      if (!parentTagWritten) {
        parentTagWritten = true;
        out.append(indent, 0, indent.length() - 1).append('<').append(configurationName).append('>');
      }

      Class<?> returnType = method.getReturnType();
      String name = parseName(methodName);

      if (value instanceof IFlexConfiguration) {
        build(value, returnType, indent + "\t", name.substring(0, name.length() - 14));
      }
      else if (configuration instanceof IASDocConfiguration && "footer".equals(name)) {
        // todo
        throw new UnsupportedOperationException();
      }
      else if (value instanceof IRuntimeSharedLibraryPath || value instanceof IRuntimeSharedLibraryPath[]) {
        final IRuntimeSharedLibraryPath[] values;
        if (returnType.isArray()) {
          //noinspection ConstantConditions
          values = (IRuntimeSharedLibraryPath[])value;
        }
        else {
          //noinspection ConstantConditions
          values = new IRuntimeSharedLibraryPath[]{(IRuntimeSharedLibraryPath)value};
        }

        for (IRuntimeSharedLibraryPath arg : values) {
          out.append("\n\t<").append(name).append(">\n\t\t<path-element>").append(arg.pathElement()).append("</path-element>");

          //noinspection unchecked
          for (Map.Entry<String, String> entry : (Set<Map.Entry<String, String>>)arg.rslUrl().entrySet()) {
            out.append("\n\t\t<rsl-url>").append(entry.getKey()).append("</rsl-url>");

            if (entry.getValue() != null) {
              out.append("\n\t\t<policy-file-url>").append(entry.getValue()).append("</policy-file-url>");
            }
          }

          out.append("\n\t</").append(name).append('>');
        }
      }
      else if (value instanceof IFlexArgument || value instanceof IFlexArgument[]) {
        IFlexArgument[] values;
        Class<?> type = returnType;
        if (type.isArray()) {
          //noinspection ConstantConditions
          values = (IFlexArgument[])value;
          type = returnType.getComponentType();
        }
        else {
          //noinspection ConstantConditions
          values = new IFlexArgument[]{(IFlexArgument)value};
          type = returnType;
        }

        for (IFlexArgument iFlexArgument : values) {
          out.append(indent).append('<').append(name).append('>');

          for (String argMethodName : (String[])type.getField("ORDER").get(iFlexArgument)) {
            Object argValue = type.getDeclaredMethod(argMethodName).invoke(iFlexArgument);
            if (argValue == null) {
              throw new UnsupportedOperationException();
            }
            else if (argValue instanceof Collection<?> || argValue.getClass().isArray()) {
              throw new UnsupportedOperationException();
            }
            else if (argValue instanceof Map<?, ?>) {
              throw new UnsupportedOperationException();
//              Map<?, ?> map = ((Map<?, ?>) argValue);
//              for (Object argValue1 : map.entrySet()) {
//                @SuppressWarnings({"unchecked"}) Map.Entry<String, ?> entry = (Map.Entry<String, ?>) argValue1;
//                out.append(indent).append("\t<").append(entry.getKey()).append('>').append(entry.getValue().toString()).append("</").append(entry.getKey()).append('>');
//              }
            }
            else {
              writeTag(indent, argMethodName, (String)argValue);
            }
          }

          out.append(indent).append("</").append(name).append('>');
        }
      }
      else if (configuration instanceof IMetadataConfiguration &&
               (name.equals("language") || name.equals("creator") || name.equals("publisher"))) {
        for (String v : (String[])value) {
          out.append(indent).append("<").append(name).append(">").append(v).append("</").append(name).append('>');
        }
      }
      else if (returnType.isArray() || value instanceof Collection<?>) {
        Object[] values;
        if (returnType.isArray()) {
          //noinspection ConstantConditions
          values = (Object[])value;
        }
        else {
          values = ((Collection<?>)value).toArray();
        }

        out.append(indent).append('<').append(name);

        // fucking adobe, ability to compile pure AS3 project without fucking themes — node must be present, but empty (relevant only for "theme", but we are ready for adobe surprises)
        if (values.length == 0) {
          out.append("/>");
        }
        else {
          out.append('>');

          String childTagName = childTagNameMap.get(name);
          if (childTagName == null) {
            childTagName = "path-element";
          }

          for (Object v : values) {
            writeTag(indent, childTagName, v.toString());
          }
          out.append(indent).append("</").append(name).append('>');
        }
      }
      else {
        out.append(indent).append('<').append(name).append('>');
        out.append(value.toString());
        out.append("</").append(name).append('>');
      }
    }

    if (parentTagWritten && configurationName != null) {
      out.append(indent, 0, indent.length() - 1).append("</").append(configurationName).append('>');
    }
  }

  private void writeTag(String indent, String name, String value) throws IOException {
    out.append(indent).append("\t<").append(name).append(">").append(value).append("</").append(name).append('>');
  }

  private static String parseName(String name) {
    String[] nodes = StringUtil.splitCamelCase(StringUtil.removePrefix(name));

    StringBuilder finalName = new StringBuilder();
    for (String node : nodes) {
      if (finalName.length() != 0) {
        finalName.append('-');
      }
      finalName.append(node.toLowerCase());
    }

    return finalName.toString();
  }
}