;;; TOOL: wat2wasm
(module
  (func $foo
    i32.const 1
    i32.const 0
    call $bar
    drop)
  (func $bar (param i32 i32) (result i32)
    i32.const 0))