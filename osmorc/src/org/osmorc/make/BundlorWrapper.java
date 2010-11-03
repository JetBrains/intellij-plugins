package org.osmorc.make;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.VfsUtil;
import com.springsource.bundlor.ClassPath;
import com.springsource.bundlor.ManifestGenerator;
import com.springsource.bundlor.ManifestWriter;
import com.springsource.bundlor.ant.internal.support.StandardManifestTemplateFactory;
import com.springsource.bundlor.blint.support.DefaultManifestValidatorContributorsFactory;
import com.springsource.bundlor.blint.support.StandardManifestValidator;
import com.springsource.bundlor.support.DefaultManifestGeneratorContributorsFactory;
import com.springsource.bundlor.support.StandardManifestGenerator;
import com.springsource.bundlor.support.classpath.ClassPathFactory;
import com.springsource.bundlor.support.classpath.StandardClassPathFactory;
import com.springsource.bundlor.support.manifestwriter.StandardManifestWriterFactory;
import com.springsource.util.parser.manifest.ManifestContents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A wrapper around SpringSource's Bundlor tool.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class BundlorWrapper {
  public boolean wrapModule(@NotNull CompileContext compileContext,
                            @NotNull String inputJar,
                            @NotNull String outputJar,
                            @NotNull String manifestTemplateFile) {
    ManifestWriter manifestWriter = new StandardManifestWriterFactory().create(inputJar, outputJar);
    ManifestGenerator manifestGenerator = new StandardManifestGenerator(DefaultManifestGeneratorContributorsFactory.create());
    ClassPathFactory cpf = new StandardClassPathFactory();
    ClassPath classPath = cpf.create(inputJar);

    StandardManifestValidator manifestValidator = new StandardManifestValidator(DefaultManifestValidatorContributorsFactory.create());

    ManifestContents manifest =
      manifestGenerator.generate(new StandardManifestTemplateFactory().create(manifestTemplateFile, null, null, null), classPath);

    try {
      manifestWriter.write(manifest);
    }
    catch (Exception e) {
      compileContext.addMessage(CompilerMessageCategory.ERROR, "Error writing manifest: " + e.getMessage(), null, 0, 0);
      return false;
    }
    finally {
      manifestWriter.close();
    }

    final List<String> warningsList = manifestValidator.validate(manifest);
    for (String s : warningsList) {
      compileContext.addMessage(CompilerMessageCategory.WARNING, s, VfsUtil.pathToUrl(manifestTemplateFile), 0, 0);
    }
    return true;
  }


}
