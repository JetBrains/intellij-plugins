import type { E2EPage, E2ELocator, EventSpy, E2EPageOptions, ScreenshotFn } from "../../../utils/test/playwright/index";
import type { SelectPopoverOption } from '../select-popover-interface';
export declare class SelectPopoverPage {
    private page;
    private multiple?;
    private options;
    popover: E2ELocator;
    selectPopover: E2ELocator;
    ionPopoverDidDismiss: EventSpy;
    constructor(page: E2EPage);
    setup(config: E2EPageOptions, options: SelectPopoverOption[], multiple?: boolean): Promise<void>;
    screenshot(screenshot: ScreenshotFn, name: string): Promise<void>;
    clickOption(value: string): Promise<void>;
    pressSpaceOnOption(value: string): Promise<void>;
    private getOption;
}
