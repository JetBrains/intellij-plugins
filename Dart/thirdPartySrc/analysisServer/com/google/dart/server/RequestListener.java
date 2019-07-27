// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.dart.server;

/**
 * Listener for arbitrary requests sent from the analysis server.
 */
public interface RequestListener {
  void onRequest(String jsonString);
}
