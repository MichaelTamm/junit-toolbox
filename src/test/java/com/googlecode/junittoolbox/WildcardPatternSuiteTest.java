package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.samples.AllFrontendTests;
import com.googlecode.junittoolbox.samples.AllTests;
import com.googlecode.junittoolbox.samples.AllTestsInThisPackage;
import com.googlecode.junittoolbox.samples.LoginBeanTest;
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
        List<Runner> children = getChildren(runner);
        assertThat(children.size(), is(3));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
    }

    @Test
    public void test_AllTestsInThisPackage_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTestsInThisPackage.class).getRunner();
        List<Runner> children = getChildren(runner);
        assertThat(children.size(), is(1));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
    }

    @Test
    public void test_AllFrontendTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllFrontendTests.class).getRunner();
        List<Runner> children = getChildren(runner);
        assertThat(children.size(), is(2));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
    }
}
