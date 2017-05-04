/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 */
export var MdIconButtonCssMatStyler = (function () {
    function MdIconButtonCssMatStyler() {
    }
    MdIconButtonCssMatStyler = __decorate([
        Directive({
            selector: 'button[md-raised-button], button[mat-raised-button], ' +
                      'a[md-raised-button], a[mat-raised-button]',
            host: {
                '[class.mat-icon-button]': 'true',
            }
        }),
        __metadata('design:paramtypes', [])
    ], MdIconButtonCssMatStyler);
    return MdIconButtonCssMatStyler;
}());
