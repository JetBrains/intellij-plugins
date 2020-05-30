// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

class Base {

    bar(test: boolean): string {
        return ""
    }

    foo: string | boolean

}

@Component({
    selector: 'todo-cmp',
    template: `
    {{ <caret> }}
  `,
})
export class TodoCmp extends Base implements Foo, Foo2 {

    foo: string;

    bar(): string;
    bar(test: string): string;
    bar(test: boolean): string;
    bar(test?: string | boolean): string {
        return '';
    }

}

interface Foo {
    bar(): string;

    foo: string;
}

interface Foo2 {
    bar(test: string): string;

}

