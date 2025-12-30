@Directive({selector: '[ngFor][ngForOf]', properties: ['ngForOf'], lifecycle: [onCheck]})
export class NgFor {
  set ngForOf(value: any) {
  }
}