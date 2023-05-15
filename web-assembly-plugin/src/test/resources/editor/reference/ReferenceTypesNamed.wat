;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types

(module
  (table $foo 1 externref)
  (table 1 externref)

  (func (result externref)
    i32.const 0
    table.get $foo
  )
  (func (param externref)
    i32.const 0
    get_local 0
    table.set $foo
  )
  (func (result i32)
    ref.null extern
    i32.const 0
    table.grow 1
  )
)