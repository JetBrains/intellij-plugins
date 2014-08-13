package org.osmorc.maven.facet;

/**
 * Exception thrown by the dependency embedder in case something is not readable.
 */
public class DependencyEmbedderException extends Exception {
  public DependencyEmbedderException(String message) {
    super(message);
  }
}
