;;; TOOL: wat2wasm
;;; ERROR: 1
(module binary
  "\00ASM"
  "\0b\00\00\00")
(;; STDERR ;;;
out/test/parse/module/bad-binary-module-magic.txt:3:2: error: error in binary module: @0x00000004: bad magic value
(module binary
 ^^^^^^
;;; STDERR ;;)
