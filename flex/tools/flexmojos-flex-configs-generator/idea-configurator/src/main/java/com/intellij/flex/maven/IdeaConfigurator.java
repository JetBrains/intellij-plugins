package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.compiler.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class IdeaConfigurator implements FlexConfigGenerator {
  protected static final String PATH_ELEMENT = "path-element";
  protected static final String FILE_SPECS = "file-specs";
  protected static final String LOCAL_FONTS_SNAPSHOT = "local-fonts-snapshot";
  private static final String FONTS_SER = "fonts.ser";

  protected final MavenSession session;

  protected final StringBuilder out;
  private Build build;

  private String classifier;

  protected final File outputDirectory;
  @SuppressWarnings("StaticNonFinalField")
  protected static File sharedFontsSer;
  @SuppressWarnings("StaticNonFinalField")
  protected static String sharedFontsSerPath;

  public IdeaConfigurator(MavenSession session, File outputDirectory) {
    this.session = session;
    this.outputDirectory = outputDirectory;

    out = new StringBuilder(8192);
    out.append("<flex-config xmlns=\"http://www.adobe.com/2006/flex-config\">");
  }

  @Override
  public void generate(Mojo configuration, File sourceFile) throws Exception {
    build(configuration, ICommandLineConfiguration.class, "\n\t", null);

    out.append("\n\t<file-specs>\n");
    writeTag("\t", PATH_ELEMENT, sourceFile.getAbsolutePath(), FILE_SPECS);
    out.append("\n\t</file-specs>");
  }

  @Override
  public void generate(Mojo configuration) throws Exception {
    build(configuration, ICompcConfiguration.class, "\n\t", null);
  }

  @Override
  public void preGenerate(MavenProject project, String classifier) throws IOException {
    this.classifier = classifier;
    build = project.getBuild();
  }

  @Override
  public String postGenerate(MavenProject project) throws IOException {
    out.append("\n</flex-config>");
    final String configFile = getConfigFilePath(project, classifier);
    Utils.write(out, new File(outputDirectory, configFile));
    return configFile;
  }

  protected String getConfigFilePath(MavenProject project, String classifier) {
    // artifact id is first in path — it is convenient for us
    StringBuilder pathBuilder = new StringBuilder(32).append(project.getArtifactId()).append('-').append(project.getGroupId());
    if (classifier != null) {
      pathBuilder.append('-').append(classifier);
    }
    return pathBuilder.append(".xml").toString();
  }

  @SuppressWarnings({"ConstantConditions"})
  private <E> void build(E configuration, Class configClass, String indent, String configurationName) throws Exception {
    boolean parentTagWritten = configurationName == null;

    final Method[] methods = configClass.getMethods();
    Arrays.sort(methods, new Comparator<Method>() {
      @Override
      public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (Method method : methods) {
      method.setAccessible(true);
      if (method.getParameterTypes().length != 0) {
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
      final String name = camelCaseToSnake(methodName);

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
          values = (Object[])value;
        }
        else {
          values = ((Collection<?>)value).toArray();
        }



        // ability to compile pure AS3 project without themes — node must be present, but empty (relevant only for "theme")
        if (values.length == 0) {
          if (name.equals("theme") || name.equals("locale")) {
            out.append(indent).append('<').append(name).append("/>");
          }
        }
        else {
          out.append(indent).append('<').append(name);
          if (Utils.APPENDABLE.contains(name)) {
            out.append(" append=\"true\"");
          }
          out.append('>');

          String childTagName = Utils.CHILD_TAG_NAME_MAP.get(name);
          if (childTagName == null) {
            childTagName = PATH_ELEMENT;
          }

          for (Object v : values) {
            if (v == null) {
              System.out.print('\n' + childTagName + " child value for " + name + " is null\n");
            }
            if (v != null) {
              writeTag(indent, childTagName, v.toString(), name);
            }
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

  protected void processValue(String value, String name) throws IOException {
    // http://juick.com/develar/1363289
    if (name.equals(LOCAL_FONTS_SNAPSHOT)) {
      final File fontsSer = new File(build.getOutputDirectory(), FONTS_SER);
      String defaultPath;
      // the same as flexmojos does
      try {
        defaultPath = fontsSer.getCanonicalPath();
      }
      catch (IOException e) {
        defaultPath = fontsSer.getAbsolutePath();
      }

      if (value.equals(defaultPath)) {
        if (sharedFontsSerPath == null) {
          sharedFontsSer = new File(outputDirectory, FONTS_SER);
          sharedFontsSerPath = sharedFontsSer.getPath();
          if (!sharedFontsSer.exists()) {
            Utils.copyFile(fontsSer, sharedFontsSer);
          }
        }

        value = sharedFontsSerPath;
      }
    }

    out.append(value);
  }

  protected void writeTag(String indent, String name, String value, @SuppressWarnings("UnusedParameters") String parentName) throws IOException {
    out.append(indent).append("\t<").append(name).append(">").append(value).append("</").append(name).append('>');
  }

  private static String camelCaseToSnake(final String s) {
    StringBuilder builder = new StringBuilder(s.length() + 4 /* probable max hyphen count */);
    for (int i = removePrefix(s), n = s.length(); i < n; i++) {
      char c = s.charAt(i);
      if (Character.isUpperCase(c)) {
        builder.append('-').append(Character.toLowerCase(c));
      }
      else {
        builder.append(c);
      }
    }

    return builder.substring(builder.charAt(0) == '-' ? 1 : 0);
  }

  private static int removePrefix(String s) {
    int cut = 0;
    for (int i = 0; i < s.length(); i++) {
      if (Character.isUpperCase(s.charAt(i))) {
        cut = i;
        break;
      }
    }

    return cut;
  }
}