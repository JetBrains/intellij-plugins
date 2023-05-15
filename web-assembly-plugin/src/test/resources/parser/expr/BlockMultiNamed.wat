;;; TOOL: wat2wasm
(type $v_v (func))
(type $v_ii (func (param i32 i32)))
(type $ii_v (func (result i32 i32)))
(type $ff_ff (func (param f32 f32) (result f32 f32)))

(func
  block (type $v_v)
  end)

(func
  i32.const 1
  i32.const 2
  block (type $v_ii)
    drop
    drop
  end)

(func
  block (type $ii_v)
    i32.const 1
    i32.const 2
  end
  drop
  drop)

(func
  f32.const 1
  f32.const 2
  block (type $ff_ff)
  end
  drop
  drop)