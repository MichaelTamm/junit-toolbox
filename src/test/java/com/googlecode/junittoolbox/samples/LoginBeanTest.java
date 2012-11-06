package com.googlecode.junittoolbox.samples;

import com.googlecode.junittoolbox.InnerTestClassesSuite;
import com.googlecode.junittoolbox.InnerTestClassesSuiteTest;
import com.googlecode.junittoolbox.WildcardPatternSuiteTest;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * For {@link WildcardPatternSuiteTest} and {@link InnerTestClassesSuiteTest}.
 */
@RunWith(InnerTestClassesSuite.class)
public class LoginBeanTest {

    public static class UnitTests {
        @Test
        public void unitTestMethod() {}
    }

    public static class IntegrationTests {
        @Test
        public void integrationTestMethod() {}
    }

    @RunWith(Theories.class)
    public static class TheoryTests {
        @Theory
        public void theoryMethod() {}
    }

    public static class NotATestClass {}
}
