;;; TOOL: wat2wasm
;;; ERROR: 1
(module (func i32.const 4294967296))
(;; STDERR ;;;
out/test/parse/expr/bad-const-i32-overflow.txt:3:25: error: invalid literal "4294967296"
(module (func i32.const 4294967296))
                        ^^^^^^^^^^
;;; STDERR ;;)
