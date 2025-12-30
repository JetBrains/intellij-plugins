import type { E2EPage, ScreenshotFn } from "../../../utils/test/playwright/index";
/**
 * Warning: This function will fail when in RTL mode.
 * TODO(FW-3711): Remove the `directions` config when this issue preventing
 * tests from passing in RTL mode is resolved.
 */
export declare const testSlidingItem: (page: E2EPage, itemID: string, screenshotNameSuffix: string, screenshot: ScreenshotFn, openStart?: boolean) => Promise<void>;
