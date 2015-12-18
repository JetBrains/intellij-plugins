@Directive({selector: '[ng-for][ng-for-of]', properties: ['ngForOf'], lifecycle: [onCheck]})
export class NgFor {
  set ngForOf(value: any) {
  }
}