package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.samples.LoginBeanTest;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Runner;

import java.util.List;

import static com.googlecode.junittoolbox.TestHelper.getChildren;
import static com.googlecode.junittoolbox.TestHelper.hasItemWithTestClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class InnerTestClassesSuiteTest {

    @Test
    public void test() throws Exception {
        Runner runner = ClassRequest.aClass(LoginBeanTest.class).getRunner();
        List<?> children = getChildren(runner);
        assertThat(children.size(), is(3));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.UnitTests.class));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.IntegrationTests.class));
        assertThat(children, hasItemWithTestClass(LoginBeanTest.TheoryTests.class));
    }
}
