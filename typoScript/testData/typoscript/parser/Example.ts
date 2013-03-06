/*
 *   Menu description
 */
<INCLUDE_TYPOSCRIPT: source="FILE: folder/html/typoscript.txt">
temp.menu = COA
temp.menu { some ignored text
  10 = HMENU
  10.entryLevel = 0
  10.1 = TMENU
  10.1 {
    wrap = <div class="menuBar" style="width:80%;">|</div>
    NO.ATagParams = class="menuButton"
  }
}

# Default PAGE object:
page = PAGE
[GLOBAL]
page.typeNum = 0
page.10 < temp.menu
&test = myText