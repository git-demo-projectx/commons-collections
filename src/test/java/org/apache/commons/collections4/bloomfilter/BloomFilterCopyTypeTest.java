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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the compile-time contract of {@link BloomFilter#copy()}. */
class BloomFilterCopyTypeTest {

    @TempDir
    Path outputDirectory;

    private void assertCompiles(final boolean expected, final String body) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        final String source = "import org.apache.commons.collections4.bloomfilter.*;"
                + "class CopyTypeCheck { void check() {"
                + "Shape shape = Shape.fromNP(10000, 0.01);" + body + "} }";
        final JavaFileObject sourceFile = new SimpleJavaFileObject(URI.create("string:///CopyTypeCheck.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final boolean compiled = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("--release", "8", "-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", outputDirectory.toString()), null, Collections.singletonList(sourceFile)).call();
            assertEquals(expected, compiled, diagnostics.getDiagnostics().toString());
        }
    }

    @Test
    void testConcreteCopiesCompile() throws IOException {
        assertCompiles(true,
                "SimpleBloomFilter simple = new SimpleBloomFilter(shape).copy();"
                + "SparseBloomFilter sparse = new SparseBloomFilter(shape).copy();"
                + "ArrayCountingBloomFilter counting = new ArrayCountingBloomFilter(shape).copy();"
                + "BloomFilter<SimpleBloomFilter> filter = simple; SimpleBloomFilter copy = filter.copy();"
                + "CountingBloomFilter<ArrayCountingBloomFilter> counter = counting;"
                + "ArrayCountingBloomFilter counterCopy = counter.copy();"
                + "LayerManager<SimpleBloomFilter> manager = LayerManager.<SimpleBloomFilter>builder()"
                + ".setSupplier(() -> new SimpleBloomFilter(shape)).get();"
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layers = new LayeredBloomFilter<>(shape, manager);"
                + "LayeredBloomFilter<SimpleBloomFilter> layerCopy = layers.copy();"
                + "class Decorator extends WrappedBloomFilter<Decorator, SimpleBloomFilter> {"
                + "Decorator(SimpleBloomFilter wrapped) { super(wrapped); }"
                + "public Decorator copy() { return new Decorator(getWrapped().copy()); } }"
                + "BloomFilter<Decorator> decorator = new Decorator(simple); Decorator decoratorCopy = decorator.copy();");
    }

    @Test
    void testWrongConcreteCopyDoesNotCompile() throws IOException {
        assertCompiles(false, "SparseBloomFilter filter = new SimpleBloomFilter(shape).copy();");
    }

    @Test
    void testWrongInterfaceCopyDoesNotCompile() throws IOException {
        assertCompiles(false, "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape);"
                + "SparseBloomFilter copy = filter.copy();");
    }

    @Test
    void testWrongCountingCopyDoesNotCompile() throws IOException {
        assertCompiles(false, "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape);"
                + "SimpleBloomFilter copy = filter.copy();");
    }
}
