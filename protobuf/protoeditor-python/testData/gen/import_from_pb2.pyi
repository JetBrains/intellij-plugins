from typing import ClassVar as _ClassVar, Optional as _Optional

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message

DESCRIPTOR: _descriptor.FileDescriptor


class SharedMessage(_message.Message):
  __slots__ = ("shared_content",)
  SHARED_CONTENT_FIELD_NUMBER: _ClassVar[int]
  shared_content: str

  def __init__(self, shared_content: _Optional[str] = ...) -> None: ...
