package training.testFramework;

import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.IdeaTestFixtureFactoryImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by karashevich on 03/11/15.
 */
public class LearnIdeaTestFixtureFactoryImpl extends IdeaTestFixtureFactoryImpl{

    private static final LearnIdeaTestFixtureFactoryImpl ourInstance = new LearnIdeaTestFixtureFactoryImpl();

    @NotNull
    public static LearnIdeaTestFixtureFactoryImpl getFixtureFactory() {
        IdeaTestFixtureFactory.getFixtureFactory();
        return ourInstance;
    }


    @NotNull
    public TestFixtureBuilder<IdeaProjectTestFixture> createFixtureBuilder(@NotNull String name){
        return new LearnIdeaTestFixtureBuilderImpl(new LearnIdeaTestFixtureImpl(name), myFixtureBuilderProviders);
    }
}
