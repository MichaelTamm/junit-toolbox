package com.googlecode.junittoolbox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SuiteConfiguration {

	String parallel() default "methods";
}
