;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types

(module
  (table $foo 1 anyfunc)
  (table $bar 1 anyfunc)

  (func (result i32) i32.const 0)
  (func (result i32) i32.const 1)

  (func (result i32)
    i32.const 0
    call_indirect $foo (type 0))

  (func (result i32)
    i32.const 0
    call_indirect $bar (type 0))

  (type (func (param i32)))
)