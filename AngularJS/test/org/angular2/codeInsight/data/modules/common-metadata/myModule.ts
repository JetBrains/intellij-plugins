// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Directive, NgModule} from '@angular/core';
import {AsyncPipe, CommonModule, DatePipe, NgIf} from "./common";


@Directive({
    selector: "[attr]"
})
export class MyDirective {

}

@NgModule({
    imports: [
        {
            ngModule: CommonModule,
            providers: []
        }
    ],
    declarations: [
        MyDirective,
        AsyncPipe
    ],
    exports: [
        AsyncPipe,
        DatePipe,
        NgIf
    ]
})
export class CommonModuleMetadataTest {

}
