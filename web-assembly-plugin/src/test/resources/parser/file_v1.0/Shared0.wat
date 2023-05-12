(module
  (import "js" "memory" (memory 1))
  (import "js" "table" (table 1 anyfunc))
  (elem (i32.const 0) $shared0func)
  (func $shared0func (result i32)
   i32.const 0
   i32.load)
)