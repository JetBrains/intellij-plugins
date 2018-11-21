// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ViewChild} from "@angular/core";

@Component({
    selector: 'test-component',
    templateUrl: './test.component.html'
})
export class TestComponent {
    @ViewChild("ourRe<caret>ference")
    view:any;

}