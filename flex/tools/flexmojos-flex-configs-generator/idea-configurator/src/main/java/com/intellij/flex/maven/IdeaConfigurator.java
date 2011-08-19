package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.compiler.IASDocConfiguration;
import org.sonatype.flexmojos.compiler.IFlexArgument;
import org.sonatype.flexmojos.compiler.IFlexConfiguration;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.generator.iface.StringUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class IdeaConfigurator {
  protected static final String PATH_ELEMENT = "path-element";
  protected static final String FILE_SPECS = "file-specs";
  public static final String SOURCE_PATH = "source-path";

  protected OutputStreamWriter out;
  protected Build build;

  private static final Map<String, String> CHILD_TAG_NAME_MAP = new HashMap<String, String>(12);

  static {
    CHILD_TAG_NAME_MAP.put("keep-as3-metadata", "name");
    CHILD_TAG_NAME_MAP.put("include-namespaces", "uri");
    CHILD_TAG_NAME_MAP.put("include-classes", "class");
    CHILD_TAG_NAME_MAP.put("include-libraries", "library");
    CHILD_TAG_NAME_MAP.put("locale", "locale-element");
    CHILD_TAG_NAME_MAP.put("managers", "manager-class");
    CHILD_TAG_NAME_MAP.put("externs", "symbol");
    CHILD_TAG_NAME_MAP.put("includes", "symbol");
    CHILD_TAG_NAME_MAP.put("extensions", "extension");
    CHILD_TAG_NAME_MAP.put("include-resource-bundles", "bundle");
    CHILD_TAG_NAME_MAP.put("theme", "filename");
  }

  public void buildConfiguration(Mojo configuration, File sourceFile, Class configurationClass) throws Exception {
    //noinspection NullableProblems
    build(configuration, configurationClass, "\n\t", null);

    out.append("\n\t<file-specs>\n");
    writeTag("\t\t", PATH_ELEMENT, sourceFile.getAbsolutePath(), FILE_SPECS);
    out.append("\n\t</file-specs>");
    close();
  }

  public void buildConfiguration(Mojo configuration, Class configurationClass) throws Exception {
    //noinspection NullableProblems
    build(configuration, configurationClass, "\n\t", null);
    close();
  }

  protected String getConfigFilePath(MavenProject project, String classifier) {
    StringBuilder pathBuilder = new StringBuilder(build.getDirectory()).append(File.separatorChar).append(build.getFinalName());
    if (classifier != null) {
      pathBuilder.append('-').append(classifier);
    }
    //pathBuilder.append("-config-report.xml");
    return pathBuilder.append("-configs.xml").toString();
  }

  public void init(MavenSession session, MavenProject project, String classifier) throws IOException {
    build = project.getBuild();
    File configFile = new File(getConfigFilePath(project, classifier));
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
  private <E> void build(E configuration, Class configClass, String indent, String configurationName) throws Exception {
    boolean parentTagWritten = configurationName == null;

    final Method[] methods = configClass.getDeclaredMethods();
    Arrays.sort(methods, new MethodComparator());

    for (Method method : methods) {
      method.setAccessible(true);
      if (!Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0) {
        continue;
      }

      final String methodName = method.getName();
      if (methodName.equals("getLoadConfig") || methodName.equals("getDumpConfig") || ("metadata".equals(configurationName) && methodName.equals("getDate"))) {
        continue;
      }

      final Object value = method.invoke(configuration);
      if (value == null) {
        continue;
      }

      if ((methodName.equals("getFixedLiteralVector") || methodName.equals("getHeadlessServer")) && !((Boolean)value)) {
        continue;
      }

      if (!parentTagWritten) {
        parentTagWritten = true;
        out.append(indent, 0, indent.length() - 1).append('<').append(configurationName).append('>');
      }

      final Class<?> returnType = method.getReturnType();
      final String name = parseName(methodName);

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

        Field orderField = type.getField("ORDER");
        orderField.setAccessible(true);
        String[] order = (String[])type.getField("ORDER").get(returnType);

        for (IFlexArgument iFlexArgument : values) {
          out.append(indent).append('<').append(name).append('>');

          for (String argMethodName : order) {
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
              writeTag(indent, argMethodName.equals("serialNumber") ? "serial-number" : argMethodName, (String)argValue, name);
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

          String childTagName = CHILD_TAG_NAME_MAP.get(name);
          if (childTagName == null) {
            childTagName = PATH_ELEMENT;
          }

          for (Object v : values) {
            writeTag(indent, childTagName, v.toString(), name);
          }

          if (name.equals(SOURCE_PATH) && "compiler".equals(configurationName)) {
            addGeneratedSources((File[])values, indent);
          }

          out.append(indent).append("</").append(name).append('>');
        }
      }
      else {
        out.append(indent).append('<').append(name).append('>');
        processValue(value.toString(), name);
        out.append("</").append(name).append('>');
      }
    }

    if (parentTagWritten && configurationName != null) {
      out.append(indent, 0, indent.length() - 1).append("</").append(configurationName).append('>');
    }
  }

  private void addGeneratedSources(File[] existing, String indent) throws IOException {
    File generatedSources = new File(build.getDirectory(), "/generated-sources");
    if (!generatedSources.isDirectory()) {
      return;
    }

    final List<File> existingList = Arrays.asList(existing);
    for (File file : generatedSources.listFiles()) {
      if (file.isDirectory() && !file.isHidden() && existingList.indexOf(file) == -1) {
        writeTag(indent, PATH_ELEMENT, file.getAbsolutePath(), SOURCE_PATH);
      }
    }
  }

  protected void processValue(String value, String name) throws IOException {
    out.append(value);
  }

  protected void writeTag(String indent, String name, String value, String parentName) throws IOException {
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