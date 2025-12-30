// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, NgModule} from "@angular/core";

@Component({
    selector: 'app-classic',
    template: `<p>classic works!</p>`,
})
export class ClassicComponent {
}

@NgModule({
    declarations: [
        ClassicComponent,
    ],
    exports: [
        ClassicComponent,
    ],
})
export class ClassicModule {
}