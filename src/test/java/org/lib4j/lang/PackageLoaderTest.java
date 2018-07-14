/* Copyright (c) 2006 lib4j
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.lib4j.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageLoaderTest {
  private static final Logger logger = LoggerFactory.getLogger(PackageLoaderTest.class);
  private static final Method findLoadedClass;

  static {
    try {
      findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
      findLoadedClass.setAccessible(true);
    }
    catch (final NoSuchMethodException | SecurityException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static boolean isClassLoaded(final ClassLoader classLoader, final String name) {
    if (classLoader == null)
      throw new IllegalArgumentException("classLoader == null");

    try {
      return findLoadedClass.invoke(classLoader, name) != null;
    }
    catch (final InvocationTargetException e) {
      throw new UnsupportedOperationException(e);
    }
    catch (final IllegalAccessException e) {
      throw new SecurityException(e);
    }
  }

  private static boolean isClassLoaded(final String name) {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    if (isClassLoaded(classLoader, name))
      return true;

//    classLoader = Thread.currentThread().getContextClassLoader();
//    if (ClassLoaders.isClassLoaded(classLoader, name))
//      return true;

//    final Class<?> callerClass = Reflection.getCallerClass();
//    classLoader = callerClass.getClassLoader();
//    if (ClassLoaders.isClassLoaded(classLoader, name))
//      return true;

    return false;
  }

  @Test
  public void testPackageLoader() throws PackageNotFoundException {
    final String[] testClasses = new String[] {
      "org.junit.PackageLoaderClass1",
      "org.junit.PackageLoaderClass2",
      "org.junit.PackageLoaderClass3",
      "org.junit.runners.Parameterized"
    };

    for (final String testClass : testClasses)
      Assert.assertFalse(testClass, isClassLoaded(testClass));

    final Set<Class<?>> loadedClasses = PackageLoader.getContextPackageLoader().loadPackage("org.junit");
    final Set<String> classNames = new HashSet<>();
    for (final Class<?> loadedClass : loadedClasses)
      classNames.add(loadedClass.getName());

    for (final String testClass : testClasses) {
      logger.debug(testClass);
      Assert.assertTrue(classNames.contains(testClass));
      Assert.assertTrue(isClassLoaded(testClass));
    }

    try {
      PackageLoader.getContextPackageLoader().loadPackage((String)null);
      Assert.fail("Expected a IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testJar() throws ClassNotFoundException, PackageNotFoundException {
    boolean[] encountered = new boolean[1];
    final Set<Class<?>> loadedClasses = PackageLoader.getContextPackageLoader().loadPackage("org.junit.runner", new Predicate<Class<?>>() {
      @Override
      public boolean test(final Class<?> t) {
        return encountered[0] = "org.junit.runner.FilterFactory".equals(t.getName()) || encountered[0];
      }
    });
    Assert.assertTrue("Should have been loaded by PackageLoader", loadedClasses.contains(Class.forName("org.junit.runner.FilterFactory", false, ClassLoader.getSystemClassLoader())));
    Assert.assertTrue(encountered[0]);
  }
}