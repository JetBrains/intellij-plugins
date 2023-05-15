;;; TOOL: wat2wasm
;;; ARGS: --enable-reference-types

(module
  (table $foo 1 externref)
  (table $bar 1 externref)
  (table $baz 1 funcref)
  (elem declare func 0)

  (func (result externref)
    i32.const 0
    table.get $foo
  )
  (func (result externref)
    i32.const 0
    table.get $bar
  )

  (func (param externref)
    i32.const 0
    get_local 0
    table.set $foo
  )
  (func (param externref)
    i32.const 0
    get_local 0
    table.set $bar
  )

  (func (result i32)
    ref.null extern
    i32.const 0
    table.grow $foo
  )
  (func (result i32)
    ref.null extern
    i32.const 0
    table.grow $bar
  )

  (func (param externref) (result i32)
    local.get 0
    ref.is_null
  )

  (func (result funcref)
    ref.func 0
  )

  (func (result i32)
    table.size $foo
  )
  (func (result i32)
    table.size $bar
  )
  (func (result i32)
    table.size $baz
  )
)