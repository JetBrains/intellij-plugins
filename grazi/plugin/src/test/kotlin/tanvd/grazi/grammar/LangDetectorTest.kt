package tanvd.grazi.grammar

import tanvd.grazi.GraziTestBase
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangDetector

class LangDetectorTest : GraziTestBase(true) {
    private val langs = Lang.values().toList()

    fun `test english detection`() {
        var lang = LangDetector.getLang("The manager wants to speak to every employees in his office." +
                " The reason we were late is because there was an accident." +
                "We describe an unusual of benign prostatic hyperplasia that demonstrated florid sclerosing." +
                "At the time, Guider was working just two day's drive from Narooma, in a Canberra suburb." +
                "Your action with regarding to the decision of the committee is not satisfactory.", langs)
        assertEquals("en", lang?.shortCode)

        lang = LangDetector.getLang("It is baton", langs)
        assertEquals("en", lang?.shortCode)
    }

    fun `test russian detection`() {
        var lang = LangDetector.getLang("Благодаря предупреждению мы избежали неприятностей." +
                "Применение численных методов оптимизации никогда не может гарантировать, что получится найден глобальный экстремум." +
                "Когда я шёл по улице, у меня развязался шнурок." +
                "В ведение организации было передано имущество." +
                "Лаборатория была создана при исследовательском центре.", langs)
        assertEquals("ru", lang?.shortCode)

        lang = LangDetector.getLang("Это батон", langs)
        assertEquals("ru", lang?.shortCode)
    }

    fun `test german detection`() {
        var lang = LangDetector.getLang("Sie fand das ein passenderes Wort mit Hilfe des Synonymlexikons." +
                "Die Delfine gehören zu den Zahnwalen. Delfine sind in allen Meeren verbreitet." +
                "Dann hatten wir Freizeit. Dann gab es Essen. Schließlich gingen wir schlafen." +
                "Sie fand das ein passenderes Wort mit Hilfe des Synonymlexikons." +
                "Es wurde mir nicht an der wiege gesungen, dass ich mal an LanguageTool mitarbeiten würde.", langs)
        assertEquals("de", lang?.shortCode)

        lang = LangDetector.getLang("Er gab ihm recht", langs)
        assertEquals("de", lang?.shortCode)
    }

    fun `test chinese detection`() {
        var lang = LangDetector.getLang("实际生产的量 超过了计划的百分之百。" +
                "让我们执行一个合格公民应尽的责任" +
                "在这方面，一定要 加强 打击的力度." +
                "那只狗躺在门 门前 或 门以前 或 门的前边 前" +
                "我没考好的原因是因为我没有仔细审题", langs)
        assertEquals("zh", lang?.shortCode)

        lang = LangDetector.getLang("两 天", langs)
        assertEquals("zh", lang?.shortCode)
    }

    fun `test dutch detection`() {
        var lang = LangDetector.getLang("Het probleem is zich beginnen voordoen toen ik gebruik maakte van mijn nieuwe pc." +
                "Kinderwagens met ten minste twee afmetingen van meer dan 80 cm worden in T-treinen niet aangenomen." +
                "Godverdomme wat een bende, niks dan ma-juscule smurrie, chemisch spul dat giftig stonk." +
                "In de film zit zelfs een kleine scène in een badkamertje die de vernedering van fysieke menselijke zwakheid blootlegt." +
                "Indertijd in de jaren '60 was het knippen en plakken met de bandopnemer.", langs)
        assertEquals("nl", lang?.shortCode)

        lang = LangDetector.getLang("Vermoedelijk uit", langs)
        assertEquals("nl", lang?.shortCode)
    }

    fun `test french detection`() {
        var lang = LangDetector.getLang("Dans la conjoncture économique actuelle, les Français dépensent moins." +
                "L’équipe m’a demandé mon pronostique pour le match de demain." +
                "C’est le MIT qui a publié la première version de X Windows en juin 1984." +
                "Dicollecte vise à améliorer les dictionnaires orthographiques français." +
                "Vous devez vous adresser auprès du responsable.", langs)
        assertEquals("fr", lang?.shortCode)

        lang = LangDetector.getLang("a ce jour", langs)
        assertEquals("fr", lang?.shortCode)
    }

    fun `test greek detection`() {
        var lang = LangDetector.getLang("Οι βελτιώσεις των προσεγγίσεων είναι εμφανείς και οι μέθοδοι αυτοί είναι κατάλληλοι για την κατανόηση των μεθόδων μεγαλύτερης τάξης." +
                "Προσθέτουμε στο τρέχον έτος 1 μέρα και ο χρόνος αυτός ονομάζεται δίσεκτος." +
                "Το υπερρεαλιστικό κίνημα δεν είχε ποτέ κάποιο αποτέλεσμα σε ό,τι αφορά τις πολιτικές του τοποθετήσεις και προσπάθειες." +
                "Όσο μεγαλύτερη ροπή θέλουμε να μεταφέρουμε τόσο ισχυρότερα και περισσότερα πρέπει να είναι τα ελατήρια." +
                "Οι Έλληνες ήταν ανέκαθεν οι παραδοσιακοί έμποροι της Μεσογείου.", langs)
        assertEquals("el", lang?.shortCode)

        lang = LangDetector.getLang("Είχα πάει", langs)
        assertEquals("el", lang?.shortCode)
    }

