;;; TOOL: wat2wasm
;;; ERROR: 1
(module
  (func
    f32.const 0
    call $baz)
  (import "foo" "bar" (func $baz (param f32))))
(;; STDERR ;;;
out/test/parse/expr/callimport-defined-later.txt:7:4: error: imports must occur before all non-import definitions
  (import "foo" "bar" (func $baz (param f32))))
   ^^^^^^
;;; STDERR ;;)