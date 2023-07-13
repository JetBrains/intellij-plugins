import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-svg-highlighting',
  templateUrl: './svg-highlighting.component.svg'
})
export class SvgHighlightingComponent implements OnInit {

  @Input()
  height: number;

  items: {width: number}[];

  constructor() { }

  ngOnInit() {
  }

}
