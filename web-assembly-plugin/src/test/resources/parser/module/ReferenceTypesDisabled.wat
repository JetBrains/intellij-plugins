;;; TOOL: wat2wasm
;;; ERROR: 1

(module
  (table $t 1 externref)
  (func
    i32.const 0
    i32.const 0
    table.get $t
    table.set $t
  )
)
(;; STDERR ;;;
out/test/parse/module/reference-types-disabled.txt:5:15: error: value type not allowed: externref
  (table $t 1 externref)
              ^^^^^^^^^
out/test/parse/module/reference-types-disabled.txt:9:5: error: opcode not allowed: table.get
    table.get $t
    ^^^^^^^^^
out/test/parse/module/reference-types-disabled.txt:10:5: error: opcode not allowed: table.set
    table.set $t
    ^^^^^^^^^
out/test/parse/module/reference-types-disabled.txt:9:15: error: undefined table variable "$t"
    table.get $t
              ^^
out/test/parse/module/reference-types-disabled.txt:10:15: error: undefined table variable "$t"
    table.set $t
              ^^
;;; STDERR ;;)