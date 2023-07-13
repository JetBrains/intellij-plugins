// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
