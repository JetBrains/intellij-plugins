import type { E2EPage, E2EPageOptions, ScreenshotFn } from "../../../utils/test/playwright/index";
export declare class PopoverFixture {
    readonly page: E2EPage;
    constructor(page: E2EPage);
    goto(url: string, config: E2EPageOptions): Promise<void>;
    open(selector: string): Promise<void>;
    screenshot(modifier: string, screenshot: ScreenshotFn): Promise<void>;
}
