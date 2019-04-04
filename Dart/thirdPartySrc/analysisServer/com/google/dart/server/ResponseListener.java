package com.google.dart.server;

import com.google.gson.JsonObject;

/**
 * Listener for arbitrary responses received from the analysis server.
 */
public interface ResponseListener {
  void onResponse(JsonObject json);
}
