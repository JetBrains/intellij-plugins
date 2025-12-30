import {NgModule} from '@angular/core';

import {DestinationComponent} from './destination.component';
import {CdkTableModule} from "@angular/cdk/table";
import {CommonModule} from "@angular/common";

@NgModule({
    declarations: [
        DestinationComponent
    ],
    imports: [
        CdkTableModule,
        CommonModule
    ],
    bootstrap: [DestinationComponent]
})
export class DestModule {
}
