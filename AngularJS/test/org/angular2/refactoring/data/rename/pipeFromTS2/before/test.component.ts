// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ViewChild} from "@angular/core";
import {FooPipe} from "./foo.pipe";

@Component({
    selector: 'test-component',
    templateUrl: './test.component.html'
})
export class TestComponent {
}

let pipe: FooPipe;