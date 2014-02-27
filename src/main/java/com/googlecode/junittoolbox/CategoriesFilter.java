package com.googlecode.junittoolbox;

import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.experimental.categories.Category;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class CategoriesFilter extends Filter {

    static CategoriesFilter forTestSuite(Class<?> testSuiteClass) {
        List<Class<?>> includedCategories = new ArrayList<Class<?>>();
        List<Class<?>> excludedCategories = new ArrayList<Class<?>>();
        IncludeCategory includeCategoryAnnotation= testSuiteClass.getAnnotation(IncludeCategory.class);
        if (includeCategoryAnnotation != null) { includedCategories.add(includeCategoryAnnotation.value()); }
        IncludeCategories includeCategoriesAnnotation= testSuiteClass.getAnnotation(IncludeCategories.class);
        if (includeCategoriesAnnotation != null) { includedCategories.addAll(Arrays.asList(includeCategoriesAnnotation.value())); }
        ExcludeCategory excludeCategoryAnnotation= testSuiteClass.getAnnotation(ExcludeCategory.class);
        if (excludeCategoryAnnotation != null) { excludedCategories.add(excludeCategoryAnnotation.value()); }
        ExcludeCategories excludeCategoriesAnnotation= testSuiteClass.getAnnotation(ExcludeCategories.class);
        if (excludeCategoriesAnnotation != null) { excludedCategories.addAll(Arrays.asList(excludeCategoriesAnnotation.value())); }
        return (includedCategories.isEmpty() && excludedCategories.isEmpty() ? null : new CategoriesFilter(includedCategories, excludedCategories));
    }

    private final List<Class<?>> includedCategories = new ArrayList<Class<?>>();
    private final List<Class<?>> excludedCategories = new ArrayList<Class<?>>();

    private CategoriesFilter(Collection<Class<?>> includedCategories, Collection<Class<?>> excludedCategories) {
        this.includedCategories.addAll(includedCategories);
        this.excludedCategories.addAll(excludedCategories);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        String separator = "include categories: ";
        for (Class<?> categoryClass : includedCategories) {
            sb.append(separator);
            sb.append(categoryClass.getSimpleName());
            separator = ", ";
        }
        separator = (sb.length() == 0 ? "" : "; ") + "exclude categories: ";
        for (Class<?> categoryClass : excludedCategories) {
            sb.append(separator);
            sb.append(categoryClass.getSimpleName());
            separator = ", ";
        }
        return sb.toString();
    }

    @Override
    public boolean shouldRun(Description description) {
        if (hasCorrectCategoryAnnotation(description)) {
            return true;
        }
        for (Description childDescription : description.getChildren()) {
            if (shouldRun(childDescription)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCorrectCategoryAnnotation(Description description) {
        List<Class<?>> categories = categories(description);
        if (categories.isEmpty()) {
            // Test has no @Category annotation, is match if no included categories have been specified ...
            return includedCategories.isEmpty();
        }
        if (!excludedCategories.isEmpty()) {
            for (Class<?> category : categories) {
                for (Class<?> excludedCategory : excludedCategories) {
                    if (excludedCategory.isAssignableFrom(category)) {
                        // Test has an excluded category ...
                        return false;
                    }
                }
            }
        }
        if (includedCategories.isEmpty()) {
            // Test is not excluded and no included categories have been specified ...
            return true;
        }
        for (Class<?> category : categories) {
            for (Class<?> includedCategory : includedCategories) {
                if (includedCategory.isAssignableFrom(category)) {
                    // Test has an included category ...
                    return true;
                }
            }
        }
        // Test has no included category ...
        return false;
    }

    private List<Class<?>> categories(Description description) {
        ArrayList<Class<?>> categories = new ArrayList<Class<?>>();
        categories.addAll(Arrays.asList(directCategories(description)));
        categories.addAll(Arrays.asList(directCategories(parentDescription(description))));
        return categories;
    }

    private Description parentDescription(Description description) {
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            return null;
        }
        return Description.createSuiteDescription(testClass);
    }

    private Class<?>[] directCategories(Description description) {
        if (description == null) {
            return new Class<?>[0];
        }
        Category annotation = description.getAnnotation(Category.class);
        if (annotation == null) {
            return new Class<?>[0];
        }
        return annotation.value();
    }
}