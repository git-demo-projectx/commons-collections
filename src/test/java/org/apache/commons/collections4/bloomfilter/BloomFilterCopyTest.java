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

    private boolean compiles(final String statements, final DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
        final String source = "import org.apache.commons.collections4.bloomfilter.*; "
                + "class CopyTypes { void check() { Shape shape = Shape.fromNP(10000, 0.01); " + statements + " } }";
        final JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///CopyTypes.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            return compiler.getTask(null, manager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path"), "-d", output.toString()),
                    null, Collections.singletonList(file)).call();
        }
    }

    @Test
    void testCopyPreservesType() throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertTrue(compiles("BloomFilter<SimpleBloomFilter> simple = new SimpleBloomFilter(shape); "
                + "SimpleBloomFilter simpleCopy = simple.copy(); "
                + "BloomFilter<SparseBloomFilter> sparse = new SparseBloomFilter(shape); "
                + "SparseBloomFilter sparseCopy = sparse.copy(); "
                + "CountingBloomFilter<ArrayCountingBloomFilter> counting = new ArrayCountingBloomFilter(shape); "
                + "ArrayCountingBloomFilter countingCopy = counting.copy(); "
                + "LayerManager<SimpleBloomFilter> manager = LayerManager.<SimpleBloomFilter>builder()"
                + ".setSupplier(() -> new SimpleBloomFilter(shape)).get(); "
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layered = new LayeredBloomFilter<>(shape, manager); "
                + "LayeredBloomFilter<SimpleBloomFilter> layeredCopy = layered.copy(); "
                + "class Decorator extends WrappedBloomFilter<Decorator> { "
                + "Decorator(BloomFilter<?> filter) { super(filter); } "
                + "public Decorator copy() { return new Decorator(getWrapped(BloomFilter::copy)); } } "
                + "WrappedBloomFilter<Decorator> wrapped = new Decorator(simple); Decorator wrappedCopy = wrapped.copy();",
                diagnostics), () -> diagnostics.getDiagnostics().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter copy = new SimpleBloomFilter(shape).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy();",
        "BloomFilter<SparseBloomFilter> filter = new SparseBloomFilter(shape); SimpleBloomFilter copy = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); SimpleBloomFilter copy = filter.copy();"
    })
    void testCopyRejectsUnrelatedType(final String statements) throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertFalse(compiles(statements, diagnostics), "An unrelated copy type must not compile");
    }
}
