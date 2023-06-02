// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
    selector: 'app-root',
    template: `
      <div style="text-align:center">
        
        <a #anchor></a>
        {{ testAnchor(anchor) }}
        
        <area #area>
        {{ testArea(area) }}
        
        <b #b></b>
        {{ testB(b) }}
        
        <br #br/>
        {{ testBR(br) }}
        
        <dl #dl></dl>
        {{ testDList(dl) }}
        
        <datalist #datalist></datalist>
        {{ testDataList(datalist) }}
        
        <fieldset #fieldset></fieldset>
        {{ testFieldSet(fieldset) }}
        
        <frameset #frameset></frameset>
        {{ testFrameSet(frameset) }}
        
        <hr #hr/>
        {{ testHR(hr) }}
        
        <h1 #h1></h1><h2 #h2></h2><h3 #h3></h3><h4 #h4></h4><h5 #h5></h5><h6 #h6></h6>
        {{ testHeading(h1) }} {{ testHeading(h2) }} {{ testHeading(h3) }} {{ testHeading(h4) }} {{ testHeading(h5) }} {{ testHeading(h6)}}
        
        <iframe #iframe></iframe>
        {{ testIFrame(iframe) }}
        
        <img #img>
        {{ testImage(img) }}
        
        <li #li></li>
        {{ testLI(li) }}
        
        <label #label></label>
        {{ testLabel(label) }}
        
        <ins #ins></ins><del #del></del>
        {{ testMod(ins) }} {{ testMod(del) }}
        
        <nav #nav></nav>
        {{ testNav(nav) }}
        
        <ol #ol></ol>
        {{ testOList(ol) }}
        
        <optgroup #optgroup></optgroup>
        {{ testOptGroup(optgroup) }}
        
        <p #p></p>
        {{ testParagraph(p) }}
        
        <blockquote #blockquote></blockquote><q #q></q>
        {{ testQuote(blockquote) }} {{ testQuote(q) }}
        
        <caption #caption></caption>
        {{ testTableCaption(caption) }}
          
        <col #col/><colgroup #colgroup></colgroup>
        {{ testTableCol(col) }} {{ testTableCol(colgroup) }}
        
        <td #td></td>
        {{ testTableDataCell(td) }}
        
        <th #th></th>
        {{ testTableHeaderCell(th) }}
        
        <tr #tr></tr>
        {{ testTableRow(tr) }}
        
        <tfoot #tfoot></tfoot><thead #thead></thead><tbody #tbody></tbody>
        {{ testTableSection(tfoot) }} {{ testTableSection(thead) }} {{ testTableSection(tbody) }}

        <textarea #textarea></textarea>
        {{ testTextArea(textarea) }}
        
        <ul #ul></ul>
        {{ testUList(ul) }}

        {{ testAnchor(<error descr="Argument type  HTMLUListElement  is not assignable to parameter type  HTMLAnchorElement ">ul</error>) }}
        {{ testFieldSet(<error descr="Argument type  HTMLTableSectionElement  is not assignable to parameter type  HTMLFieldSetElement ">tfoot</error>) }}
        
      </div>  
    `
})
export class AppComponent {
    testAnchor(val: HTMLAnchorElement) {}
    testArea(val: HTMLAreaElement) {}
    testB(val: HTMLElement) {}
    testBR(val: HTMLBRElement) {}
    testDList(val: HTMLDListElement) {}
    testDataList(val: HTMLDataListElement) {}
    testFieldSet(val: HTMLFieldSetElement) {}
    testFrameSet(val: HTMLFrameSetElement) {}
    testHR(val: HTMLHRElement) {}
    testHeading(val: HTMLHeadingElement) {}
    testIFrame(val: HTMLIFrameElement) {}
    testImage(val: HTMLImageElement) {}
    testLI(val: HTMLLIElement) {}
    testLabel(val: HTMLLabelElement) {}
    testMod(val: HTMLModElement) {}
    testNav(val: HTMLElement) {}
    testOList(val: HTMLOListElement) {}
    testOptGroup(val: HTMLOptGroupElement) {}
    testParagraph(val: HTMLParagraphElement) {}
    testQuote(val: HTMLQuoteElement) {}
    testTableCaption(val: HTMLTableCaptionElement) {}
    testTableCol(val: HTMLTableColElement) {}
    testTableDataCell(val: HTMLTableDataCellElement) {}
    testTableHeaderCell(val: HTMLTableHeaderCellElement) {}
    testTableRow(val: HTMLTableRowElement) {}
    testTableSection(val: HTMLTableSectionElement) {}
    testTextArea(val: HTMLTextAreaElement) {}
    testUList(val: HTMLUListElement) {}
}