    fun `test italian detection`() {
        var lang = LangDetector.getLang("Giorgio fu costretto a adire le vie legali per far valere i propri diritti." +
                "Ti avviso affinché tu puoi rimediare." +
                "Non sono vengono preparate con le tecnologie e le infrastrutture adatte." +
                "Bisogna cogliere la occasione migliore." +
                "E non si capisce il perché di tanta paura.", langs)
        assertEquals("it", lang?.shortCode)

        lang = LangDetector.getLang("Mi aspetti", langs)
        assertEquals("it", lang?.shortCode)
    }

    fun `test japanese detection`() {
        var lang = LangDetector.getLang("歌わさせていただきます" +
                "この件に関しては言わずもがなだ。" +
                "鼻にも掛けない。" +
                "取り沙汰される。" +
                "アルプス一万尺", langs)
        assertEquals("ja", lang?.shortCode)

        // FIXME japanese detection is awful
//        lang = LangDetector.getLang("しつこい", langs)
//        assertEquals("ja", lang?.shortCode)
    }

    fun `test persian detection`() {
        var lang = LangDetector.getLang("وی حاضر به همکاری شد." +
                "خون\u200Cآشام" +
                "سرشناس\u200Cتر" +
                "ر برای تو بود" +
                "چگونه به کار تو رسیدگی کنم؟", langs)
        assertEquals("fa", lang?.shortCode)

        lang = LangDetector.getLang("قرمزفام", langs)
        assertEquals("fa", lang?.shortCode)
    }

    fun `test polish detection`() {
        var lang = LangDetector.getLang("Jak będziesz w szpitalu wariatów, pozdrów Antoniego ode Aleksandra!" +
                "Odwrotna strona medalu jest taka, że nie chce być wyśmiana przez innych." +
                "Nie chodzi tu o temat, ale o sam fakt krępacji wynikający z uczestniczenia w dialogu z dziewczyną." +
                "Bogiem a prawdą jedynymi osobami, które mogą mieć niekłamaną satysfakcję ze zdobycia Everestu, są Hilary i Tenzing." +
                "Nie ulega wątpliwości, że znany aktor skutecznie odebrał premierowi i jego świcie powagę.", langs)
        assertEquals("pl", lang?.shortCode)

        lang = LangDetector.getLang("za pomocą", langs)
        assertEquals("pl", lang?.shortCode)
    }

    fun `test portuguese detection`() {
        var lang = LangDetector.getLang("Este programa é o que é preciso para evitar faltas de distração." +
                "Algumas pessoas e ideias parecem fora do lugar ou há frente do seu tempo." +
                "Os sensos demográficos têm particular interesse para a coordenação dos governos." +
                "As estirpes destas raças de cães são adequadas à sua personalidade." +
                "O Mário além de ter pedido uma indemnização, exigiu um pedido de desculpas.", langs)
        assertEquals("pt", lang?.shortCode)

        lang = LangDetector.getLang("criou novas", langs)
        assertEquals("pt", lang?.shortCode)
    }

    fun `test romanian detection`() {
        var lang = LangDetector.getLang("În ceea ce privește introducerea preliminară făcută de el, nu sunt de acord." +
                "El a captat atenția celor mai multor persoane cu studii superioare." +
                "El este unul dintre cei mai remarcabil cățărători ai tuturor timpurilor." +
                "A existat un permanent control al domniei reprezentată de orlicul de târg." +
                "Este un reprezentant al hoților, al clasei hulită de oamenii cinstiți", langs)
        assertEquals("ro", lang?.shortCode)

        lang = LangDetector.getLang("este rezolvabil", langs)
        assertEquals("ro", lang?.shortCode)
    }

    fun `test slovak detection`() {
        var lang = LangDetector.getLang("Aké pekné inteligentné dievča." +
                "Neučila sa, a predsa si poradila." +
                "Všetci ľudia sa rodia slobodní a sebe rovní, čo sa týka ich dôstojnosti a práv. " +
                "Sú obdarení rozumom a majú navzájom jednať v bratskom duchu." +
                "Pobelavé kaderie šije im obtáča, modré ich oči bystro v okolo si páča.", langs)
        assertEquals("sk", lang?.shortCode)

        lang = LangDetector.getLang("Aké pekné", langs)
        assertEquals("sk", lang?.shortCode)
    }

    fun `test spanish detection`() {
        var lang = LangDetector.getLang("Hay que esperar turno, dado a que en muchas ciudades la oferta es menor." +
                "Las cifras indican a grosso modo que la gestión es correcta." +
                "Se puede regresar a la zona basándonos en lo expuesto por las autoridades." +
                "Tuvo que afrontar muchos problemas con la mudanza." +
                "Tanto es así que los resultados fueron positivos", langs)
        assertEquals("es", lang?.shortCode)

        lang = LangDetector.getLang("los mozos", langs)
        assertEquals("es", lang?.shortCode)
    }

    fun `test ukrainian detection`() {
        var lang = LangDetector.getLang("зберігання та перероблювання продукції рослинництва. " +
                "Я ніколи не встигаю прочитати рухомий рядок на електронних рекламах." +
                "Шухевич знав про це, ще ж і надто, він допоміг виробити. " +
                "Виробнича слава України дорогого коштує марганчанам кажучи" +
                "Якщо потрібно з кимось порадитись, то це треба робити один на один.", langs)
        assertEquals("uk", lang?.shortCode)

        lang = LangDetector.getLang("по крайній мірі", langs)
        assertEquals("uk", lang?.shortCode)
    }
}
