<template>
  <a
    @click='(fooo + <weak_warning descr="Unresolved variable or type baar">baar</weak_warning>)()'
    title="<TYPO descr="Typo: In word 'prblematic'">prblematic</TYPO> Beh&auml;lter <TYPO descr="Typo: In word 'tekst'">tekst</TYPO>"

  >
    nonbre&nbsp;aking space
    Beh&auml;lter

    <TYPO descr="Typo: In word 'ntml'">ntml</TYPO> ther{{'<TYPO descr="Typo: In word 'apeu'">apeu</TYPO>'}}thic <TYPO descr="Typo: In word 'prblems'">prblems</TYPO>
  </a>
</template>
<script setup>
  let <TYPO descr="Typo: In word 'fooo'">fooo</TYPO> = 12
  fooo + <weak_warning descr="Unresolved variable or type baar">baar</weak_warning>
</script>