package com.googlecode.junittoolbox.samples;

import com.googlecode.junittoolbox.ExcludeCategories;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.googlecode.junittoolbox.samples.frontend.LoginFrontendTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses(LoginFrontendTest.class)
@ExcludeCategories({SlowTests.class, FlakyTests.class})
public class NormalLoginTests {}
