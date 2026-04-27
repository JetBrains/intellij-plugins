import datetime
from collections.abc import Iterable as _Iterable, Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

from google.protobuf import any_pb2 as _any_pb2
from google.protobuf import api_pb2 as _api_pb2
from google.protobuf import descriptor as _descriptor
from google.protobuf import duration_pb2 as _duration_pb2
from google.protobuf import empty_pb2 as _empty_pb2
from google.protobuf import field_mask_pb2 as _field_mask_pb2
from google.protobuf import message as _message
from google.protobuf import source_context_pb2 as _source_context_pb2
from google.protobuf import struct_pb2 as _struct_pb2
from google.protobuf import timestamp_pb2 as _timestamp_pb2
from google.protobuf import type_pb2 as _type_pb2
from google.protobuf import wrappers_pb2 as _wrappers_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper

import import_from_pb2 as _import_from_pb2

DESCRIPTOR: _descriptor.FileDescriptor


class SomeEnum(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
  __slots__ = ()
  ENUM_VALUE_UNSPECIFIED: _ClassVar[SomeEnum]
  ENUM_VALUE_1: _ClassVar[SomeEnum]
  ENUM_VALUE_ALIAS: _ClassVar[SomeEnum]
  ENUM_VALUE_DEPRECATED: _ClassVar[SomeEnum]
  ENUM_VALUE_CUSTOM_OPT: _ClassVar[SomeEnum]


ENUM_VALUE_UNSPECIFIED: SomeEnum
ENUM_VALUE_1: SomeEnum
ENUM_VALUE_ALIAS: SomeEnum
ENUM_VALUE_DEPRECATED: SomeEnum
ENUM_VALUE_CUSTOM_OPT: SomeEnum
FILE_OPT_FIELD_NUMBER: _ClassVar[int]
file_opt: _descriptor.FieldDescriptor
MESSAGE_OPT_FIELD_NUMBER: _ClassVar[int]
message_opt: _descriptor.FieldDescriptor
COMPLEX_MESSAGE_OPT_FIELD_NUMBER: _ClassVar[int]
complex_message_opt: _descriptor.FieldDescriptor
FIELD_OPT_FIELD_NUMBER: _ClassVar[int]
field_opt: _descriptor.FieldDescriptor
SOURCE_RETENTION_OPT_FIELD_NUMBER: _ClassVar[int]
source_retention_opt: _descriptor.FieldDescriptor
ENUM_OPT_FIELD_NUMBER: _ClassVar[int]
enum_opt: _descriptor.FieldDescriptor
ENUM_VALUE_OPT_FIELD_NUMBER: _ClassVar[int]
enum_value_opt: _descriptor.FieldDescriptor


class MyOptions(_message.Message):
  __slots__ = ("info", "message_only")
  INFO_FIELD_NUMBER: _ClassVar[int]
  MESSAGE_ONLY_FIELD_NUMBER: _ClassVar[int]
  info: str
  message_only: bool

  def __init__(
    self,
    info: _Optional[str] = ...,
    message_only: _Optional[bool] = ...
  ) -> None: ...


class ScalarTypesMessage(_message.Message):
  __slots__ = ("bool_field", "bytes_field", "int32_field", "int64_field", "uint32_field", "uint64_field", "sint32_field", "sint64_field",
               "fixed32_field", "fixed64_field", "sfixed32_field", "sfixed64_field", "float_field", "double_field", "string_field")
  BOOL_FIELD_FIELD_NUMBER: _ClassVar[int]
  BYTES_FIELD_FIELD_NUMBER: _ClassVar[int]
  INT32_FIELD_FIELD_NUMBER: _ClassVar[int]
  INT64_FIELD_FIELD_NUMBER: _ClassVar[int]
  UINT32_FIELD_FIELD_NUMBER: _ClassVar[int]
  UINT64_FIELD_FIELD_NUMBER: _ClassVar[int]
  SINT32_FIELD_FIELD_NUMBER: _ClassVar[int]
  SINT64_FIELD_FIELD_NUMBER: _ClassVar[int]
  FIXED32_FIELD_FIELD_NUMBER: _ClassVar[int]
  FIXED64_FIELD_FIELD_NUMBER: _ClassVar[int]
  SFIXED32_FIELD_FIELD_NUMBER: _ClassVar[int]
  SFIXED64_FIELD_FIELD_NUMBER: _ClassVar[int]
  FLOAT_FIELD_FIELD_NUMBER: _ClassVar[int]
  DOUBLE_FIELD_FIELD_NUMBER: _ClassVar[int]
  STRING_FIELD_FIELD_NUMBER: _ClassVar[int]
  bool_field: bool
  bytes_field: bytes
  int32_field: int
  int64_field: int
  uint32_field: int
  uint64_field: int
  sint32_field: int
  sint64_field: int
  fixed32_field: int
  fixed64_field: int
  sfixed32_field: int
  sfixed64_field: int
  float_field: float
  double_field: float
  string_field: str

  def __init__(
    self,
    bool_field: _Optional[bool] = ...,
    bytes_field: _Optional[bytes] = ...,
    int32_field: _Optional[int] = ...,
    int64_field: _Optional[int] = ...,
    uint32_field: _Optional[int] = ...,
    uint64_field: _Optional[int] = ...,
    sint32_field: _Optional[int] = ...,
    sint64_field: _Optional[int] = ...,
    fixed32_field: _Optional[int] = ...,
    fixed64_field: _Optional[int] = ...,
    sfixed32_field: _Optional[int] = ...,
    sfixed64_field: _Optional[int] = ...,
    float_field: _Optional[float] = ...,
    double_field: _Optional[float] = ...,
    string_field: _Optional[str] = ...
  ) -> None: ...


class SomeMessage(_message.Message):
  __slots__ = ("scalar_types_message", "some_enum", "repeated_string_field", "unpacked_int32_field", "repeated_enum_field",
               "repeated_any_field", "string_int32_map", "int32_any_map", "bool_duration_map", "int32_shared_map",
               "int32_recursive_message_map", "int32_enum_map", "oneof_string", "oneof_int32", "oneof_message", "wkt_message",
               "old_string_data", "hidden_int32_metadata")

  class Nested_Enum(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    NESTED_ENUM_UNSPECIFIED: _ClassVar[SomeMessage.Nested_Enum]
    NESTED_ENUM_VALUE_1: _ClassVar[SomeMessage.Nested_Enum]

  NESTED_ENUM_UNSPECIFIED: SomeMessage.Nested_Enum
  NESTED_ENUM_VALUE_1: SomeMessage.Nested_Enum

  class Nested_Message(_message.Message):
    __slots__ = ("nested_string_field",)
    NESTED_STRING_FIELD_FIELD_NUMBER: _ClassVar[int]
    nested_string_field: str

    def __init__(
      self,
      nested_string_field: _Optional[str] = ...
    ) -> None: ...

  class StringInt32MapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: str
    value: int

    def __init__(
      self,
      key: _Optional[str] = ...,
      value: _Optional[int] = ...
    ) -> None: ...

  class Int32AnyMapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: int
    value: _any_pb2.Any

    def __init__(
      self,
      key: _Optional[int] = ...,
      value: _Optional[_Union[_any_pb2.Any, _Mapping]] = ...
    ) -> None: ...

  class BoolDurationMapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: bool
    value: _duration_pb2.Duration

    def __init__(
      self,
      key: _Optional[bool] = ...,
      value: _Optional[_Union[datetime.timedelta, _duration_pb2.Duration, _Mapping]] = ...
    ) -> None: ...

  class Int32SharedMapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: int
    value: _import_from_pb2.SharedMessage

    def __init__(
      self,
      key: _Optional[int] = ...,
      value: _Optional[_Union[_import_from_pb2.SharedMessage, _Mapping]] = ...
    ) -> None: ...

  class Int32RecursiveMessageMapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: int
    value: SomeMessage

    def __init__(
      self,
      key: _Optional[int] = ...,
      value: _Optional[_Union[SomeMessage, _Mapping]] = ...
    ) -> None: ...

  class Int32EnumMapEntry(_message.Message):
    __slots__ = ("key", "value")
    KEY_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    key: int
    value: SomeEnum

    def __init__(
      self,
      key: _Optional[int] = ...,
      value: _Optional[_Union[SomeEnum, str]] = ...
    ) -> None: ...

  SCALAR_TYPES_MESSAGE_FIELD_NUMBER: _ClassVar[int]
  SOME_ENUM_FIELD_NUMBER: _ClassVar[int]
  REPEATED_STRING_FIELD_FIELD_NUMBER: _ClassVar[int]
  UNPACKED_INT32_FIELD_FIELD_NUMBER: _ClassVar[int]
  REPEATED_ENUM_FIELD_FIELD_NUMBER: _ClassVar[int]
  REPEATED_ANY_FIELD_FIELD_NUMBER: _ClassVar[int]
  STRING_INT32_MAP_FIELD_NUMBER: _ClassVar[int]
  INT32_ANY_MAP_FIELD_NUMBER: _ClassVar[int]
  BOOL_DURATION_MAP_FIELD_NUMBER: _ClassVar[int]
  INT32_SHARED_MAP_FIELD_NUMBER: _ClassVar[int]
  INT32_RECURSIVE_MESSAGE_MAP_FIELD_NUMBER: _ClassVar[int]
  INT32_ENUM_MAP_FIELD_NUMBER: _ClassVar[int]
  ONEOF_STRING_FIELD_NUMBER: _ClassVar[int]
  ONEOF_INT32_FIELD_NUMBER: _ClassVar[int]
  ONEOF_MESSAGE_FIELD_NUMBER: _ClassVar[int]
  WKT_MESSAGE_FIELD_NUMBER: _ClassVar[int]
  OLD_STRING_DATA_FIELD_NUMBER: _ClassVar[int]
  HIDDEN_INT32_METADATA_FIELD_NUMBER: _ClassVar[int]
  scalar_types_message: ScalarTypesMessage
  some_enum: SomeEnum
  repeated_string_field: _containers.RepeatedScalarFieldContainer[str]
  unpacked_int32_field: _containers.RepeatedScalarFieldContainer[int]
  repeated_enum_field: _containers.RepeatedScalarFieldContainer[SomeEnum]
  repeated_any_field: _containers.RepeatedCompositeFieldContainer[_any_pb2.Any]
  string_int32_map: _containers.ScalarMap[str, int]
  int32_any_map: _containers.MessageMap[int, _any_pb2.Any]
  bool_duration_map: _containers.MessageMap[bool, _duration_pb2.Duration]
  int32_shared_map: _containers.MessageMap[int, _import_from_pb2.SharedMessage]
  int32_recursive_message_map: _containers.MessageMap[int, SomeMessage]
  int32_enum_map: _containers.ScalarMap[int, SomeEnum]
  oneof_string: str
  oneof_int32: int
  oneof_message: ScalarTypesMessage
  wkt_message: WellKnownTypesMessage
  old_string_data: str
  hidden_int32_metadata: int

  def __init__(
    self,
    scalar_types_message: _Optional[_Union[ScalarTypesMessage, _Mapping]] = ...,
    some_enum: _Optional[_Union[SomeEnum, str]] = ...,
    repeated_string_field: _Optional[_Iterable[str]] = ...,
    unpacked_int32_field: _Optional[_Iterable[int]] = ...,
    repeated_enum_field: _Optional[_Iterable[_Union[SomeEnum, str]]] = ...,
    repeated_any_field: _Optional[_Iterable[_Union[_any_pb2.Any, _Mapping]]] = ...,
    string_int32_map: _Optional[_Mapping[str, int]] = ...,
    int32_any_map: _Optional[_Mapping[int, _any_pb2.Any]] = ...,
    bool_duration_map: _Optional[_Mapping[bool, _duration_pb2.Duration]] = ...,
    int32_shared_map: _Optional[_Mapping[int, _import_from_pb2.SharedMessage]] = ...,
    int32_recursive_message_map: _Optional[_Mapping[int, SomeMessage]] = ...,
    int32_enum_map: _Optional[_Mapping[int, SomeEnum]] = ...,
    oneof_string: _Optional[str] = ...,
    oneof_int32: _Optional[int] = ...,
    oneof_message: _Optional[_Union[ScalarTypesMessage, _Mapping]] = ...,
    wkt_message: _Optional[_Union[WellKnownTypesMessage, _Mapping]] = ...,
    old_string_data: _Optional[str] = ...,
    hidden_int32_metadata: _Optional[int] = ...
  ) -> None: ...


class WellKnownTypesMessage(_message.Message):
  __slots__ = ("double_value_field", "float_value_field", "int64_value_field", "uint64_value_field", "int32_value_field",
               "uint32_value_field", "bool_value_field", "string_value_field", "bytes_value_field", "timestamp_field", "duration_field",
               "any_field", "struct_field", "value_field", "list_value_field", "empty_field", "field_mask_field", "api_field", "type_field",
               "method_field", "enum_descriptor_field", "enum_value_descriptor_field", "field_descriptor_field", "option_field",
               "mixin_field", "source_context_field")
  DOUBLE_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  FLOAT_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  INT64_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  UINT64_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  INT32_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  UINT32_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  BOOL_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  STRING_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  BYTES_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  TIMESTAMP_FIELD_FIELD_NUMBER: _ClassVar[int]
  DURATION_FIELD_FIELD_NUMBER: _ClassVar[int]
  ANY_FIELD_FIELD_NUMBER: _ClassVar[int]
  STRUCT_FIELD_FIELD_NUMBER: _ClassVar[int]
  VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  LIST_VALUE_FIELD_FIELD_NUMBER: _ClassVar[int]
  EMPTY_FIELD_FIELD_NUMBER: _ClassVar[int]
  FIELD_MASK_FIELD_FIELD_NUMBER: _ClassVar[int]
  API_FIELD_FIELD_NUMBER: _ClassVar[int]
  TYPE_FIELD_FIELD_NUMBER: _ClassVar[int]
  METHOD_FIELD_FIELD_NUMBER: _ClassVar[int]
  ENUM_DESCRIPTOR_FIELD_FIELD_NUMBER: _ClassVar[int]
  ENUM_VALUE_DESCRIPTOR_FIELD_FIELD_NUMBER: _ClassVar[int]
  FIELD_DESCRIPTOR_FIELD_FIELD_NUMBER: _ClassVar[int]
  OPTION_FIELD_FIELD_NUMBER: _ClassVar[int]
  MIXIN_FIELD_FIELD_NUMBER: _ClassVar[int]
  SOURCE_CONTEXT_FIELD_FIELD_NUMBER: _ClassVar[int]
  double_value_field: _wrappers_pb2.DoubleValue
  float_value_field: _wrappers_pb2.FloatValue
  int64_value_field: _wrappers_pb2.Int64Value
  uint64_value_field: _wrappers_pb2.UInt64Value
  int32_value_field: _wrappers_pb2.Int32Value
  uint32_value_field: _wrappers_pb2.UInt32Value
  bool_value_field: _wrappers_pb2.BoolValue
  string_value_field: _wrappers_pb2.StringValue
  bytes_value_field: _wrappers_pb2.BytesValue
  timestamp_field: _timestamp_pb2.Timestamp
  duration_field: _duration_pb2.Duration
  any_field: _any_pb2.Any
  struct_field: _struct_pb2.Struct
  value_field: _struct_pb2.Value
  list_value_field: _struct_pb2.ListValue
  empty_field: _empty_pb2.Empty
  field_mask_field: _field_mask_pb2.FieldMask
  api_field: _api_pb2.Api
  type_field: _type_pb2.Type
  method_field: _api_pb2.Method
  enum_descriptor_field: _type_pb2.Enum
  enum_value_descriptor_field: _type_pb2.EnumValue
  field_descriptor_field: _type_pb2.Field
  option_field: _type_pb2.Option
  mixin_field: _api_pb2.Mixin
  source_context_field: _source_context_pb2.SourceContext

  def __init__(
    self,
    double_value_field: _Optional[_Union[_wrappers_pb2.DoubleValue, _Mapping]] = ...,
    float_value_field: _Optional[_Union[_wrappers_pb2.FloatValue, _Mapping]] = ...,
    int64_value_field: _Optional[_Union[_wrappers_pb2.Int64Value, _Mapping]] = ...,
    uint64_value_field: _Optional[_Union[_wrappers_pb2.UInt64Value, _Mapping]] = ...,
    int32_value_field: _Optional[_Union[_wrappers_pb2.Int32Value, _Mapping]] = ...,
    uint32_value_field: _Optional[_Union[_wrappers_pb2.UInt32Value, _Mapping]] = ...,
    bool_value_field: _Optional[_Union[_wrappers_pb2.BoolValue, _Mapping]] = ...,
    string_value_field: _Optional[_Union[_wrappers_pb2.StringValue, _Mapping]] = ...,
    bytes_value_field: _Optional[_Union[_wrappers_pb2.BytesValue, _Mapping]] = ...,
    timestamp_field: _Optional[_Union[datetime.datetime, _timestamp_pb2.Timestamp, _Mapping]] = ...,
    duration_field: _Optional[_Union[datetime.timedelta, _duration_pb2.Duration, _Mapping]] = ...,
    any_field: _Optional[_Union[_any_pb2.Any, _Mapping]] = ...,
    struct_field: _Optional[_Union[_struct_pb2.Struct, _Mapping]] = ...,
    value_field: _Optional[_Union[_struct_pb2.Value, _Mapping]] = ...,
    list_value_field: _Optional[_Union[_struct_pb2.ListValue, _Mapping]] = ...,
    empty_field: _Optional[_Union[_empty_pb2.Empty, _Mapping]] = ...,
    field_mask_field: _Optional[_Union[_field_mask_pb2.FieldMask, _Mapping]] = ...,
    api_field: _Optional[_Union[_api_pb2.Api, _Mapping]] = ...,
    type_field: _Optional[_Union[_type_pb2.Type, _Mapping]] = ...,
    method_field: _Optional[_Union[_api_pb2.Method, _Mapping]] = ...,
    enum_descriptor_field: _Optional[_Union[_type_pb2.Enum, _Mapping]] = ...,
    enum_value_descriptor_field: _Optional[_Union[_type_pb2.EnumValue, _Mapping]] = ...,
    field_descriptor_field: _Optional[_Union[_type_pb2.Field, _Mapping]] = ...,
    option_field: _Optional[_Union[_type_pb2.Option, _Mapping]] = ...,
    mixin_field: _Optional[_Union[_api_pb2.Mixin, _Mapping]] = ...,
    source_context_field: _Optional[_Union[_source_context_pb2.SourceContext, _Mapping]] = ...
  ) -> None: ...
