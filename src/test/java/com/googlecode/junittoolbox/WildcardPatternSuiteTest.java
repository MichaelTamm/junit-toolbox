package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.samples.AllFrontendTests;
import com.googlecode.junittoolbox.samples.AllTests;
import com.googlecode.junittoolbox.samples.AllTestsInThisPackage;
import com.googlecode.junittoolbox.samples.LoginBeanTest;
import com.googlecode.junittoolbox.samples.frontend.FillOutFormFrontendTest;
import com.googlecode.junittoolbox.samples.frontend.LoginFrontendTest;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Runner;
import org.junit.runners.Suite;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WildcardPatternSuiteTest {

    @Test
    public void test_AllTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTests.class).getRunner();
        List children = getChildren(runner);
        assertThat(children.size(), is(3));
        assertThat(children, hasItem(withTestClass(LoginBeanTest.class)));
        assertThat(children, hasItem(withTestClass(LoginFrontendTest.class)));
        assertThat(children, hasItem(withTestClass(FillOutFormFrontendTest.class)));
    }

    @Test
    public void test_AllTestsInThisPackage_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTestsInThisPackage.class).getRunner();
        List children = getChildren(runner);
        assertThat(children.size(), is(1));
        assertThat(children, hasItem(withTestClass(LoginBeanTest.class)));
    }

    @Test
    public void test_AllFrontendTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllFrontendTests.class).getRunner();
        List children = getChildren(runner);
        assertThat(children.size(), is(2));
        assertThat(children, hasItem(withTestClass(LoginFrontendTest.class)));
        assertThat(children, hasItem(withTestClass(FillOutFormFrontendTest.class)));
    }

    private List getChildren(Runner runner) throws Exception {
        Field fRunnersField = Suite.class.getDeclaredField("fRunners");
        fRunnersField.setAccessible(true);
        return (List) fRunnersField.get(runner);
    }

    private Matcher<Runner> withTestClass(final Class<?> testClass) {
        return new CustomTypeSafeMatcher<Runner>("with test class " + testClass.getName()) {
            @Override
            protected boolean matchesSafely(Runner runner) {
                return testClass.equals(runner.getDescription().getTestClass());
            }
        };
    }
}
