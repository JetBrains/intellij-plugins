;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func (export "f")))

;; Can't use assert_return when parsing a .wat file.
(assert_return (invoke "f"))
(;; STDERR ;;;
out/test/parse/module/bad-module-with-assert.txt:6:1: error: unexpected token (, expected EOF.
(assert_return (invoke "f"))
^
;;; STDERR ;;)
