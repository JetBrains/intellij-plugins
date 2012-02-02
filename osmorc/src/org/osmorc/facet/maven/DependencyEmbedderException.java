package org.osmorc.facet.maven;

/**
 * Exception thrown by the dependency embedder in case something is not readable.
 */
public class DependencyEmbedderException extends Exception {

  public DependencyEmbedderException() {
    super();
  }

  public DependencyEmbedderException(String message) {
    super(message);
  }

  public DependencyEmbedderException(String message, Throwable cause) {
    super(message, cause);
  }

  public DependencyEmbedderException(Throwable cause) {
    super(cause);
  }
}
