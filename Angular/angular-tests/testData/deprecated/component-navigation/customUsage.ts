// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core"

@Component({
    selector: 'usage',
    template: `<my-customer></my-customer>
    <div my-customer-attr=""></div>
    <div foo-directive></div>
    `
})
class Dependency {

}
