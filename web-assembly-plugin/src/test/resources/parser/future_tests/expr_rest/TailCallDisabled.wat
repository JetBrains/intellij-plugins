;;; TOOL: wat2wasm
;;; ERROR: 1

(module
  (table 1 anyfunc)
  (func return_call 0)
  (func i32.const 0 return_call_indirect)
)
(;; STDERR ;;;
out/test/parse/expr/tail-call-disabled.txt:6:9: error: opcode not allowed: return_call
  (func return_call 0)
        ^^^^^^^^^^^
out/test/parse/expr/tail-call-disabled.txt:7:21: error: opcode not allowed: return_call_indirect
  (func i32.const 0 return_call_indirect)
                    ^^^^^^^^^^^^^^^^^^^^
;;; STDERR ;;)
