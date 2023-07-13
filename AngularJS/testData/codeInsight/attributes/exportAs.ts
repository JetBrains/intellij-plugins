// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from '@angular/core';

@Component({
    selector: 'hero-message',
    template: `<div #r="ref" #f="foo">
      {{ r.foo }}
      {{ r.<error descr="Unresolved variable bar">bar</error> }}
      {{ f.<weak_warning descr="Unresolved variable bar">bar</weak_warning> }}
      {{ doIt(r) }}
      {{ doIt(<error descr="Argument type  \"test\"  is not assignable to parameter type  RefDirective ">"test"</error>) }}
    </div>`,
})
export class HeroAsyncMessageComponent {
    doIt(dir: RefDirective) {

    }
}

@Directive({
    selector: 'div',
    exportAs: 'ref'
})
export class RefDirective {
    public foo: string;
}
