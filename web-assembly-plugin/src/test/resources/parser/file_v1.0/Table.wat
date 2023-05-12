(module
  (func $thirteen (result i32) (i32.const 13))
  (func $fourtytwo (result i32) (i32.const 42))
  (table (export "tbl") anyfunc (elem $thirteen $fourtytwo))
)