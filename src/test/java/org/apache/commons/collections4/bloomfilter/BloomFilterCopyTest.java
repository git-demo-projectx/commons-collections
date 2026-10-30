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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BloomFilterCopyTest {

    @TempDir
    Path output;

    private boolean compiles(final String body, final DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyCheck.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*; class CopyCheck { " + body + " }";
            }
        };
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            return compiler.getTask(null, manager, diagnostics,
                    Arrays.asList("-proc:none", "-d", output.toString(), "-classpath", System.getProperty("java.class.path")),
                    null, Collections.singletonList(source)).call();
        }
    }

    @Test
    void testCopiesRetainTheirTypes() throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertTrue(compiles("void copy(BloomFilter<SimpleBloomFilter> simple, BloomFilter<SparseBloomFilter> sparse, "
                + "CountingBloomFilter<ArrayCountingBloomFilter> counting, "
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layered) { "
                + "SimpleBloomFilter a = simple.copy(); SparseBloomFilter b = sparse.copy(); "
                + "ArrayCountingBloomFilter c = counting.copy(); LayeredBloomFilter<SimpleBloomFilter> d = layered.copy(); } "
                + "<T extends WrappedBloomFilter<T>> T copy(WrappedBloomFilter<T> wrapped) { return wrapped.copy(); }",
                diagnostics), () -> diagnostics.getDiagnostics().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "void copy() { SparseBloomFilter filter = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy(); }",
        "SparseBloomFilter copy(BloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(BloomFilter<SparseBloomFilter> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(CountingBloomFilter<ArrayCountingBloomFilter> filter) { return filter.copy(); }",
        "LayeredBloomFilter<SparseBloomFilter> copy(LayeredBloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }",
        "<T extends WrappedBloomFilter<T>> SimpleBloomFilter copy(WrappedBloomFilter<T> filter) { return filter.copy(); }"
    })
    void testCopiesCannotBeAssignedToUnrelatedTypes(final String body) throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertFalse(compiles(body, diagnostics), () -> diagnostics.getDiagnostics().toString());
        assertTrue(diagnostics.getDiagnostics().stream().anyMatch(d -> d.getCode().startsWith("compiler.err.prob.found.req")),
                () -> diagnostics.getDiagnostics().toString());
    }
}
