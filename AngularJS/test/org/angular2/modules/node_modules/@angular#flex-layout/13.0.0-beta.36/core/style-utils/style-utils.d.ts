import { StylesheetMap } from '../stylesheet-map/stylesheet-map';
import { LayoutConfigOptions } from '../tokens/library-config';
import * as i0 from "@angular/core";
export declare class StyleUtils {
    private _serverStylesheet;
    private _serverModuleLoaded;
    private _platformId;
    private layoutConfig;
    constructor(_serverStylesheet: StylesheetMap, _serverModuleLoaded: boolean, _platformId: Object, layoutConfig: LayoutConfigOptions);
    /**
     * Applies styles given via string pair or object map to the directive element
     */
    applyStyleToElement(element: HTMLElement, style: StyleDefinition | string, value?: string | number | null): void;
    /**
     * Applies styles given via string pair or object map to the directive's element
     */
    applyStyleToElements(style: StyleDefinition, elements?: HTMLElement[]): void;
    /**
     * Determine the DOM element's Flexbox flow (flex-direction)
     *
     * Check inline style first then check computed (stylesheet) style
     */
    getFlowDirection(target: HTMLElement): [string, string];
    hasWrap(target: HTMLElement): boolean;
    /**
     * Find the DOM element's raw attribute value (if any)
     */
    lookupAttributeValue(element: HTMLElement, attribute: string): string;
    /**
     * Find the DOM element's inline style value (if any)
     */
    lookupInlineStyle(element: HTMLElement, styleName: string): string;
    /**
     * Determine the inline or inherited CSS style
     * NOTE: platform-server has no implementation for getComputedStyle
     */
    lookupStyle(element: HTMLElement, styleName: string, inlineOnly?: boolean): string;
    /**
     * Applies the styles to the element. The styles object map may contain an array of values
     * Each value will be added as element style
     * Keys are sorted to add prefixed styles (like -webkit-x) first, before the standard ones
     */
    private _applyMultiValueStyleToElement;
    private _setServerStyle;
    private _getServerStyle;
    private _readStyleAttribute;
    private _writeStyleAttribute;
    static ɵfac: i0.ɵɵFactoryDeclaration<StyleUtils, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<StyleUtils>;
}
/**
 * Definition of a css style. Either a property name (e.g. "flex-basis") or an object
 * map of property name and value (e.g. {display: 'none', flex-order: 5})
 */
export declare type StyleDefinition = {
    [property: string]: string | number | null;
};
