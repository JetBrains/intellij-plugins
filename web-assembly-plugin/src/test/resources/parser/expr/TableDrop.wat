;;; TOOL: wat2wasm
;;; ARGS: --enable-bulk-memory
(module
  (memory 0)

  (func
    elem.drop 0)

  (func)

  (table 0 funcref)
  (elem func 1))