import {NgModule} from '@angular/core';

import {DestinationComponent} from './destination.component';
import {CommonModule} from "@angular/common";

@NgModule({
    declarations: [
        DestinationComponent
    ],
    imports: [
        CommonModule
    ],
    bootstrap: [DestinationComponent]
})
export class DestModule {
}
