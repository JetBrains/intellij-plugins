import {NgModule} from '@angular/core';

import {InjectedToBasic} from './injectedToBasic';
import {CommonModule} from "@angular/common";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
  declarations: [
    InjectedToBasic
  ],
  imports: [
    CommonModule,
    CdkTableModule
  ],
  providers: [],
  bootstrap: [InjectedToBasic]
})
export class AppModule {
}
