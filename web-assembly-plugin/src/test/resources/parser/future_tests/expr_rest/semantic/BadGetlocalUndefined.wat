;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    get_local 0
    drop))
(;; STDERR ;;;
out/test/parse/expr/bad-getlocal-undefined.txt:5:15: error: local variable out of range (max 0)
    get_local 0
              ^
;;; STDERR ;;)
