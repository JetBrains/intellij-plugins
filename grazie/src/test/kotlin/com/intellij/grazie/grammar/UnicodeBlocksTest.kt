package com.intellij.grazie.grammar

import com.intellij.grazie.jlanguage.LangUnicodeBlock
import com.intellij.testFramework.UsefulTestCase

class UnicodeBlocksTest : UsefulTestCase() {
  private fun check(sentence: String, domain: Set<Character.UnicodeBlock>): Boolean {
    var offset = 0
    while (offset < sentence.length) {
      val codepoint = Character.codePointAt(sentence, offset)

      if (sentence[offset] !in setOf(' ', '.', ',') && Character.UnicodeBlock.of(codepoint) !in domain) {
        println(Character.UnicodeBlock.of(codepoint))
        println(sentence[offset])
        return false
      }

      offset += Character.charCount(codepoint)
    }

    return true
  }

  fun `test english unicode blocks`() {
    val sentence = "The manager wants to speak to every employees in his office." +
      " The reason we were late is because there was an accident." +
      "We describe an unusual of benign prostatic hyperplasia that demonstrated florid sclerosing." +
      "At the time, Guider was working just two day's drive from Narooma, in a Canberra suburb." +
      "Your action with regarding to the decision of the committee is not satisfactory."
    assertTrue(check(sentence, LangUnicodeBlock.ENGLISH.blocks))
  }

  fun `test russian unicode blocks`() {
    val sentence = "Благодаря предупреждению мы избежали неприятностей." +
      "Применение численных методов оптимизации никогда не может гарантировать, что получится найден глобальный экстремум." +
      "Когда я шёл по улице, у меня развязался шнурок." +
      "В ведение организации было передано имущество." +
      "Лаборатория была создана при исследовательском центре."
    assertTrue(check(sentence, LangUnicodeBlock.RUSSIAN.blocks))
  }

  fun `test german unicode blocks`() {
    val sentence = "Sie fand das ein passenderes Wort mit Hilfe des Synonymlexikons." +
      "Die Delfine gehören zu den Zahnwalen. Delfine sind in allen Meeren verbreitet." +
      "Dann hatten wir Freizeit. Dann gab es Essen. Schließlich gingen wir schlafen." +
      "Sie fand das ein passenderes Wort mit Hilfe des Synonymlexikons." +
      "Es wurde mir nicht an der wiege gesungen, dass ich mal an LanguageTool mitarbeiten würde."
    assertTrue(check(sentence, LangUnicodeBlock.GERMAN.blocks))
  }

  fun `test chinese unicode blocks`() {
    val sentence = "实际生产的量 超过了计划的百分之百。" +
      "让我们执行一个合格公民应尽的责任" +
      "在这方面，一定要 加强 打击的力度." +
      "那只狗躺在门 门前 或 门以前 或 门的前边 前" +
      "我没考好的原因是因为我没有仔细审题"
    assertTrue(check(sentence, LangUnicodeBlock.CHINESE.blocks))
  }

  fun `test dutch unicode blocks`() {
    val sentence = "Het probleem is zich beginnen voordoen toen ik gebruik maakte van mijn nieuwe pc." +
      "Kinderwagens met ten minste twee afmetingen van meer dan 80 cm worden in T-treinen niet aangenomen." +
      "Godverdomme wat een bende, niks dan ma-juscule smurrie, chemisch spul dat giftig stonk." +
      "In de film zit zelfs een kleine scène in een badkamertje die de vernedering van fysieke menselijke zwakheid blootlegt." +
      "Indertijd in de jaren '60 was het knippen en plakken met de bandopnemer."
    assertTrue(check(sentence, LangUnicodeBlock.DUTCH.blocks))
  }

  fun `test french unicode blocks`() {
    val sentence = "Dans la conjoncture économique actuelle, les Français dépensent moins." +
      "L’équipe m’a demandé mon pronostique pour le match de demain." +
      "C’est le MIT qui a publié la première version de X Windows en juin 1984." +
      "Dicollecte vise à améliorer les dictionnaires orthographiques français." +
      "Vous devez vous adresser auprès du responsable."
    assertTrue(check(sentence, LangUnicodeBlock.FRENCH.blocks))
  }

  fun `test greek unicode blocks`() {
    val sentence =
      "Οι βελτιώσεις των προσεγγίσεων είναι εμφανείς και οι μέθοδοι αυτοί είναι κατάλληλοι για την κατανόηση των μεθόδων μεγαλύτερης τάξης." +
        "Προσθέτουμε στο τρέχον έτος μέρα και ο χρόνος αυτός ονομάζεται δίσεκτος." +
        "Το υπερρεαλιστικό κίνημα δεν είχε ποτέ κάποιο αποτέλεσμα σε ό,τι αφορά τις πολιτικές του τοποθετήσεις και προσπάθειες." +
        "Όσο μεγαλύτερη ροπή θέλουμε να μεταφέρουμε τόσο ισχυρότερα και περισσότερα πρέπει να είναι τα ελατήρια." +
        "Οι Έλληνες ήταν ανέκαθεν οι παραδοσιακοί έμποροι της Μεσογείου."
    assertTrue(check(sentence, LangUnicodeBlock.GREEK.blocks))
  }

