import {Component, OnInit} from '@angular/core';
import {
  DataSourceDirective,
} from './data-source.directive';

@Component({
  selector: 'app-combobox',
  templateUrl: './combobox.component.html',
  hostDirectives: [
    {
      directive: DataSourceDirective,
      outputs: ['dataSourceChanged'],
    },
  ],
})
export class ComboboxComponent implements OnInit {

}
