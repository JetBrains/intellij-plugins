// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ElementRef, ViewChild} from '@angular/core';

@Component({
    selector: 'app-test',
    templateUrl: "./viewChildrenReferenceHTML.html"
})
export class TestComponent {

    @ViewChild('ar<caret>ea') area: ElementRef;

}
