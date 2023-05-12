;;; TOOL: wat2wasm
(module
  (global i32 (i32.const 0))
  (global (mut f32) (f32.const 0))
  (export "global0" (global 0))

  (memory 1)
  (export "mem1" (memory 0))
  (export "mem2" (memory 0))

  (table 0 anyfunc)
  (export "my_table" (table 0))

  (func $n (result i32) (i32.const 0))
  (export "n" (func $n))

  (func (nop))
  (export "a" (func 1))
  (export "b" (func 1))
)