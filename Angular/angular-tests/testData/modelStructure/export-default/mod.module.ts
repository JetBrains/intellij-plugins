import {NgModule} from '@angular/core';
import {RouterModule} from "@angular/router";
import {CompAComponent} from './comp-a.component';


@NgModule({
  declarations: [
    CompAComponent,
  ],
  imports: [
    RouterModule.forChild([{path: '', loadComponent: () => import('./comp-b.component')}]),
  ],
  exports: [
    CompAComponent,
  ]
})
export default class ModModule {}
