;;; TOOL: wat2wasm
;;; ARGS: --enable-bulk-memory

(module
  (memory 1)
  (data $data "a")
  (func
    i32.const 0 i32.const 0 i32.const 0 memory.init $data
    data.drop $data
  )

  (table 1 anyfunc)
  (elem $elem funcref (ref.func 0) (ref.null func))
  (elem $elem2 func 0)
  (func
    i32.const 0 i32.const 0 i32.const 0 table.init $elem
    elem.drop $elem
  )
)