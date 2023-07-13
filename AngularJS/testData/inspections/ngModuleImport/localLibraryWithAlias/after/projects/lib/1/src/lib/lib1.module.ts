import {NgModule} from '@angular/core';
import {Lib1Component} from './lib1.component';

@NgModule({
  declarations: [Lib1Component],
  exports: [Lib1Component]
})
export class Lib1Module {}
