# proto-file: lang/resolve/import_root.proto
# proto-message: ImportRoot
# proto-import: lang/resolve/import_explicit1.proto
# proto-import: lang/resolve/import_explicit2.proto
# proto-import: lang/resolve/import_any.proto

field: "foo"

import_message {
  field: "inner"
  any_message {
    [type.googleapis.com/foo.bar.any.AnyMessage] {
      any_field: "foobar"
    }
  }
  [foo.bar.local_ext]: "local_ext"
  [foo.bar.explicit_ext_1]: "explicit_ext_1"
  [foo.bar.explicit_ext_2]: "explicit_ext_2"
  [foo.bar.<error descr="Cannot resolve symbol 'bogus_ext'">bogus_ext</error>]: "bogus_ext"
  # The following is in a file imported by import_root.proto, but it's not a public import so the
  # symbol is not exported.
  [foo.bar.<error descr="Cannot resolve symbol 'not_exported_ext'">not_exported_ext</error>]: "not_exported_ext"
}
