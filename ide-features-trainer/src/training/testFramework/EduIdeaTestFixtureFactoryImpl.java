package training.testFramework;

import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.IdeaTestFixtureFactoryImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by karashevich on 03/11/15.
 */
public class EduIdeaTestFixtureFactoryImpl extends IdeaTestFixtureFactoryImpl{

    private static final EduIdeaTestFixtureFactoryImpl ourInstance = new EduIdeaTestFixtureFactoryImpl();

    @NotNull
    public static EduIdeaTestFixtureFactoryImpl getFixtureFactory() {
        IdeaTestFixtureFactory.getFixtureFactory();
        return ourInstance;
    }


    @NotNull
    public TestFixtureBuilder<IdeaProjectTestFixture> createFixtureBuilder(@NotNull String name){
        return new EduIdeaTestFixtureBuilderImpl(new EduIdeaTestFixtureImpl(name), myFixtureBuilderProviders);
    }
}
