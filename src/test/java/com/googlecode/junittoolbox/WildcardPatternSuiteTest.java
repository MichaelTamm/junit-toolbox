package com.googlecode.junittoolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.googlecode.junittoolbox.samples.AllSlowTests;
import com.googlecode.junittoolbox.samples.AllTests;
import com.googlecode.junittoolbox.samples.AllTestsBreadthFirst;
import com.googlecode.junittoolbox.samples.AllTestsInThisPackage;
import com.googlecode.junittoolbox.samples.LoginBeanTest;
import com.googlecode.junittoolbox.samples.NormalLoginTests;
import com.googlecode.junittoolbox.samples.backend.LoginBackendTest;
import com.googlecode.junittoolbox.samples.frontend.FillOutFormFrontendTest;
import com.googlecode.junittoolbox.samples.frontend.LoginFrontendTest;
import com.googlecode.junittoolbox.samples.suites.AllFrontendTests;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Runner;

import static com.googlecode.junittoolbox.TestHelper.getChildren;
import static com.googlecode.junittoolbox.TestHelper.hasItemWithTestClass;
import static com.googlecode.junittoolbox.TestHelper.hasItemWithTestMethod;
import static com.googlecode.junittoolbox.TestHelper.withTestClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WildcardPatternSuiteTest {

    @Test
    public void test_AllTests_sample_depth_first() throws Exception {
        Runner runner = ClassRequest.aClass(AllTests.class).getRunner();
        Collection<?> children = getChildren(runner);
        List<?> list = new ArrayList<>(children);

        assertThat(list.size(), is(4));
        assertThat(list.get(0), withTestClass(LoginBackendTest.class));
        assertThat(list.get(1), withTestClass(FillOutFormFrontendTest.class));
        assertThat(list.get(2), withTestClass(LoginFrontendTest.class));
        assertThat(list.get(3), withTestClass(LoginBeanTest.class));
    }

    @Test
    public void test_AllTests_sample_breadth_first() throws Exception {
        Runner runner = ClassRequest.aClass(AllTestsBreadthFirst.class).getRunner();
        Collection<?> children = getChildren(runner);
        List<?> list = new ArrayList<>(children);

        assertThat(list.size(), is(4));
        assertThat(list.get(0), withTestClass(LoginBeanTest.class));
        assertThat(list.get(1), withTestClass(LoginBackendTest.class));
        assertThat(list.get(2), withTestClass(FillOutFormFrontendTest.class));
        assertThat(list.get(3), withTestClass(LoginFrontendTest.class));
    }

    @Test
    public void test_AllTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTests.class).getRunner();
        Collection<?> children = getChildren(runner);
        assertThat(children.size(), is(4));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
        assertThat(children, hasItemWithTestClass(LoginBackendTest.class));
    }

    @Test
    public void test_AllTestsInThisPackage_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllTestsInThisPackage.class).getRunner();
        Collection<?> children = getChildren(runner);
        assertThat(children.size(), is(1));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.class));
    }

    @Test
    public void test_AllFrontendTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllFrontendTests.class).getRunner();
        Collection<?> children = getChildren(runner);
        assertThat(children.size(), is(2));
        assertThat(children, hasItemWithTestClass(LoginFrontendTest.class));
        assertThat(children, hasItemWithTestClass(FillOutFormFrontendTest.class));
    }

    @Test
    public void test_AllSlowTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(AllSlowTests.class).getRunner();
        Collection<?> children1 = getChildren(runner);
        assertThat(children1.size(), is(2));
        assertThat(children1, hasItemWithTestClass(LoginFrontendTest.class));
        final Runner loginFrontendTestRunner = (Runner) children1.iterator().next();
        Collection<?> children2 = getChildren(loginFrontendTestRunner);
        assertThat(children2.size(), is(1));
        assertThat(children2, hasItemWithTestMethod("slowTest"));
    }

    @Test
    public void test_NormalLoginTests_sample() throws Exception {
        Runner runner = ClassRequest.aClass(NormalLoginTests.class).getRunner();
        Collection<?> children1 = getChildren(runner);
        assertThat(children1.size(), is(1));
        assertThat(children1, hasItemWithTestClass(LoginFrontendTest.class));
        final Runner loginFrontendTestRunner = (Runner) children1.iterator().next();
        Collection<?> children2 = getChildren(loginFrontendTestRunner);
        assertThat(children2.size(), is(1));
        assertThat(children2, hasItemWithTestMethod("fastTest"));
    }
}
