;;; TOOL: wat2wasm
;;; ARGS0: --enable-tail-call
(module
  (table 1 anyfunc)
  (func return_call 0)
  (func i32.const 0 return_call_indirect)
)
