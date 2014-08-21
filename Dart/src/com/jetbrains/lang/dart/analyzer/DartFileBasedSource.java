package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.internal.context.TimestampedData;
import com.google.dart.engine.source.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class DartFileBasedSource implements Source {
  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartFileBasedSource");

  private final @NotNull VirtualFile myFile;
  private String encoding;
  private final URI myUri;
  private long myModificationStampWhenFileContentWasRead = -1;

  private DartFileBasedSource(final @NotNull VirtualFile file)
    throws URISyntaxException, MalformedURLException {
    myFile = file;
    String url = file.getUrl();
    URI uri = null;
    try {
      uri = new URL(url).toURI();
    }
    catch (MalformedURLException e) {
      // Tests create dummy files with the temp protocol
      if (url.startsWith("temp:")) {
        url = url.replaceFirst("temp:", "file:");
        uri = new URL(url).toURI();
      }
    }
    myUri = uri;
  }

  @NotNull
  public VirtualFile getFile() {
    return myFile;
  }

  public boolean isOutOfDate() {
    return myModificationStampWhenFileContentWasRead == -1 || myModificationStampWhenFileContentWasRead != getModificationStamp();
  }

  @Override
  public boolean equals(final Object object) {
    return object != null && this.getClass() == object.getClass()
           && myFile.equals(((DartFileBasedSource)object).myFile);
  }

  @Override
  public boolean exists() {
    return myFile.exists() && !myFile.isDirectory();
  }

  public TimestampedData<CharSequence> getContents() throws Exception {
    final Pair<CharSequence, Long> contentsAndTimestamp = loadFile(myFile);
    myModificationStampWhenFileContentWasRead = contentsAndTimestamp.second;
    return new TimestampedData<CharSequence>(contentsAndTimestamp.second, contentsAndTimestamp.first);
  }

  @Override
  public void getContentsToReceiver(final ContentReceiver receiver) throws Exception {
    final Pair<CharSequence, Long> contentsAndTimestamp = loadFile(myFile);
    myModificationStampWhenFileContentWasRead = contentsAndTimestamp.second;
    receiver.accept(contentsAndTimestamp.first, contentsAndTimestamp.second);
  }

  @Override
  public String getEncoding() {
    if (encoding == null) {
      encoding = myUri.toString();
    }
    return encoding;
  }

  @Override
  public String getFullName() {
    return myFile.getPath();
  }

  @Override
  public long getModificationStamp() {
    final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(myFile);
    if (cachedDocument != null) {
      return cachedDocument.getModificationStamp();
    }
    else {
      return myFile.getModificationStamp();
    }
  }

  @Override
  public String getShortName() {
    return myFile.getName();
  }

  @Override
  public URI getUri() {
    return myUri;
  }

  @NotNull
  @Override
  public UriKind getUriKind() {
    String scheme = myUri.getScheme();
    if (scheme.equals(PackageUriResolver.PACKAGE_SCHEME)) {
      return UriKind.PACKAGE_URI;
    }
    else if (scheme.equals(DartUriResolver.DART_SCHEME)) {
      return UriKind.DART_URI;
    }
    else if (scheme.equals(FileUriResolver.FILE_SCHEME)) {
      return UriKind.FILE_URI;
    }
    return UriKind.FILE_URI;
  }

  @Override
  public int hashCode() {
    return myFile.hashCode();
  }

  @Override
  public boolean isInSystemLibrary() {
    return false;
  }

  @Override
  public URI resolveRelativeUri(URI containedUri) throws AnalysisException {
    try {
      URI baseUri = myUri;
      boolean isOpaque = myUri.isOpaque();
      if (isOpaque) {
        String scheme = myUri.getScheme();
        String part = myUri.getRawSchemeSpecificPart();
        if (scheme.equals(DartUriResolver.DART_SCHEME) && part.indexOf('/') < 0) {
          part = part + "/" + part + ".dart";
        }
        baseUri = new URI(scheme + ":/" + part);
      }
      URI result = baseUri.resolve(containedUri).normalize();
      if (isOpaque) {
        result = new URI(result.getScheme() + ":" + result.getRawSchemeSpecificPart().substring(1));
      }
      return result;
    }
    catch (Exception exception) {
      throw new AnalysisException("Could not resolve URI (" + containedUri
                                  + ") relative to source (" + myUri + ")", exception);
    }
  }

  @Override
  public String toString() {
    return myFile.getPath();
  }

  private static Pair<CharSequence, Long> loadFile(final VirtualFile file) throws Exception {
    final Ref<CharSequence> contentsRef = Ref.create();
    final Ref<Long> timestampRef = Ref.create();
    final Exception exception = ApplicationManager.getApplication().runReadAction(new NullableComputable<Exception>() {
      @Nullable
      public Exception compute() {
        final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(file);
        if (cachedDocument != null) {
          contentsRef.set(cachedDocument.getCharsSequence());
          timestampRef.set(cachedDocument.getModificationStamp());
        }
        else {
          try {
            contentsRef.set(VfsUtilCore.loadText(file));
            timestampRef.set(file.getModificationStamp());
          }
          catch (IOException e) {
            return e;
          }
        }
        return null;
      }
    });

    if (exception != null) throw exception;

    return Pair.create(contentsRef.get(), timestampRef.get());
  }

  @Nullable
  public static DartFileBasedSource getSource(final @NotNull Project project, final @NotNull VirtualFile file) {
    try {
      final DartFileBasedSource source = new DartFileBasedSource(file);
      return DartAnalyzerService.getInstance(project).getOrCreateSource(file, new Function<VirtualFile, DartFileBasedSource>() {
        public DartFileBasedSource fun(final VirtualFile file) {
          return source;
        }
      });
    }
    catch (URISyntaxException e) {
      LOG.error(e);
    }
    catch (MalformedURLException e) {
      LOG.error(e);
    }
    return null;
  }
}
