/*
 * Copyright (c) 2015, the Dart project authors.
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
package org.dartlang.vm.service.element;

// This is a generated file.

/**
 * Adding new values to {@link InstanceKind} is considered a backwards compatible change. Clients
 * should treat unrecognized instance kinds as {@link PlainInstance}.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public enum InstanceKind {

  /**
   * true or false.
   */
  Bool,

  /**
   * An instance of the Dart class BoundedType.
   */
  BoundedType,

  /**
   * An instance of the built-in VM Closure implementation. User-defined Closures will be
   * PlainInstance.
   */
  Closure,

  /**
   * An instance of the Dart class double.
   */
  Double,

  Float32List,

  /**
   * Vector instance kinds.
   */
  Float32x4,

  Float32x4List,

  Float64List,

  Float64x2,

  Float64x2List,

  /**
   * An instance of the Dart class int.
   */
  Int,

  Int16List,

  Int32List,

  Int32x4,

  Int32x4List,

  Int64List,

  Int8List,

  /**
   * An instance of the built-in VM List implementation. User-defined Lists will be PlainInstance.
   */
  List,

  /**
   * An instance of the built-in VM Map implementation. User-defined Maps will be PlainInstance.
   */
  Map,

  /**
   * An instance of the Dart class MirrorReference.
   */
  MirrorReference,

  /**
   * null instance.
   */
  Null,

  /**
   * A general instance of the Dart class Object.
   */
  PlainInstance,

  /**
   * An instance of the Dart class RegExp.
   */
  RegExp,

  /**
   * An instance of the Dart class StackTrace.
   */
  StackTrace,

  /**
   * An instance of the Dart class String.
   */
  String,

  /**
   * An instance of the Dart class Type.
   */
  Type,

  /**
   * An instance of the Dart class TypeParameter.
   */
  TypeParameter,

  /**
   * An instance of the Dart class TypeRef.
   */
  TypeRef,

  Uint16List,

  Uint32List,

  Uint64List,

  /**
   * An instance of the built-in VM TypedData implementations. User-defined TypedDatas will be
   * PlainInstance.
   */
  Uint8ClampedList,

  Uint8List,

  /**
   * An instance of the Dart class WeakProperty.
   */
  WeakProperty,

  /**
   * Represents a value returned by the VM but unknown to this client.
   */
  Unknown
}
