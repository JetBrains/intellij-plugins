;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (import "bar" "baz" (func $foo (param i32)))
  (import "quux" "blorf" (func $foo (param f32))))
(;; STDERR ;;;
out/test/parse/module/bad-import-func-redefinition.txt:5:4: error: redefinition of function "$foo"
  (import "quux" "blorf" (func $foo (param f32))))
   ^^^^^^
;;; STDERR ;;)
