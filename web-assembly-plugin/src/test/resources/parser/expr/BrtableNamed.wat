;;; TOOL: wat2wasm
(module
  (func
    block $exit
      block $1
        block $0
          i32.const 0
          br_table $0 $1 $exit 
        end
        nop
      end
      nop
     end))