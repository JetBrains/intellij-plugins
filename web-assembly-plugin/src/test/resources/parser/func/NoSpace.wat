;;; TOOL: wat2wasm
(module
  (func $foomore
    i32.const 0x0
    call $foo)
  (func $foo (param i32) nop)
)