/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.bloomfilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests that a copy cannot be assigned to an unrelated filter type. */
public class BloomFilterCopyTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter copy = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(Shape.fromNP(10000, 0.01));"
            + " SparseBloomFilter copy = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(Shape.fromNP(10000, 0.01));"
            + " SparseBloomFilter copy = filter.copy();"
    })
    public void testRejectUnrelatedCopyType(final String statement) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "This test requires a JDK");
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final String source = "import org.apache.commons.collections4.bloomfilter.*;"
                + "class CopyTypeCheck { void check() { " + statement + " } }";
        final JavaFileObject sourceFile = new SimpleJavaFileObject(
                URI.create("string:///CopyTypeCheck.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        final JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics,
                Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path")),
                null, Collections.singletonList(sourceFile));
        assertFalse(task.call(), "An unrelated copy type must not compile");
        assertTrue(diagnostics.getDiagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.getCode().equals("compiler.err.prob.found.req")),
                diagnostics.getDiagnostics().toString());
    }
}
