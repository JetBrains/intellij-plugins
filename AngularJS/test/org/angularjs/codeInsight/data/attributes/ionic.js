Item.decorators = [
  { type: Component, args: [{
    selector: 'ion-list-header,ion-item,[ion-item],ion-item-divider',
    template: '<ng-content select="[item-left],ion-checkbox:not([item-right])"></ng-content>' +
              '<div class="item-inner">' +
              '<div class="input-wrapper">' +
              '<ng-content select="ion-label"></ng-content>' +
              '<ion-label *ngIf="_viewLabel">' +
              '<ng-content></ng-content>' +
              '</ion-label>' +
              '<ng-content select="ion-select,ion-input,ion-textarea,ion-datetime,ion-range,[item-content]"></ng-content>' +
              '</div>' +
              '<ng-content select="[item-right],ion-radio,ion-toggle"></ng-content>' +
              '<ion-reorder *ngIf="_hasReorder"></ion-reorder>' +
              '</div>' +
              '<div class="button-effect"></div>',
    host: {
      'class': 'item'
    },
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
  },] },
];
