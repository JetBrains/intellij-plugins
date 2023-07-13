// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
