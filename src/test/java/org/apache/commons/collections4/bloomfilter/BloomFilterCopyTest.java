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
import java.nio.file.Paths;
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

/** Tests compile-time constraints on Bloom filter copies. */
public class BloomFilterCopyTest {

    @TempDir
    Path output;

    private void assertCompilation(final boolean expected, final String statement) throws Exception {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK");
        final String source = "import org.apache.commons.collections4.bloomfilter.*;"
                + "class CopyCheck { void check() { Shape shape = Shape.fromNP(10000, 0.01);"
                + statement + "} }";
        final JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///CopyCheck.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
                return source;
            }
        };
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final String classPath = Paths.get(BloomFilter.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final boolean compiled = compiler.getTask(null, manager, diagnostics,
                    Arrays.asList("-classpath", classPath, "-d", output.toString(), "-proc:none"),
                    null, Collections.singleton(file)).call();
            assertEquals(expected, compiled, diagnostics.getDiagnostics().toString());
        }
    }

    @Test
    public void testSameTypeCopiesCompile() throws Exception {
        assertCompilation(true,
                "BloomFilter<SimpleBloomFilter> simple = new SimpleBloomFilter(shape);"
                + "SimpleBloomFilter simpleCopy = simple.copy();"
                + "BloomFilter<SparseBloomFilter> sparse = new SparseBloomFilter(shape);"
                + "SparseBloomFilter sparseCopy = sparse.copy();"
                + "CountingBloomFilter<ArrayCountingBloomFilter> counting = new ArrayCountingBloomFilter(shape);"
                + "ArrayCountingBloomFilter countingCopy = counting.copy();"
                + "LayerManager<SimpleBloomFilter> manager = LayerManager.<SimpleBloomFilter>builder()"
                + ".setSupplier(() -> new SimpleBloomFilter(shape)).get();"
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layered = new LayeredBloomFilter<>(shape, manager);"
                + "LayeredBloomFilter<SimpleBloomFilter> layeredCopy = layered.copy();"
                + "class Wrapper extends WrappedBloomFilter<Wrapper> {"
                + "Wrapper(BloomFilter<?> filter) { super(filter); }"
                + "public Wrapper copy() { return new Wrapper(getWrapped().copy()); } }"
                + "WrappedBloomFilter<Wrapper> wrapped = new Wrapper(simple);"
                + "Wrapper wrappedCopy = wrapped.copy();");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter filter = new SimpleBloomFilter(shape).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy();",
        "BloomFilter<?> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); SimpleBloomFilter copy = filter.copy();"
    })
    public void testDifferentTypeCopiesDoNotCompile(final String statement) throws Exception {
        assertCompilation(false, statement);
    }
}