  fun `test italian unicode blocs`() {
    val sentence = "Giorgio fu costretto a adire le vie legali per far valere i propri diritti." +
      "Ti avviso affinché tu puoi rimediare." +
      "Non sono vengono preparate con le tecnologie e le infrastrutture adatte." +
      "Bisogna cogliere la occasione migliore." +
      "E non si capisce il perché di tanta paura."
    assertTrue(check(sentence, LangUnicodeBlock.ITALIAN.blocks))
  }

  fun `test japanese unicode blocks`() {
    val sentence = "歌わさせていただきます" +
      "この件に関しては言わずもがなだ。" +
      "鼻にも掛けない。" +
      "取り沙汰される。" +
      "アルプス一万尺"
    assertTrue(check(sentence, LangUnicodeBlock.JAPANESE.blocks))
  }

  fun `test persian unicode blocks`() {
    val sentence = "وی حاضر به همکاری شد." +
      "خون\u200Cآشام" +
      "سرشناس\u200Cتر" +
      "ر برای تو بود" +
      "چگونه به کار تو رسیدگی کنم؟"
    assertTrue(check(sentence, LangUnicodeBlock.PERSIAN.blocks))
  }

  fun `test polish unicode blocks`() {
    val sentence = "Jak będziesz w szpitalu wariatów, pozdrów Antoniego ode Aleksandra!" +
      "Odwrotna strona medalu jest taka, że nie chce być wyśmiana przez innych." +
      "Nie chodzi tu o temat, ale o sam fakt krępacji wynikający z uczestniczenia w dialogu z dziewczyną." +
      "Bogiem a prawdą jedynymi osobami, które mogą mieć niekłamaną satysfakcję ze zdobycia Everestu, są Hilary i Tenzing." +
      "Nie ulega wątpliwości, że znany aktor skutecznie odebrał premierowi i jego świcie powagę."
    assertTrue(check(sentence, LangUnicodeBlock.POLISH.blocks))
  }

  fun `test portuguese unicode blocks`() {
    val sentence = "Este programa é o que é preciso para evitar faltas de distração." +
      "Algumas pessoas e ideias parecem fora do lugar ou há frente do seu tempo." +
      "Os sensos demográficos têm particular interesse para a coordenação dos governos." +
      "As estirpes destas raças de cães são adequadas à sua personalidade." +
      "O Mário além de ter pedido uma indemnização, exigiu um pedido de desculpas."
    assertTrue(check(sentence, LangUnicodeBlock.PORTUGUESE.blocks))
  }

  fun `test romanian unicode blocks`() {
    val sentence = "În ceea ce privește introducerea preliminară făcută de el, nu sunt de acord." +
      "El a captat atenția celor mai multor persoane cu studii superioare." +
      "El este unul dintre cei mai remarcabil cățărători ai tuturor timpurilor." +
      "A existat un permanent control al domniei reprezentată de orlicul de târg." +
      "Este un reprezentant al hoților, al clasei hulită de oamenii cinstiți"
    assertTrue(check(sentence, LangUnicodeBlock.ROMANIAN.blocks))
  }

  fun `test slovak unicode blocks`() {
    val sentence = "Aké pekné inteligentné dievča." +
      "Neučila sa, a predsa si poradila." +
      "Všetci ľudia sa rodia slobodní a sebe rovní, čo sa týka ich dôstojnosti a práv. " +
      "Sú obdarení rozumom a majú navzájom jednať v bratskom duchu." +
      "Pobelavé kaderie šije im obtáča, modré ich oči bystro v okolo si páča."
    assertTrue(check(sentence, LangUnicodeBlock.SLOVAK.blocks))
  }

  fun `test spanish unicode blocks`() {
    val sentence = "Hay que esperar turno, dado a que en muchas ciudades la oferta es menor." +
      "Las cifras indican a grosso modo que la gestión es correcta." +
      "Se puede regresar a la zona basándonos en lo expuesto por las autoridades." +
      "Tuvo que afrontar muchos problemas con la mudanza." +
      "Tanto es así que los resultados fueron positivos"
    assertTrue(check(sentence, LangUnicodeBlock.SPANISH.blocks))
  }

  fun `test ukrainian unicode blocks`() {
    val sentence = "зберігання та перероблювання продукції рослинництва. " +
      "Я ніколи не встигаю прочитати рухомий рядок на електронних рекламах." +
      "Шухевич знав про це, ще ж і надто, він допоміг виробити. " +
      "Виробнича слава України дорогого коштує марганчанам кажучи" +
      "Якщо потрібно з кимось порадитись, то це треба робити один на один."
    assertTrue(check(sentence, LangUnicodeBlock.UKRAINIAN.blocks))
  }
}
