/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.protobuf;

/**
 * Stub for the base class of proto2 generated classes. The open source version is actually
 * GeneratedMessageV3, and in the future there will probably be a V4.
 *
 * <p>The integration tests require resolving parent classes, hence we add these stubs.
 */
public abstract class GeneratedMessage {

  /** Builder. */
  public abstract static class Builder<BuilderType extends Builder<BuilderType>> {}
}
