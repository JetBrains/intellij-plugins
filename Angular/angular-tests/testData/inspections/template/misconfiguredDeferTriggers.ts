// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
  selector: 'my-comp',
  template: `
    <!-- should emit when on immediate coexists with other mains -->
    @defer (on immediate; <warning descr="The immediate trigger makes additional triggers redundant">on timer(100ms)</warning> ; <warning descr="The immediate trigger makes additional triggers redundant">on viewport(ref)</warning> ) { <div></div> }
    
    <!-- should emit when on immediate coexists with prefetch -->
    @defer (on immediate;  <warning descr="Prefetch triggers have no effect because immediate executes earlier">prefetch on viewport</warning> ) { <div></div> }
    
    <!-- should emit when prefetch timer >= main timer -->
    @defer (on timer(1s); <warning descr="The prefetch timer (2000ms) is not scheduled before the main timer (1000ms), so it won't run prior to rendering">prefetch on timer(2000ms)</warning>) { <div></div> }
  
    <!-- should emit when prefetch identical to main viewport/interaction/hover -->
    @defer (on viewport(ref); <warning descr="Prefetch viewport matches the main trigger and provides no benefit">prefetch on viewport(ref)</warning>) { <div></div> }
    
    <!-- should not emit for valid prefetch earlier than main -->
    @defer (on timer(1s); prefetch on timer(500ms)) { <div></div> }
    
    <!-- should not emit when main is viewport(ref) and prefetch is viewport without reference -->
    @defer (on viewport(ref); prefetch on viewport) { <div></div> } @placeholder { <div></div> }
    
    <!-- should not emit when main is interaction(refA) and prefetch is interaction(refB) -->
    @defer (on interaction(refA); prefetch on interaction(refB)) { <div></div> } @placeholder { <div></div> }
    
    
  `,
})
export class MyComponent {

}