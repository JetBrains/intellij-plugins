;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (global i32 (get_global 1)))
(;; STDERR ;;;
out/test/parse/module/bad-global-invalid-getglobal.txt:4:27: error: global variable out of range: 1 (max 1)
  (global i32 (get_global 1)))
                          ^
;;; STDERR ;;)
