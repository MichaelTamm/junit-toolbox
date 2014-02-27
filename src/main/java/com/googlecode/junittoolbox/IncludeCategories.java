package com.googlecode.junittoolbox;

import org.junit.experimental.categories.Category;
import org.junit.experimental.categories.Categories.IncludeCategory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be used with the {@link WildcardPatternSuite}
 * and the {@link ParallelSuite} runner to include tests if they are
 * annotated with {@link Category @Category}. In contrast to the standard
 * JUnit {@link IncludeCategory @IncludeCategory} annotation
 * this annotation allows you to specify not only one but several classes.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IncludeCategories {
    public Class<?>[] value();
}
