# Overview #

The JUnit Toolbox provides some useful classes for writing automated tests with JUnit:
  * [MultithreadingTester](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/MultithreadingTester.html) -- Helper class for writing stress tests using multiple, concurrently running threads
  * [PollingWait](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/PollingWait.html) -- Helper class to wait for asynchronous operations
  * [ParallelRunner](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelRunner.html) -- Executes all `@Test` methods as well as the calls to `@Theory` methods  with different parameter assignments concurrently using several worker threads.
  * [ParallelParameterized](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelParameterized.html) -- A replacement for the JUnit runner `Parameterized` which executes the tests for each parameter set concurrently.
  * [WildcardPatternSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/WildcardPatternSuite.html) -- A replacement for the JUnit runners `Suite` and `Categories`, which allows you to specify the children classes of your test suite class using a wildcard pattern. Furthermore you can include and/or exclude multiple categories.
  * [ParallelSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelSuite.html) -- An extension of the `WildcardPatternSuite`, which executes its children classes concurrently using several worker threads. Although it extends `WildcardPatternSuite` you are not forced to use a wildcard pattern, you can also list the children class using the `@SuiteClasses` annotation known from JUnit.
  * [InnerTestClassesSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/InnerTestClassesSuite.html) -- A replacement for the JUnit runner `Enclosed` which executes all inner test classes of the class annotated with ` @RunWith(InnerTestClassesSuite.class)`. In contrast to the `Enclosed` runner provided by JUnit it detects if an inner class is actually a test class and ignores all other inner classes.

`ParallelRunner`, `ParallelParameterized`, and `ParallelSuite` share a common Fork-Join-Pool. You can control the maximum number of worker threads by specifying the system property `maxParallelTestThreads`. If this system property is not set, there will be as many worker threads as the number of processors available to the JVM.

# How to use it #

