import { Component, Input } from '@angular/core';

@Component({
             selector: 'quick-docs-default',
             standalone: true,
             template: `quick-docs-default works`
           })
class QuickDocsDefault {
  /** This is a JSDoc test
   * @default true
   * @internal this is a test for @internal JSDoc tag
   * @deprecated this is a test for @deprecated JSDoc tag
   * */
  @Input() property = true;
}

@Component({
 standalone: true,
 imports: [
   QuickDocsDefault
 ],
 template: `
    <quick-docs-default
      [prop<caret>erty]='false'
    ></quick-docs-default>`
})
class QuickDocsExample {
}