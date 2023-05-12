(module
  (start $f2)
  (start $i64)

  (table $namedTable 2 funcref)
  (func $f1 (result i32)
    i32.const 42)
  (func $f2(result i32)
    i32.const 13)
  (elem (table $namedTable) (i32.const 0) $f1 $f2)
  (elem (table $namedImportedTable) (i32.const 0) 2 3)
  (type $return_i32 (func (result i32)))
  (func (export "callByIndex") (param $i i32) (result i32)
    local.get $i
    call_indirect (type $return_i32))
  (import "test" "i64" (func $i64 (param i64) (result i64)))
  (import "test" "namedImportedTable" (table $namedImportedTable 2 funcref))

  (memory (data))
  (import "test" "noname" (memory 2))
  (data (memory 0) i32.const 0)
  (data (memory 1) i32.const 0)
)