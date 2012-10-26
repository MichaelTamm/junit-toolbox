package com.googlecode.junittoolbox.samples;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.googlecode.junittoolbox.WildcardPatternSuiteTest;
import org.junit.runner.RunWith;

/**
 * For {@link WildcardPatternSuiteTest}.
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses("**/*Test.class")
public class AllTests {}
