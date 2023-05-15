;;; TOOL: wat2wasm
(<fold text='...' expand='true'>type $v_v (func)</fold>)
(<fold text='...' expand='true'>type $v_ii (func (param i32 i32))</fold>)
(<fold text='...' expand='true'>type $ii_v (func (result i32 i32))</fold>)
(<fold text='...' expand='true'>type $ff_ff (func (param f32 f32) (result f32 f32))</fold>)

(<fold text='...' expand='true'>func
  i32.const 0
  if<fold text='...' expand='true'> (type $v_v)
  </fold>end</fold>)

(<fold text='...' expand='true'>func
  i32.const 0
  i32.const 1
  i32.const 2
  if<fold text='...' expand='true'> (type $v_ii)
    drop
    drop
  else
    drop
    drop
  </fold>end</fold>)

(<fold text='...' expand='true'>func
  i32.const 0
  if<fold text='...' expand='true'> (type $ii_v)
    i32.const 1
    i32.const 2
  else
    i32.const 3
    i32.const 4
  </fold>end
  drop
  drop</fold>)

(<fold text='...' expand='true'>func
  f32.const 1
  f32.const 2
  i32.const 0
  if<fold text='...' expand='true'> (type $ff_ff)
  </fold>end
  drop
  drop</fold>)