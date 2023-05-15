;;; TOOL: wat2wasm
;;; ERROR: 1

(module
  (memory 1)
  (data $data "a")
  (func
    i32.const 0 i32.const 0 i32.const 0 memory.init 0 memory.init $data
    data.drop 0
    data.drop $data
    i32.const 0 i32.const 0 i32.const 0 memory.copy
    i32.const 0 i32.const 0 i32.const 0 memory.fill
  )

  (table $t1 1 anyfunc)
  (elem $elem func 0)
  (elem funcref (ref.null func))
  (func
    i32.const 0 i32.const 0 i32.const 0 table.init 0
    i32.const 0 i32.const 0 i32.const 0 table.init $t1 0
    elem.drop 0
    elem.drop $elem
    i32.const 0 i32.const 0 i32.const 0 table.copy $t1 $t2
  )
  (table $t2 1 anyfunc)
)
(;; STDERR ;;;
out/test/parse/expr/bulk-memory-disabled.txt:6:4: error: passive data segments are not allowed
  (data $data "a")
   ^^^^
out/test/parse/expr/bulk-memory-disabled.txt:8:41: error: opcode not allowed: memory.init
    i32.const 0 i32.const 0 i32.const 0 memory.init 0
                                        ^^^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:9:5: error: opcode not allowed: data.drop
    data.drop 0
    ^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:10:41: error: opcode not allowed: memory.copy
    i32.const 0 i32.const 0 i32.const 0 memory.copy
                                        ^^^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:11:41: error: opcode not allowed: memory.fill
    i32.const 0 i32.const 0 i32.const 0 memory.fill
                                        ^^^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:15:23: error: unexpected token 0, expected ).
  (elem $elem funcref 0)
                      ^
out/test/parse/expr/bulk-memory-disabled.txt:16:17: error: ref.null not allowed
  (elem funcref (ref.null func))
                ^
out/test/parse/expr/bulk-memory-disabled.txt:18:41: error: opcode not allowed: table.init
    i32.const 0 i32.const 0 i32.const 0 table.init 0
                                        ^^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:19:5: error: opcode not allowed: elem.drop
    elem.drop 0
    ^^^^^^^^^
out/test/parse/expr/bulk-memory-disabled.txt:20:41: error: opcode not allowed: table.copy
    i32.const 0 i32.const 0 i32.const 0 table.copy
                                        ^^^^^^^^^^
;;; STDERR ;;)