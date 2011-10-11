package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.compiler.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class IdeaConfigurator implements FlexConfigGenerator {
  private static final Map<String, String> CHILD_TAG_NAME_MAP = new HashMap<String, String>(12);
  
  protected static final String PATH_ELEMENT = "path-element";
  protected static final String FILE_SPECS = "file-specs";
  private static final String SOURCE_PATH = "source-path";
  protected static final String LOCAL_FONTS_SNAPSHOT = "local-fonts-snapshot";
  private static final String FONTS_SER = "fonts.ser";

  protected final MavenSession session;

  protected Writer out;
  private Build build;

  private MojoExecution flexmojosGeneratorMojoExecution;
  private ExpressionEvaluator flexmojosGeneratorExpressionEvaluator;

  protected final File outputDirectory;
  protected File sharedFontsSer;
  protected String sharedFontsSerPath;

  public IdeaConfigurator(MavenSession session, File outputDirectory) {
    this.session = session;
    this.outputDirectory = outputDirectory;
    //noinspection ResultOfMethodCallIgnored
    outputDirectory.mkdirs();
  }

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

  @Override
  public void generate(Mojo configuration, File sourceFile) throws Exception {
    //noinspection NullableProblems
    build(configuration, ICommandLineConfiguration.class, "\n\t", null);

    out.append("\n\t<file-specs>\n");
    writeTag("\t\t", PATH_ELEMENT, sourceFile.getAbsolutePath(), FILE_SPECS);
    out.append("\n\t</file-specs>");
  }

  @Override
  public void generate(Mojo configuration) throws Exception {
    build(configuration, ICompcConfiguration.class, "\n\t", null);
  }

  @Override
  public void preGenerate(MavenProject project, String classifier, MojoExecution flexmojosGeneratorMojoExecution) throws IOException {
    this.flexmojosGeneratorMojoExecution = flexmojosGeneratorMojoExecution;
    build = project.getBuild();
    File configFile = new File(getConfigFilePath(project, classifier));
    //noinspection ResultOfMethodCallIgnored
    configFile.getParentFile().mkdirs();
    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "utf-8"));
    out.write("<flex-config xmlns=\"http://www.adobe.com/2006/flex-config\">");
  }

  @Override
  public void postGenerate() {
    build = null;
    flexmojosGeneratorMojoExecution = null;
    flexmojosGeneratorExpressionEvaluator = null;

    if (out == null) {
      return;
    }

    try {
      out.write("\n</flex-config>");
      out.close();
    }
    catch (IOException ignored) {
    }

    out = null;
  }

  protected String getConfigFilePath(MavenProject project, String classifier) {
    StringBuilder pathBuilder = new StringBuilder(outputDirectory.getAbsolutePath()).append(File.separatorChar);
    // artifact id is first in path — it is convenient for us
    pathBuilder.append(project.getArtifactId()).append('-').append(project.getGroupId());
    if (classifier != null) {
      pathBuilder.append('-').append(classifier);
    }
    return pathBuilder.append("-config.xml").toString();
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
    final List<File> existingList;
    if (flexmojosGeneratorMojoExecution == null) {
      existingList = Arrays.asList(existing);
    }
    else {
      existingList = new ArrayList<File>();
      Collections.addAll(existingList, existing);

      PlexusConfiguration configuration = new XmlPlexusConfiguration(flexmojosGeneratorMojoExecution.getConfiguration());
      writeGeneratedSource(configuration, "baseOutputDirectory", existingList, indent);
      writeGeneratedSource(configuration, "outputDirectory", existingList, indent);
    }

    File generatedSources = new File(build.getDirectory(), "/generated-sources");
    if (!generatedSources.isDirectory()) {
      return;
    }

    for (File file : generatedSources.listFiles()) {
      writeGeneratedSource(file, existingList, indent);
    }
  }

  private void writeGeneratedSource(PlexusConfiguration parentConfiguration, String parameterName, List<File> existingList, String indent)
    throws IOException {
    final PlexusConfiguration configuration = parentConfiguration.getChild(parameterName);
    if (configuration == null) {
      return;
    }
    
    String filepath = configuration.getValue();
    if (filepath == null) {
      final String defaultValue = configuration.getAttribute("default-value");
      if (defaultValue == null) {
        return;
      }

      if (flexmojosGeneratorExpressionEvaluator == null) {
        flexmojosGeneratorExpressionEvaluator = new PluginParameterExpressionEvaluator(session, flexmojosGeneratorMojoExecution);
      }
      
      try {
        filepath = (String)flexmojosGeneratorExpressionEvaluator.evaluate(defaultValue);
      }
      catch (ExpressionEvaluationException e) {
        throw new RuntimeException(e);
      }
    }

    if (filepath != null) {
      File file = new File(filepath);
      if (!existingList.contains(file)) {
        writeTag(indent, PATH_ELEMENT, file.getAbsolutePath(), SOURCE_PATH);
      }
    }
  }
    
  private void writeGeneratedSource(File file, List<File> existingList, String indent) throws IOException {
    if (file.isDirectory() && !file.isHidden() && !existingList.contains(file)) {
      writeTag(indent, PATH_ELEMENT, file.getAbsolutePath(), SOURCE_PATH);
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
        if (sharedFontsSer == null) {
          sharedFontsSer = new File(outputDirectory, FONTS_SER);
          if (!sharedFontsSer.exists()) {
            FileUtils.copyFile(fontsSer, sharedFontsSer);
          }

          sharedFontsSerPath = sharedFontsSer.getAbsolutePath();
        }

        value = sharedFontsSerPath;
      }
    }

    out.append(value);
  }

  protected void writeTag(String indent, String name, String value, String parentName) throws IOException {
    out.append(indent).append("\t<").append(name).append(">").append(value).append("</").append(name).append('>');
  }

  private static String camelCaseToSnake(final String s) {
    StringBuilder builder = new StringBuilder(s.length() + 4 /* probable max hyphen count */);
    boolean isFirst = true;
    for (int i = removePrefix(s), n = s.length(); i < n; i++) {
      char c = s.charAt(i);
      if (Character.isUpperCase(c)) {
        if (!isFirst) {
          builder.append('-');
        }
        else {
          isFirst = false;
        }
        builder.append(Character.toLowerCase(c));
      }
      else {
        builder.append(c);
      }
    }

    return builder.toString();
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