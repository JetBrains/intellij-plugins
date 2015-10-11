/*
 * Copyright (c) 2014, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.AnalysisStatus;
import org.dartlang.analysis.server.protocol.PubStatus;

/**
 * Processor for "server.status" notification.
 * 
 * @coverage dart.server.remote
 */
public class NotificationServerStatusProcessor extends NotificationProcessor {

  public NotificationServerStatusProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  @Override
  public void process(JsonObject response) throws Exception {
    JsonObject paramsObject = response.get("params").getAsJsonObject();
    AnalysisStatus analysisStatus = getAnalysisStatus(paramsObject);
    PubStatus pubStatus = getPubStatus(paramsObject);
    getListener().serverStatus(analysisStatus, pubStatus);
  }

  private AnalysisStatus getAnalysisStatus(JsonObject paramsObject) {
    JsonElement analysisMember = paramsObject.get("analysis");
    if (analysisMember == null) {
      return null;
    }
    JsonObject analysisObject = analysisMember.getAsJsonObject();
    boolean isAnalyzing = analysisObject.get("isAnalyzing").getAsBoolean();
    String analysisTarget = safelyGetAsString(analysisObject, "analysisTarget");
    return new AnalysisStatus(isAnalyzing, analysisTarget);
  }

  private PubStatus getPubStatus(JsonObject paramsObject) {
    JsonElement pubMember = paramsObject.get("pub");
    if (pubMember == null) {
      return null;
    }
    JsonObject pubObject = pubMember.getAsJsonObject();
    boolean isListingPackageDirs = pubObject.get("isListingPackageDirs").getAsBoolean();
    return new PubStatus(isListingPackageDirs);
  }
}
