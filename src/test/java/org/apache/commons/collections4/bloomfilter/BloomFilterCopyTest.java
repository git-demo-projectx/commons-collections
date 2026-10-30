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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BloomFilterCopyTest {

    @TempDir
    Path outputDirectory;

    private void assertCompiles(final String method, final boolean expected) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A JDK is required to check copy return types");
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyUsage.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*; class CopyUsage { " + method + " }";
            }
        };
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final Boolean result = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("--release", "8", "-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", outputDirectory.toString()), null, Collections.singletonList(source)).call();
            assertEquals(expected, result, () -> diagnostics.getDiagnostics().toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SimpleBloomFilter copy(BloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }",
        "SparseBloomFilter copy(BloomFilter<SparseBloomFilter> filter) { return filter.copy(); }",
        "ArrayCountingBloomFilter copy(CountingBloomFilter<ArrayCountingBloomFilter> filter) { return filter.copy(); }",
        "<T extends BloomFilter<T>> T copy(T filter) { return filter.copy(); }",
        "<T extends CountingBloomFilter<T>> T copy(T filter) { return filter.copy(); }",
        "<T extends WrappedBloomFilter<T>> T copy(T filter) { return filter.copy(); }",
        "<T extends BloomFilter<T>> LayeredBloomFilter<T> copy(LayeredBloomFilter<T> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(BloomFilterExtractor filters) { return filters.flatten().copy(); }",
        "static class Decorator extends WrappedBloomFilter<Decorator> { "
                + "Decorator(BloomFilter<?> filter) { super(filter); } "
                + "public Decorator copy() { return new Decorator(getWrapped().copy()); } }"
    })
    void testCopyReturnsFilterType(final String method) throws IOException {
        assertCompiles(method, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter copy() { return new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy(); }",
        "SparseBloomFilter copy(BloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(BloomFilter<SparseBloomFilter> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(CountingBloomFilter<ArrayCountingBloomFilter> filter) { return filter.copy(); }",
        "SimpleBloomFilter copy(BloomFilter<?> filter) { return filter.copy(); }",
        "<T extends WrappedBloomFilter<T>> SimpleBloomFilter copy(T filter) { return filter.copy(); }",
        "<T extends BloomFilter<T>> T copy(LayeredBloomFilter<T> filter) { return filter.copy(); }"
    })
    void testCopyRejectsUnrelatedType(final String method) throws IOException {
        assertCompiles(method, false);
    }
}
