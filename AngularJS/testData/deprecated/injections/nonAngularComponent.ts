// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "foo-lib"

@Component({
    template: `<foo></foo>`,
    host: {
        '(click)': 'event',
        '[click]': 'binding',
        'click': 'attribute',
    }
})
export class AppComponent {
    onClick() {}
}
