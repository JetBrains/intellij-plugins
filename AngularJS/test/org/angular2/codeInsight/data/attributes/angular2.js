@Directive({selector: '[ngFor][ngForOf]', properties: ['ngForOf'], lifecycle: [onCheck]})
export class NgFor {
  constructor(_template: TemplateRef) {}
  set ngForOf(value: any) {
  }
}