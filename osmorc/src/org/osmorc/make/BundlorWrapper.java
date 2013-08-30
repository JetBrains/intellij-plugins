package org.osmorc.make;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.springsource.bundlor.ClassPath;
import com.springsource.bundlor.ManifestGenerator;
import com.springsource.bundlor.ManifestWriter;
import com.springsource.bundlor.ant.internal.support.StandardManifestTemplateFactory;
import com.springsource.bundlor.blint.support.DefaultManifestValidatorContributorsFactory;
import com.springsource.bundlor.blint.support.StandardManifestValidator;
import com.springsource.bundlor.support.DefaultManifestGeneratorContributorsFactory;
import com.springsource.bundlor.support.StandardManifestGenerator;
import com.springsource.bundlor.support.classpath.StandardClassPathFactory;
import com.springsource.bundlor.support.manifestwriter.StandardManifestWriterFactory;
import com.springsource.bundlor.support.properties.PropertiesPropertiesSource;
import com.springsource.bundlor.support.properties.PropertiesSource;
import com.springsource.util.parser.manifest.ManifestContents;
import org.jetbrains.annotations.NotNull;
import org.osmorc.util.MavenIntegrationUtil;

import java.util.List;
import java.util.Properties;

/**
 * A wrapper around SpringSource's Bundlor tool.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BundlorWrapper {
  public boolean wrapModule(@NotNull CompileContext context,
                            @NotNull String inputJar,
                            @NotNull String outputJar,
                            @NotNull String manifestTemplateFile) {
    Properties properties = MavenIntegrationUtil.getMavenProjectProperties(context.getProject(), context.getCompileScope().getAffectedModules());
    PropertiesSource propertiesSource = new PropertiesPropertiesSource(properties);

    ManifestGenerator generator = new StandardManifestGenerator(DefaultManifestGeneratorContributorsFactory.create(propertiesSource));
    ClassPath classPath = new StandardClassPathFactory().create(inputJar);
    ManifestContents contents = new StandardManifestTemplateFactory().create(manifestTemplateFile, null, null, null);
    ManifestContents manifest = generator.generate(contents, classPath);

    ManifestWriter manifestWriter = new StandardManifestWriterFactory().create(inputJar, outputJar);
    try {
      manifestWriter.write(manifest);
    }
    catch (Exception e) {
      context.addMessage(CompilerMessageCategory.ERROR, "Error writing manifest: " + e.getMessage(), null, 0, 0);
      return false;
    }
    finally {
      manifestWriter.close();
    }

    List<String> warningsList = new StandardManifestValidator(DefaultManifestValidatorContributorsFactory.create()).validate(manifest);
    for (String s : warningsList) {
      context.addMessage(CompilerMessageCategory.WARNING, s, VfsUtilCore.pathToUrl(manifestTemplateFile), 0, 0);
    }

    return true;
  }
}
