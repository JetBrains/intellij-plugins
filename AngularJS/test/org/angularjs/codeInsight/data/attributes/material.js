/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 */
export var MdIconButtonCssMatStyler = (function () {
    function MdIconButtonCssMatStyler() {
    }
    MdIconButtonCssMatStyler = __decorate([
        Directive({
            selector: 'button[md-icon-button], button[mat-icon-button], a[md-icon-button], a[mat-icon-button]',
            host: {
                '[class.mat-icon-button]': 'true',
            }
        }),
        __metadata('design:paramtypes', [])
    ], MdIconButtonCssMatStyler);
    return MdIconButtonCssMatStyler;
}());
