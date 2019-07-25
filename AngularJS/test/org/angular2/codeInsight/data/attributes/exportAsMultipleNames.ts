// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from '@angular/core';

@Component({
    selector: 'hero-message',
    template: `<div #r="ref" #f="foo" #g="bar">
      {{ r. }}
      {{ f. }}
      {{ g. }}
    </div>`,
})
export class HeroAsyncMessageComponent {
    doIt(dir: RefDirective) {

    }
}

@Directive({
    selector: 'div',
    exportAs: 'ref,foo'
})
export class RefDirective {
    public foo: string;
}
