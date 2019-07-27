// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Directive} from "@angular/core"

@Component({
    selector: 'my-customer,[my-customer-attr]',
    templateUrl: './custom.html',
    properties: {
        'id': 'dependency'
    }
})
class Dependency {
    id: string;
}

@Directive({
    selector: '[foo-directive]'
})
class MyDirective {

}
