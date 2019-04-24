package com.google.dart.server;

/**
 * Listener for arbitrary responses received from the analysis server.
 */
public interface ResponseListener {
  void onResponse(String jsonString);
}
