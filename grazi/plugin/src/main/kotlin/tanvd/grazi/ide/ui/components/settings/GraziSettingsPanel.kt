package tanvd.grazi.ide.ui.components.settings

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.guessCurrentProject
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.VerticalLayout
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.components.dsl.border
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.ide.ui.components.dsl.panel
import tanvd.grazi.language.Lang
import tanvd.grazi.remote.GraziRemote
import javax.swing.JComponent

class GraziSettingsPanel : ConfigurableUi<GraziConfig>, Disposable {
    private val spellcheck = GraziSpellcheckComponent()
    private val native = GraziNativeLanguageComponent(::download)
    private val description = GraziRuleDescriptionComponent()
    private val rules = GraziRulesComponent(description.listener)
    private val languages = GraziLanguagesComponent(::download, rules::addLang, rules::removeLang)

    private fun download(lang: Lang): Boolean {
        val isSucceed = GraziRemote.download(lang, guessCurrentProject(spellcheck.component))
        if (isSucceed) update()
        return isSucceed
    }

    private fun update() {
        native.update()
        languages.update()
        rules.reset()
    }

    fun showOption(option: String?) = Runnable { rules.filter(option ?: "") }

    override fun isModified(settings: GraziConfig) = rules.isModified
            .or(settings.state.enabledSpellcheck != spellcheck.isSpellcheckEnabled)
            .or(settings.state.nativeLanguage != native.language)
            .or(settings.state.enabledLanguages != languages.values)

    override fun apply(settings: GraziConfig) {
        GraziConfig.update { state ->
            val enabledLanguages = state.enabledLanguages.toMutableSet()
            val userDisabledRules = state.userDisabledRules.toMutableSet()
            val userEnabledRules = state.userEnabledRules.toMutableSet()

            val chosenEnabledLanguages = languages.values
            Lang.values().forEach {
                if (chosenEnabledLanguages.contains(it)) {
                    enabledLanguages.add(it)
                } else {
                    enabledLanguages.remove(it)
                }
            }

            val (enabledRules, disabledRules) = rules.state

            enabledRules.forEach { id ->
                userDisabledRules.remove(id)
                userEnabledRules.add(id)
            }

            disabledRules.forEach { id ->
                userDisabledRules.add(id)
                userEnabledRules.remove(id)
            }

            state.update(
                    enabledLanguages = enabledLanguages,
                    userEnabledRules = userEnabledRules,
                    userDisabledRules = userDisabledRules,
                    nativeLanguage = native.language,
                    enabledSpellcheck = spellcheck.isSpellcheckEnabled
            )
        }

        rules.reset()
    }

    override fun reset(settings: GraziConfig) {
        native.language = settings.state.nativeLanguage
        spellcheck.isSpellcheckEnabled = settings.state.enabledSpellcheck
        languages.reset(settings.state.enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName)))
        rules.reset()

        update()
    }

    override fun getComponent(): JComponent {
        return panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().index(1).grow())) {
            panel(MigLayout(createLayoutConstraints(), AC().grow()), constraint = CC().growX().wrap()) {
                border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.insetsBottom(10), false)

                add(languages.component, CC().growX().maxHeight("").width("45%").minWidth("250px").minHeight("120px").maxHeight("120px").alignY("top"))

                panel(VerticalLayout(), CC().grow().width("55%").minWidth("250px").alignY("top")) {
                    border = padding(JBUI.insetsLeft(20))
                    add(native.component)
                    add(spellcheck.component)
                }

                update()
            }

            panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().grow()), constraint = CC().grow()) {
                border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

                add(rules.component, CC().grow().width("45%").minWidth("250px"))
                add(description.component, CC().grow().width("55%"))
            }
        }
    }

    override fun dispose() = rules.dispose()
}
