;;; TOOL: wat2wasm
;;; ARGS: --enable-bulk-memory
(module
  (memory 0)

  (func
    data.drop 0)

  (data "hi"))