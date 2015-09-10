package training.keymap;

//private val winKeymap = KeymapManager.getInstance()!!.getKeymap(KeymapKind.WIN.defaultKeymapName)
//private val macKeymap = KeymapManager.getInstance()!!.getKeymap(KeymapKind.MAC.defaultKeymapName);

import com.intellij.util.PlatformUtils;

public class KeymapDescription {

    public enum KeymapKindType {


        WIN{
            @Override
            public void setDisplayName(String displayName) {
                this.displayName = "Win/Linux";
            }

            @Override
            public void setDefaultKeymapName(String defaultKeymapName) {
                this.defaultKeymapName = "\\$default";
            }
        }
        , MAC {
            @Override
            public void setDisplayName(String displayName) {
                this.displayName = "Mac";
            }

            @Override
            public void setDefaultKeymapName(String defaultKeymapName) {
                this.defaultKeymapName = "Mac OS X 10.5+";
            }

        };

        public String displayName;
        public String defaultKeymapName;

        public void setDefaultKeymapName(String defaultKeymapName) {
            this.defaultKeymapName = defaultKeymapName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDefaultKeymapName() {
            return this.defaultKeymapName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public KeymapKindType getAlternativeKind() {
            if (this == WIN) return MAC;
            else {
                if (PlatformUtils.isAppCode()) return null;
                else return WIN;
            }
        }
    }

//    public KeymapDescription(String name) {
//        KeymapManager.getInstance().getKeymap(KeymapKindType.WIN)
//    }
}

//enum class KeymapKind(val displayName: String, val defaultKeymapName: String) {
//    WIN: KeymapKind("Win/Linux", "\$default")
//    MAC: KeymapKind("Mac", "Mac OS X 10.5+")
//
//    fun getAlternativeKind() = when (this) {
//        WIN -> MAC
//        MAC -> if (PlatformUtils.isAppCode()) null else WIN
//    }
//}

//fun getCurrentOSKind() = when {
//    SystemInfo.isMac -> KeymapKind.MAC
//    else -> KeymapKind.WIN
//}

//fun KeymapKind.getKeymap() = when (this) {
//    KeymapKind.WIN -> winKeymap
//    KeymapKind.MAC -> macKeymap
//}

//class KeymapDescription(var name: String = "", var displayText: String = "") {
//    fun getKind() = if (name.contains("Mac OS")) KeymapKind.MAC else KeymapKind.WIN
//    fun getKeymap() = KeymapManager.getInstance()!!.getKeymap(name)
//
//    override fun equals(other: Any?): Boolean {
//        return other is KeymapDescription && other.name == name && other.displayText == displayText
//    }
//
//    override fun hashCode(): Int {
//        return name.hashCode() + 31*displayText.hashCode()
//    }
//}
//
//fun getDefaultMainKeymap() = KeymapDescription(getCurrentOSKind().defaultKeymapName, "")
//fun getDefaultAlternativeKeymap() =
//        getCurrentOSKind().getAlternativeKind()?.let { KeymapDescription(it.defaultKeymapName, "for ${it.displayName}") }
