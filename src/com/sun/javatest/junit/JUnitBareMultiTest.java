/*
 * $Id$
 *
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javatest.junit;

import com.sun.javatest.Status;
import com.sun.javatest.lib.MultiStatus;
import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 *
 */
public class JUnitBareMultiTest extends JUnitMultiTest {

    protected TestCase testCaseClass;
    protected SortedMap<String, Method> tests;
    protected String testCases[] = null;
    protected List<String> excludeTestCases = new Vector<>();

    /**
     * Creates a new instance of JUnitBareMultiTest using specified ClassLoader
     */
    public JUnitBareMultiTest(ClassLoader loader) {
        super(loader);
    }

    /**
     * Entry point for direct execution, not used by the harness.
     * Note that the provided class name should be a subclass of TestCase.
     */
    public static void main(String... args) {
        String executeClass = System.getProperty("javaTestExecuteClass");
        JUnitMultiTest multiTest = new JUnitBareMultiTest(ClassLoader.getSystemClassLoader());
        multiTest.setup(executeClass);
        multiTest.run0(args);
    }

    /**
     * Common method for running the test, used by all entry points.
     * {@code setTestCaseClass()} should have been invoked before calling this.
     */
    @Override
    public Status run0(String... argv) {
        MultiStatus ms = new MultiStatus(log);

        getListAllJunitTestCases();
        if (tests == null) {
            return Status.failed("No test cases found in test.");
        }
        for (Map.Entry<String, Method> stringMethodEntry : tests.entrySet()) {
            Method method = stringMethodEntry.getValue();
            Status status = null;
            try {
                status = invokeTestCase(method);
            } catch (Throwable e) {
                printStackTrace(e);
                status = Status.failed("Error in test case: " + method.getName());
            }
            ms.add(method.getName(), status);
        }
        return ms.getStatus();
    }

    /**
     * Entry point for standalone mode.
     */
    @Override
    protected void setup(String executeClass) {
        TestCase test;
        try {
            Class<? extends TestCase> tc = getClassLoader().loadClass(executeClass).asSubclass(TestCase.class);
            String name = tc.getName();
            String constructor = tc.getConstructors()[0].toGenericString();
            test = constructor.contains("java.lang.String")
                    ? (TestCase) tc.getConstructors()[0].newInstance(name)
                    : tc.getDeclaredConstructor().newInstance();
            setTestCaseClass(test);

        } catch (NoSuchMethodException | InstantiationException e) {
            log.println("Cannot instantiate test: " + executeClass + " (" + exceptionToString(e) + ")");
        } catch (InvocationTargetException e) {
            log.println("Exception in constructor: " + executeClass + " (" + exceptionToString(e.getTargetException()) + ")");
        } catch (IllegalAccessException e) {
            log.println("Cannot access test: " + executeClass + " (" + exceptionToString(e) + ")");
        } catch (ClassNotFoundException e) {
            log.println("Cannot find test: " + executeClass + " (" + exceptionToString(e) + ")");
        }
    }

    protected void setTestCaseClass(TestCase test) {
        testCaseClass = test;
    }

    @Override
    protected Status invokeTestCase(Method m) {
        try {
            testCaseClass.setName(m.getName());
            testCaseClass.runBare();
        } catch (Throwable e) {

            e.printStackTrace(log);
            return Status.failed("test case " + m.getName() + "in test " + testCaseClass.getName() + " failed: " + e);
        }
        return Status.passed("OKAY");
    }

    protected void getListAllJunitTestCases() {
        tests = new TreeMap<>();
        try {
            Method[] methods = AccessController.doPrivileged(
                    new PrivilegedAction<Method[]>() {
                        @Override
                        public Method[] run() {
                            return testCaseClass.getClass().getMethods();
                        }
                    });
            for (Method m : methods) {
                if (m == null || excludeTestCases.contains(m.getName())) {
                    continue;
                }
                Class<?>[] paramTypes = m.getParameterTypes();
                Class<?> returnType = m.getReturnType();
                String name = m.getName();
                if ((paramTypes.length == 0) &&
                        Void.TYPE.isAssignableFrom(returnType) && name.startsWith("test")) {
                    tests.put(name, m);
                }
            }
        } catch (Throwable e) {
            tests = null;
        }
    }
}
