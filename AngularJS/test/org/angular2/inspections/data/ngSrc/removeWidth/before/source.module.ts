import {NgModule} from '@angular/core';

import {MainComponent} from './main.component';
import {NgOptimizedImage} from "@angular/common";

@NgModule({
    declarations: [
        MainComponent
    ],
    imports: [
        NgOptimizedImage
    ]
})
export class MainModule {
}
