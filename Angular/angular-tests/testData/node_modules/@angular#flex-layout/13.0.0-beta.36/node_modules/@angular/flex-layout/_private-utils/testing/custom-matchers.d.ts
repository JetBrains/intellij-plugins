/// <reference types="jasmine" />
export declare const expect: (actual: any) => NgMatchers;
/**
 * Jasmine matchers that check Angular specific conditions.
 */
export interface NgMatchers extends jasmine.Matchers<any> {
    /**
     * Expect the element to have exactly the given text.
     *
     * ## Example
     *
     * {@example testing/ts/matchers.ts region='toHaveText'}
     */
    toHaveText(expected: string): boolean;
    /**
     * Compare key:value pairs as matching EXACTLY
     */
    toHaveMap(expected: {
        [k: string]: string;
    }): boolean;
    /**
     * Expect the element to have the given CSS class.
     *
     * ## Example
     *
     * {@example testing/ts/matchers.ts region='toHaveCssClass'}
     */
    toHaveCssClass(expected: string): boolean;
    /**
     * Expect the element to have the given pairs of attribute name and attribute value
     */
    toHaveAttributes(expected: {
        [k: string]: string;
    }): boolean;
    /**
     * Expect the element to have the given CSS styles injected INLINE
     *
     * ## Example
     *
     * {@example testing/ts/matchers.ts region='toHaveStyle'}
     */
    toHaveStyle(expected: {
        [k: string]: string;
    } | string): boolean;
    /**
     * Expect the element to have the given CSS inline OR computed styles.
     *
     * ## Example
     *
     * {@example testing/ts/matchers.ts region='toHaveStyle'}
     */
    toHaveStyle(expected: {
        [k: string]: string;
    } | string): boolean;
    /**
     * Invert the matchers.
     */
    not: NgMatchers;
}
/**
 * NOTE: These custom JASMINE Matchers are used only
 *       in the Karma/Jasmine testing for the Layout Directives
 *       in `src/lib/flex/api`
 */
export declare const customMatchers: jasmine.CustomMatcherFactories;
