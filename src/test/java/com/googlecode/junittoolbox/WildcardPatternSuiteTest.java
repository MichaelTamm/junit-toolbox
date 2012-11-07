package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.samples.*;
import com.googlecode.junittoolbox.samples.AllTests;
import com.googlecode.junittoolbox.samples.frontend.FillOutFormFrontendTest;
import com.googlecode.junittoolbox.samples.frontend.LoginFrontendTest;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Runner;

import java.util.List;

import static com.googlecode.junittoolbox.TestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WildcardPatternSuiteTest {

    @Test
    public void test_AllTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTests.class).getRunner();
        List<?> children = getChildren(runner);
        assertThat(children.size(), is(3));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
    }

    @Test
    public void test_AllTestsInThisPackage_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTestsInThisPackage.class).getRunner();
        List<?> children = getChildren(runner);
        assertThat(children.size(), is(1));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
    }

    @Test
    public void test_AllFrontendTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllFrontendTests.class).getRunner();
        List<?> children = getChildren(runner);
        assertThat(children.size(), is(2));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
    }

    @Test
    public void test_AllSlowTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllSlowTests.class).getRunner();
        List<?> children1 = getChildren(runner);
        assertThat(children1.size(), is(1));
        assertThat(children1, hasItemWithTestClass(LoginFrontendTest.class));
        final Runner loginFrontendTestRunner = (Runner) children1.get(0);
        List<?> children2 = getChildren(loginFrontendTestRunner);
        assertThat(children2.size(), is(1));
        assertThat(children2, hasItemWithTestMethod("slowTest"));
    }
}
