import {storeToRefs} from 'pinia';
import {useSettingsStore} from "./settingsStore";

export default function useSettings() {
    const settings = useSettingsStore();
    const {isAutoChangeTheme, isDarkMode, isOfflineMode} =
        storeToRefs(settings);

    return {
        isAutoChangeTheme,
        isDarkMode,
        isOfflineMode,
    };
}