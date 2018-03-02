package com.googlecode.junittoolbox.samples.suites;

import com.googlecode.junittoolbox.ParallelSuite;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.SuiteConfiguration;
import com.googlecode.junittoolbox.WildcardPatternSuiteTest;
import org.junit.runner.RunWith;

/**
 * For {@link WildcardPatternSuiteTest} -- tests that @SuiteClasses annotation
 * can handle a value starting with "../".
 */
@RunWith(ParallelSuite.class)
@SuiteClasses("../frontend/*SlowTest.class")
@SuiteConfiguration(parallel = "classes")
public class SlowTests {}
