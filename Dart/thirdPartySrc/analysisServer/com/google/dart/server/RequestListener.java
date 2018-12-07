package com.google.dart.server;

import com.google.gson.JsonObject;

/**
 * Listener for arbitrary requests sent from the analysis server.
 */
public interface RequestListener {
  void onRequest(JsonObject json);
}
