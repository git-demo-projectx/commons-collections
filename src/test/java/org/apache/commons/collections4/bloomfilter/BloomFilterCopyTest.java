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

    private boolean compiles(final String statements) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyCheck.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*;"
                        + "class CopyCheck { void check() { Shape shape = Shape.fromNP(10000, 0.01);"
                        + statements + "} }";
            }
        };
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
            return compiler.getTask(null, files, diagnostics,
                    Arrays.asList("--release", "8", "-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", output.toString()), null, Collections.singletonList(source)).call();
        }
    }

    @Test
    void testConcreteCopiesCompile() throws IOException {
        assertTrue(compiles("SimpleBloomFilter simple = new SimpleBloomFilter(shape).copy();"
                + "SparseBloomFilter sparse = new SparseBloomFilter(shape).copy();"
                + "ArrayCountingBloomFilter counting = new ArrayCountingBloomFilter(shape).copy();"
                + "LayerManager<SimpleBloomFilter> manager = LayerManager.<SimpleBloomFilter>builder()"
                + ".setSupplier(() -> simple).get();"
                + "LayeredBloomFilter<SimpleBloomFilter> layered = new LayeredBloomFilter<>(shape, manager).copy();"
                + "BloomFilter<SimpleBloomFilter> filter = simple; SimpleBloomFilter copy = filter.copy();"
                + "CountingBloomFilter<ArrayCountingBloomFilter> counts = counting;"
                + "ArrayCountingBloomFilter countsCopy = counts.copy();"
                + "class Decorator extends WrappedBloomFilter<Decorator> {"
                + "Decorator(BloomFilter<?> wrapped) { super(wrapped); }"
                + "public Decorator copy() { return new Decorator(getWrapped().copy()); } }"
                + "WrappedBloomFilter<Decorator> wrapped = new Decorator(simple); Decorator wrappedCopy = wrapped.copy();"
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layers = layered;"
                + "LayeredBloomFilter<SimpleBloomFilter> layersCopy = layers.copy();"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter wrong = new SimpleBloomFilter(shape).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter wrong = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape);"
                + " SimpleBloomFilter wrong = filter.copy();"
    })
    void testWrongTypeCopiesDoNotCompile(final String statements) throws IOException {
        assertFalse(compiles(statements));
    }
}
