import {NgModule} from '@angular/core';

import {SourceComponent} from './source.component';
import {CommonModule} from "@angular/common";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
            declarations: [
              SourceComponent
            ],
            imports: [
              CommonModule,
              CdkTableModule
            ],
            providers: [],
            bootstrap: [SourceComponent]
          })
export class SourceModule {
}
