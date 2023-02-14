import {NgModule} from '@angular/core';

import {Injected} from './injected';
import {CommonModule} from "@angular/common";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
  declarations: [
    Injected
  ],
  imports: [
    CommonModule,
    CdkTableModule
  ],
  providers: [],
  bootstrap: [Injected]
})
export class AppModule {
}
