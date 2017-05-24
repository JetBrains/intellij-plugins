/**
 * Material design button.
 */
class MdButton extends _MdButtonMixinBase {
  /**
   * @param {?} _elementRef
   * @param {?} _renderer
   * @param {?} _platform
   * @param {?} _focusOriginMonitor
   */
  constructor(_elementRef, _renderer, _platform, _focusOriginMonitor) {
    super();
    this._elementRef = _elementRef;
    this._renderer = _renderer;
    this._platform = _platform;
    this._focusOriginMonitor = _focusOriginMonitor;
    /**
     * Whether the button is round.
     */
    this._isRoundButton = this._hasAttributeWithPrefix('fab', 'mini-fab');
    /**
     * Whether the button is icon button.
     */
    this._isIconButton = this._hasAttributeWithPrefix('icon-button');
    /**
     * Whether the ripple effect on click should be disabled.
     */
    this._disableRipple = false;
    this._focusOriginMonitor.monitor(this._elementRef.nativeElement, this._renderer, true);
  }
  /**
   * Whether the ripple effect for this button is disabled.
   * @return {?}
   */
  get disableRipple() { return this._disableRipple; }
  /**
   * @param {?} v
   * @return {?}
   */
  set disableRipple(v) { this._disableRipple = coerceBooleanProperty(v); }
  /**
   * @return {?}
   */
  ngOnDestroy() {
    this._focusOriginMonitor.stopMonitoring(this._elementRef.nativeElement);
  }
  /**
   * The color of the button. Can be `primary`, `accent`, or `warn`.
   * @return {?}
   */
  get color() { return this._color; }
  /**
   * @param {?} value
   * @return {?}
   */
  set color(value) { this._updateColor(value); }
  /**
   * @param {?} newColor
   * @return {?}
   */
  _updateColor(newColor) {
    this._setElementColor(this._color, false);
    this._setElementColor(newColor, true);
    this._color = newColor;
  }
  /**
   * @param {?} color
   * @param {?} isAdd
   * @return {?}
   */
  _setElementColor(color, isAdd) {
    if (color != null && color != '') {
      if (isAdd) {
        this._renderer.addClass(this._getHostElement(), `mat-${color}`);
      }
      else {
        this._renderer.removeClass(this._getHostElement(), `mat-${color}`);
      }
    }
  }
  /**
   * Focuses the button.
   * @return {?}
   */
  focus() {
    this._getHostElement().focus();
  }
  /**
   * @return {?}
   */
  _getHostElement() {
    return this._elementRef.nativeElement;
  }
  /**
   * @return {?}
   */
  _isRippleDisabled() {
    return this.disableRipple || this.disabled;
  }
  /**
   * Gets whether the button has one of the given attributes
   * with either an 'md-' or 'mat-' prefix.
   * @param {...?} unprefixedAttributeNames
   * @return {?}
   */
  _hasAttributeWithPrefix(...unprefixedAttributeNames) {
    // If not on the browser, say that there are none of the attributes present.
    // Since these only affect how the ripple displays (and ripples only happen on the client),
    // detecting these attributes isn't necessary when not on the browser.
    if (!this._platform.isBrowser) {
      return false;
    }
    return unprefixedAttributeNames.some(suffix => {
      const /** @type {?} */ el = this._getHostElement();
      return el.hasAttribute('md-' + suffix) || el.hasAttribute('mat-' + suffix);
    });
  }
}
MdButton.decorators = [
  { type: Component, args: [{selector: 'button[md-button], button[md-raised-button], button[md-icon-button],' +
                                       'button[md-fab], button[md-mini-fab],' +
                                       'button[mat-button], button[mat-raised-button], button[mat-icon-button],' +
                                       'button[mat-fab], button[mat-mini-fab]',
    host: {
      '[disabled]': 'disabled || null',
    },
    template: "<span class=\"mat-button-wrapper\"><ng-content></ng-content></span> <div md-ripple *ngIf=\"!_isRippleDisabled()\" class=\"mat-button-ripple\" [class.mat-button-ripple-round]=\"_isRoundButton || _isIconButton\" [mdRippleCentered]=\"_isIconButton\" [mdRippleTrigger]=\"_getHostElement()\"></div> <!-- the touchstart handler prevents the overlay from capturing the initial tap on touch devices --> <div class=\"mat-button-focus-overlay\" (touchstart)=\"$event.preventDefault()\"></div> ",
    styles: [".mat-button,.mat-fab,.mat-icon-button,.mat-mini-fab,.mat-raised-button{box-sizing:border-box;position:relative;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;cursor:pointer;outline:0;border:none;display:inline-block;white-space:nowrap;text-decoration:none;vertical-align:baseline;font-size:14px;font-family:Roboto,\"Helvetica Neue\",sans-serif;font-weight:500;text-align:center;margin:0;min-width:88px;line-height:36px;padding:0 16px;border-radius:2px}[disabled].mat-button,[disabled].mat-fab,[disabled].mat-icon-button,[disabled].mat-mini-fab,[disabled].mat-raised-button{cursor:default}.cdk-keyboard-focused.mat-button .mat-button-focus-overlay,.cdk-keyboard-focused.mat-fab .mat-button-focus-overlay,.cdk-keyboard-focused.mat-icon-button .mat-button-focus-overlay,.cdk-keyboard-focused.mat-mini-fab .mat-button-focus-overlay,.cdk-keyboard-focused.mat-raised-button .mat-button-focus-overlay{opacity:1}.mat-button::-moz-focus-inner,.mat-fab::-moz-focus-inner,.mat-icon-button::-moz-focus-inner,.mat-mini-fab::-moz-focus-inner,.mat-raised-button::-moz-focus-inner{border:0}.mat-fab,.mat-mini-fab,.mat-raised-button{box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12);transform:translate3d(0,0,0);transition:background .4s cubic-bezier(.25,.8,.25,1),box-shadow 280ms cubic-bezier(.4,0,.2,1)}.mat-fab:not([disabled]):active,.mat-mini-fab:not([disabled]):active,.mat-raised-button:not([disabled]):active{box-shadow:0 5px 5px -3px rgba(0,0,0,.2),0 8px 10px 1px rgba(0,0,0,.14),0 3px 14px 2px rgba(0,0,0,.12)}[disabled].mat-fab,[disabled].mat-mini-fab,[disabled].mat-raised-button{box-shadow:none}.mat-button .mat-button-focus-overlay,.mat-icon-button .mat-button-focus-overlay{transition:none;opacity:0}.mat-button:hover .mat-button-focus-overlay{opacity:1}.mat-fab{box-shadow:0 3px 5px -1px rgba(0,0,0,.2),0 6px 10px 0 rgba(0,0,0,.14),0 1px 18px 0 rgba(0,0,0,.12);min-width:0;border-radius:50%;width:56px;height:56px;padding:0;flex-shrink:0}.mat-fab:not([disabled]):active{box-shadow:0 7px 8px -4px rgba(0,0,0,.2),0 12px 17px 2px rgba(0,0,0,.14),0 5px 22px 4px rgba(0,0,0,.12)}.mat-fab .mat-icon,.mat-fab i{padding:16px 0;line-height:24px}.mat-mini-fab{box-shadow:0 3px 5px -1px rgba(0,0,0,.2),0 6px 10px 0 rgba(0,0,0,.14),0 1px 18px 0 rgba(0,0,0,.12);min-width:0;border-radius:50%;width:40px;height:40px;padding:0;flex-shrink:0}.mat-mini-fab:not([disabled]):active{box-shadow:0 7px 8px -4px rgba(0,0,0,.2),0 12px 17px 2px rgba(0,0,0,.14),0 5px 22px 4px rgba(0,0,0,.12)}.mat-mini-fab .mat-icon,.mat-mini-fab i{padding:8px 0;line-height:24px}.mat-icon-button{padding:0;min-width:0;width:40px;height:40px;flex-shrink:0;line-height:40px;border-radius:50%}.mat-icon-button .mat-icon,.mat-icon-button i{line-height:24px}.mat-button,.mat-icon-button,.mat-raised-button{color:currentColor}.mat-button .mat-button-wrapper>*,.mat-icon-button .mat-button-wrapper>*,.mat-raised-button .mat-button-wrapper>*{vertical-align:middle}.mat-button-focus-overlay,.mat-button-ripple{position:absolute;top:0;left:0;bottom:0;right:0}.mat-button-focus-overlay{background-color:rgba(0,0,0,.12);border-radius:inherit;pointer-events:none;opacity:0;transition:opacity .2s cubic-bezier(.35,0,.25,1),background-color .2s cubic-bezier(.35,0,.25,1)}@media screen and (-ms-high-contrast:active){.mat-button-focus-overlay{background-color:rgba(255,255,255,.5)}}.mat-button-ripple-round{border-radius:50%;z-index:1}@media screen and (-ms-high-contrast:active){.mat-button,.mat-fab,.mat-icon-button,.mat-mini-fab,.mat-raised-button{outline:solid 1px}} /*# sourceMappingURL=button.css.map */ "],
    inputs: ['disabled'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
  },] },
];
/**
 * @nocollapse
 */
MdButton.ctorParameters = () => [
  { type: ElementRef, },
  { type: Renderer2, },
  { type: Platform, },
  { type: FocusOriginMonitor, },
];
MdButton.propDecorators = {
  'disableRipple': [{ type: Input },],
  'color': [{ type: Input },],
};
/**
 * Raised Material design button.
 */
class MdAnchor extends MdButton {
  /**
   * @param {?} elementRef
   * @param {?} renderer
   * @param {?} platform
   * @param {?} focusOriginMonitor
   */
  constructor(elementRef, renderer, platform, focusOriginMonitor) {
    super(elementRef, renderer, platform, focusOriginMonitor);
  }
  /**
   * \@docs-private
   * @return {?}
   */
  get tabIndex() {
    return this.disabled ? -1 : 0;
  }
  /**
   * @return {?}
   */
  get _isAriaDisabled() {
    return this.disabled ? 'true' : 'false';
  }
  /**
   * @param {?} event
   * @return {?}
   */
  _haltDisabledEvents(event) {
    // A disabled button shouldn't apply any actions
    if (this.disabled) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
  }
}
MdAnchor.decorators = [
  { type: Component, args: [{selector: `a[md-button], a[md-raised-button], a[md-icon-button], a[md-fab], a[md-mini-fab],
             a[mat-button], a[mat-raised-button], a[mat-icon-button], a[mat-fab], a[mat-mini-fab]`,
    host: {
      '[attr.disabled]': 'disabled || null',
      '[attr.aria-disabled]': '_isAriaDisabled',
      '(click)': '_haltDisabledEvents($event)',
    },
    inputs: ['disabled'],
    template: "<span class=\"mat-button-wrapper\"><ng-content></ng-content></span> <div md-ripple *ngIf=\"!_isRippleDisabled()\" class=\"mat-button-ripple\" [class.mat-button-ripple-round]=\"_isRoundButton || _isIconButton\" [mdRippleCentered]=\"_isIconButton\" [mdRippleTrigger]=\"_getHostElement()\"></div> <!-- the touchstart handler prevents the overlay from capturing the initial tap on touch devices --> <div class=\"mat-button-focus-overlay\" (touchstart)=\"$event.preventDefault()\"></div> ",
    styles: [".mat-button,.mat-fab,.mat-icon-button,.mat-mini-fab,.mat-raised-button{box-sizing:border-box;position:relative;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;cursor:pointer;outline:0;border:none;display:inline-block;white-space:nowrap;text-decoration:none;vertical-align:baseline;font-size:14px;font-family:Roboto,\"Helvetica Neue\",sans-serif;font-weight:500;text-align:center;margin:0;min-width:88px;line-height:36px;padding:0 16px;border-radius:2px}[disabled].mat-button,[disabled].mat-fab,[disabled].mat-icon-button,[disabled].mat-mini-fab,[disabled].mat-raised-button{cursor:default}.cdk-keyboard-focused.mat-button .mat-button-focus-overlay,.cdk-keyboard-focused.mat-fab .mat-button-focus-overlay,.cdk-keyboard-focused.mat-icon-button .mat-button-focus-overlay,.cdk-keyboard-focused.mat-mini-fab .mat-button-focus-overlay,.cdk-keyboard-focused.mat-raised-button .mat-button-focus-overlay{opacity:1}.mat-button::-moz-focus-inner,.mat-fab::-moz-focus-inner,.mat-icon-button::-moz-focus-inner,.mat-mini-fab::-moz-focus-inner,.mat-raised-button::-moz-focus-inner{border:0}.mat-fab,.mat-mini-fab,.mat-raised-button{box-shadow:0 3px 1px -2px rgba(0,0,0,.2),0 2px 2px 0 rgba(0,0,0,.14),0 1px 5px 0 rgba(0,0,0,.12);transform:translate3d(0,0,0);transition:background .4s cubic-bezier(.25,.8,.25,1),box-shadow 280ms cubic-bezier(.4,0,.2,1)}.mat-fab:not([disabled]):active,.mat-mini-fab:not([disabled]):active,.mat-raised-button:not([disabled]):active{box-shadow:0 5px 5px -3px rgba(0,0,0,.2),0 8px 10px 1px rgba(0,0,0,.14),0 3px 14px 2px rgba(0,0,0,.12)}[disabled].mat-fab,[disabled].mat-mini-fab,[disabled].mat-raised-button{box-shadow:none}.mat-button .mat-button-focus-overlay,.mat-icon-button .mat-button-focus-overlay{transition:none;opacity:0}.mat-button:hover .mat-button-focus-overlay{opacity:1}.mat-fab{box-shadow:0 3px 5px -1px rgba(0,0,0,.2),0 6px 10px 0 rgba(0,0,0,.14),0 1px 18px 0 rgba(0,0,0,.12);min-width:0;border-radius:50%;width:56px;height:56px;padding:0;flex-shrink:0}.mat-fab:not([disabled]):active{box-shadow:0 7px 8px -4px rgba(0,0,0,.2),0 12px 17px 2px rgba(0,0,0,.14),0 5px 22px 4px rgba(0,0,0,.12)}.mat-fab .mat-icon,.mat-fab i{padding:16px 0;line-height:24px}.mat-mini-fab{box-shadow:0 3px 5px -1px rgba(0,0,0,.2),0 6px 10px 0 rgba(0,0,0,.14),0 1px 18px 0 rgba(0,0,0,.12);min-width:0;border-radius:50%;width:40px;height:40px;padding:0;flex-shrink:0}.mat-mini-fab:not([disabled]):active{box-shadow:0 7px 8px -4px rgba(0,0,0,.2),0 12px 17px 2px rgba(0,0,0,.14),0 5px 22px 4px rgba(0,0,0,.12)}.mat-mini-fab .mat-icon,.mat-mini-fab i{padding:8px 0;line-height:24px}.mat-icon-button{padding:0;min-width:0;width:40px;height:40px;flex-shrink:0;line-height:40px;border-radius:50%}.mat-icon-button .mat-icon,.mat-icon-button i{line-height:24px}.mat-button,.mat-icon-button,.mat-raised-button{color:currentColor}.mat-button .mat-button-wrapper>*,.mat-icon-button .mat-button-wrapper>*,.mat-raised-button .mat-button-wrapper>*{vertical-align:middle}.mat-button-focus-overlay,.mat-button-ripple{position:absolute;top:0;left:0;bottom:0;right:0}.mat-button-focus-overlay{background-color:rgba(0,0,0,.12);border-radius:inherit;pointer-events:none;opacity:0;transition:opacity .2s cubic-bezier(.35,0,.25,1),background-color .2s cubic-bezier(.35,0,.25,1)}@media screen and (-ms-high-contrast:active){.mat-button-focus-overlay{background-color:rgba(255,255,255,.5)}}.mat-button-ripple-round{border-radius:50%;z-index:1}@media screen and (-ms-high-contrast:active){.mat-button,.mat-fab,.mat-icon-button,.mat-mini-fab,.mat-raised-button{outline:solid 1px}} /*# sourceMappingURL=button.css.map */ "],
    encapsulation: ViewEncapsulation.None
  },] },
];
/**
 * @nocollapse
 */
MdAnchor.ctorParameters = () => [
  { type: ElementRef, },
  { type: Renderer2, },
  { type: Platform, },
  { type: FocusOriginMonitor, },
];
MdAnchor.propDecorators = {
  'tabIndex': [{ type: Input,},],
};