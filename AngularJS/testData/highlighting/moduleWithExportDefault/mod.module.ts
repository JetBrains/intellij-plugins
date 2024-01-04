import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {RouterModule} from "@angular/router";
import {CompAComponent} from './comp-a.component';


@NgModule({
  declarations: [
    CompAComponent,
  ],
  imports: [
    CommonModule,
    RouterModule.forChild([{path: '', loadComponent: () => import('./comp-b.component')}]),
  ],
  exports: [
    RouterModule,
    CompAComponent,
  ]
})
export default class ModModule {}
