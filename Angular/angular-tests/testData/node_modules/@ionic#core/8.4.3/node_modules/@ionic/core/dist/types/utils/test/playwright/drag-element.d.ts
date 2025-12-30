/**
 * The drag gesture will not operate as expected when the element is dragged outside of the viewport because the Mouse class does not fire events outside of the viewport.
 *
 * For example, if the mouse is moved outside of the viewport, then the `mouseup` event will not fire.
 *
 * See https://playwright.dev/docs/api/class-mouse#mouse-move for more information.
 */
import type { ElementHandle, Locator } from '@playwright/test';
import type { E2EPage } from './';
export declare const dragElementBy: (el: Locator | ElementHandle<SVGElement | HTMLElement>, page: E2EPage, dragByX?: number, dragByY?: number, startXCoord?: number, startYCoord?: number, releaseDrag?: boolean) => Promise<void>;
/**
 * Drags an element by the given amount of pixels on the Y axis.
 * @param el The element to drag.
 * @param page The E2E Page object.
 * @param dragByY The amount of pixels to drag the element by.
 * @param startYCoord The Y coordinate to start the drag gesture at. Defaults to the center of the element.
 */
export declare const dragElementByYAxis: (el: Locator | ElementHandle<SVGElement | HTMLElement>, page: E2EPage, dragByY: number, startYCoord?: number) => Promise<void>;
