// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from 'angular2/core';

@Component({
    selector: 'hero-message',
    template: `<div #r="ref" #f="foo">
      {{ r.foo }}
      {{ r.<weak_warning descr="Unresolved variable bar">bar</weak_warning> }}
      {{ f.<weak_warning descr="Unresolved variable bar">bar</weak_warning> }}
      {{ doIt(r) }}
      {{ doIt(<weak_warning descr="Argument type string is not assignable to parameter type RefDirective">"test"</weak_warning>) }}
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
