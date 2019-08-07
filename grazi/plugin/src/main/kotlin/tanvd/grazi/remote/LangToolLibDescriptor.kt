package tanvd.grazi.remote

enum class LangToolLibDescriptor(val langsClasses: List<String>, val size: String) {
    ENGLISH(listOf("BritishEnglish", "AmericanEnglish", "CanadianEnglish", "AustralianEnglish", "NewZealandEnglish", "SouthAfricanEnglish"), "14 MB"),
    RUSSIAN(listOf("Russian"), "3 MB"),
    PERSIAN(listOf("Persian"), "1 MB"),
    FRENCH(listOf("French"), "4 MB"),
    GERMAN(listOf("GermanyGerman", "AustrianGerman", "SwissGerman"), "19 MB"),
    POLISH(listOf("Polish"), "5 MB"),
    ITALIAN(listOf("Italian"), "1 MB"),
    DUTCH(listOf("Dutch"), "17 MB"),
    PORTUGUESE(listOf( "PortugalPortuguese", "BrazilianPortuguese", "AngolaPortuguese", "MozambiquePortuguese"), "5 MB"),
    CHINESE(listOf("Chinese"), "3 MB"),
    GREEK(listOf("Greek"),  "1 MB"),
    JAPANESE(listOf("Japanese"), "1 MB"),
    ROMANIAN(listOf("Romanian"), "1 MB"),
    SLOVAK(listOf("Slovak"),"3 MB"),
    SPANISH(listOf("Spanish"),  "2 MB"),
    UKRAINIAN(listOf("Ukrainian"), "6 MB");
}
