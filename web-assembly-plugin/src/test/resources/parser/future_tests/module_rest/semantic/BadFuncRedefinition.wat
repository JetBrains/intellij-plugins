;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func $n nop)
  (func $n nop))
(;; STDERR ;;;
out/test/parse/module/bad-func-redefinition.txt:5:4: error: redefinition of function "$n"
  (func $n nop))
   ^^^^
;;; STDERR ;;)