If you use [Maven](http://maven.apache.org), add the following dependency to your `pom.xml` file:
```
<dependency>
    <groupId>com.googlecode.junit-toolbox</groupId>
    <artifactId>junit-toolbox</artifactId>
    <version>2.4</version>
</dependency>
```

# Release Notes #

## Version 2.4 (for Java 8) ##
 * [WildcardPatternSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/WildcardPatternSuite.html) can now handle wildcard patterns starting with "../" (fixes [#16](https://github.com/MichaelTamm/junit-toolbox/issues/16))
 * Fixed edge case where too many threads were created when using one of [ParallelRunner](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelRunner.html),  [ParallelParameterized](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelParameterized.html), or  [ParallelSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelSuite.html) contributed by Till Klister 

## Version 2.3 (for Java 8) ##
  * Improved handling of `AssumptionViolatedException` in [ParallelRunner](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelRunner.html) contributed by Christian Grotheer (fixes [#12](https://github.com/MichaelTamm/junit-toolbox/issues/12)).

## Version 2.2 (for Java 8) and Version 1.10 (for Java 6) ##
  * Updated to JUnit 4.12
  * Improved [WildcardPatternSuite](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/WildcardPatternSuite.html): it now ignores classes, which match the specified wildcard pattern, but are not test classes (fixes issue #8)

## Version 2.1 (for Java 8) and Version 1.9 (for Java 6) ##
  * Added the [ParallelParameterized](//michaeltamm.github.io/junit-toolbox/com/googlecode/junittoolbox/ParallelParameterized.html) runner contributed by Stefan Birkner.

## Version 2.0 ##
  * Upgraded to Java 8. Note: This does not mean, that there won't be any new version for Java 6. If a new feature or bug fix (which is compatible to Java 6) is added to the code base, I will release a new 1.x version as well as a new 2.x version.
  * Added overloaded `until` method to `PollingWait` which takes a `Callable<Boolean>` as parameter, which allows to use lambda expressions or method references. Example:
```
private PollingWait wait = new PollingWait().timeoutAfter(5, SECONDS)
                                            .pollEvery(100, MILLISECONDS);

@Test
public void test_login() throws Exception {
    // ... enter credentials into login form ...
    clickOnButton("Login");
    wait.until(() -> webDriver.findElement(By.linkText("Logout")).isDisplayed());
    // ...
}

protected void clickOnButton(String label) {
    WebElement button = findButton(label);
    wait.until(button::isDisplayed);
    button.click();
}
```

## Version 1.8 ##
  * Fixed bug in `MultithreadingTester` introduced in version 1.5

## Version 1.7 ##
  * Added annotations `@IncludeCategories` and `@ExcludeCategories`. In contrast to JUnit 4, which only offers the annotations `@IncludeCategory` and `@ExcludeCategory` which allow to specify a single category, these new annotations allow you to specify multiple categories. The annotations can be used with `WildcardPatternSuite` and with `ParallelSuite`. Example:
```
@RunWith(WildcardPatternSuite.class)
@SuiteClasses("**/*Test.class")
@ExcludeCategories({SlowTests.class, FlakyTests.class})
public class NormalTests {}
```

## Version 1.6 ##
  * Minor bugfixes in `PollingWait` class

## Version 1.5 ##
  * Added deadlock detection to `MultithreadingTester`

## Version 1.4 ##
  * Added new utility class `MultithreadingTester`
  * The `SuiteClasses` annotation accepts multiple wildcard patterns now as well as negated patterns. E.g.
```
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class", "!samples/**"})
public class AllTests {}
```

## Version 1.3 ##

  * Added `PollingWait` and `RunnableAssert` for waiting that an asynchronous operation succeeds. Unlike the `WebDriverWait` class provided by [Selenium](https://selenium.googlecode.com) this class does not wait for an artificial condition, which might be wrong and can make your test non-deterministic, this class waits until your assertions become true (or a configurable timeout is reached). Example:
```
private PollingWait wait = new PollingWait().timeoutAfter(5, SECONDS).pollEvery(100, MILLISECONDS);

@Test
public void test_auto_complete() throws Exception {
    // Enter "cheese" into auto complete field ...
    ...
    wait.until(new RunnableAssert("'cheesecake' is displayed in auto-complete <div>") { @Override public void run() throws Exception {
        WebElement autoCompleteDiv = driver.findElement(By.id("auto-complete"));
        assertThat(autoCompleteDiv, isVisible());
        assertThat(autoCompleteDiv, containsText("cheesecake"));
    }});
}
```

## Version 1.2 ##

  * `ParallelRunner` extends the `Theories` runner provided by JUnit now, and can be used as a replacement for it. It still executes all normal `@Test` methods concurrently. Furthermore it executes the calls to `@Theory` methods with different parameter assignments concurrently too.

## Version 1.1 ##
  * The `WildcardPatternSuite` runner supports the annotations `@IncludeCategory` and `@ExcludeCategory` now and can therefore be used as a replacement for the `Categories` runner provided by JUnit. Example:
```
@RunWith(WildcardPatternSuite.class)
@SuiteClasses("**/*Test.class")
@IncludeCategory(SlowTests.class)
public class OnlySlowTests {}
```
  * New runner `InnerTestClassesSuite` which runs all inner test classes of the class annotated with `@RunWith(InnerTestClassesSuite&#46;class)`. In contrast to the `Enclosed` runner provided by JUnit, it detects if an inner class is actually a test class and ignores all other inner classes. Example:
```
@RunWith(InnerTestClassesSuite.class)
public class LoginBeanTests {

    public static class UnitTests {
        @Test
        public void test1() { ... }
    }

    @Configuration
    public static class IntegrationTestsConfig { ... }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = IntegrationTestsConfig.class)
    public static class IntegrationTests {
        @Test
        public void test2() { ... }
    }
}
```

## Version 1.0 ##
  * bundled with [JUnit](https://github.com/KentBeck/junit) 4.10, [Hamcrest](https://hamcrest.googlecode.com/) 1.3, and [Mockito](https://mockito.googlecode.com/) 1.9.5
  * `WildcardPatternSuite` runner for specifying the children classes of a test suite with a [wildcard pattern](http://ant.apache.org/manual/dirtasks.html#patterns) like this:
```
@RunWith(WildcardPatternSuite.class)
@SuiteClasses("**/*Test.class")
public class AllTests {}
```
  * `ParallelSuite` for concurrent execution of test classes. You can either list the test classes, if you use the `@SuiteClasses` annotation provided by JUnit itself, for example:
```
@RunWith(ParallelSuite.class)
@SuiteClasses({
    LoginFrontendTest.class,
    FillOutFormFrontendTest.class,
    ...
})
public class AllFrontendTests {}
```
> or you can use a [wildcard pattern](http://ant.apache.org/manual/dirtasks.html#patterns) if you use the `@SuiteClasses` annotation of JUnit Toolbox:
```
@RunWith(ParallelSuite.class)
@SuiteClasses("**/*FrontendTest.class")
public class AllFrontendTests {}
```
  * `ParallelRunner` for concurrent execution of the test methods in a test class. Example:
```
@RunWith(ParallelRunner.class)
public class FooTest {
    @Test
    public void test1() {
        // Will be executed in a worker thread
    }
    @Test
    public void test2() {
        // Will be executed concurrently in another worker thread
    }
}
```
  * `ParallelSuite` and `ParallelRunner` share a common Fork-Join-Pool (to be compatible with Java 6, the JSR-166y code is used). You can control the maximum number of worker threads by specifying the system property `maxParallelTestThreads`. If this system property is not set, there will be as many worker threads as the number of processors available to the JVM.
