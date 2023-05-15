;;; TOOL: wat2wasm
(module binary
  "\00asm"            ;; magic
  "\01\00\00\00"      ;; version
  "\01\05"            ;; type section, 5 bytes
  "\01\60\00\01\7f"   ;; 1 type, function, no params, i32 result
  "\03\02"            ;; function section, 2 bytes
  "\01\00"            ;; 1 function, type 0
  "\07\08"            ;; export section, 8 bytes
  "\01\04main\00\00"  ;; 1 export, function 0, named "main"
  "\0a\08"            ;; code section, 8 bytes
  "\01\06"            ;; 1 function, 6 bytes
  "\00"               ;; 0 locals
  "\41"               ;; i32.const
  "\dc\7c"            ;; -420
  "\0f"               ;; return
  "\0b"               ;; end (of function)
)
