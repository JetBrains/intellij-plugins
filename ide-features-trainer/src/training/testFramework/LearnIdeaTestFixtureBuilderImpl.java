package training.testFramework;

import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.util.pico.ConstructorInjectionComponentAdapter;
import com.intellij.util.pico.DefaultPicoContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Field;
import java.util.Map;

class LearnIdeaTestFixtureBuilderImpl implements TestFixtureBuilder<IdeaProjectTestFixture> {
    private final LearnIdeaTestFixtureImpl myFixture;
    private final Map<Class<? extends ModuleFixtureBuilder>, Class<? extends ModuleFixtureBuilder>> myProviders;
    private final MutablePicoContainer myContainer;

    LearnIdeaTestFixtureBuilderImpl(LearnIdeaTestFixtureImpl fixture, final Map<Class<? extends ModuleFixtureBuilder>, Class<? extends ModuleFixtureBuilder>> providers) {
        myFixture = fixture;
        myProviders = providers;

        myContainer = new DefaultPicoContainer();
        myContainer.registerComponentInstance(this);
    }

    private <M extends ModuleFixtureBuilder> M createModuleBuilder(Class<M> key) {
        Class<? extends ModuleFixtureBuilder> implClass = myProviders.get(key);
        Assert.assertNotNull(key.toString(), implClass);
        final ConstructorInjectionComponentAdapter adapter = new ConstructorInjectionComponentAdapter(implClass, implClass, null, true);
        return (M)adapter.getComponentInstance(myContainer);
    }

    @NotNull
    @Override
    public LearnIdeaTestFixtureImpl getFixture() {
        return myFixture;
    }

    @Override
    public <M extends ModuleFixtureBuilder> M addModule(final Class<M> builderClass) {
        loadClassConstants(builderClass);
        final M builder = createModuleBuilder(builderClass);
        myFixture.addModuleFixtureBuilder(builder);
        return builder;
    }

    private static void loadClassConstants(final Class builderClass) {
        try {
            for (final Field field : builderClass.getFields()) {
                field.get(null);
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

