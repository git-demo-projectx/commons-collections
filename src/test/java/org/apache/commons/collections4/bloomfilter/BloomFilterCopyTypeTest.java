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

class BloomFilterCopyTypeTest {

    @TempDir
    Path outputDirectory;

    private void assertCompiles(final boolean expected, final String body) throws Exception {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A JDK is required to test copy typing");
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final String source = "import org.apache.commons.collections4.bloomfilter.*; "
                + "class CopyTyping { void test() { " + body + " } }";
        final JavaFileObject sourceFile = new SimpleJavaFileObject(URI.create("string:///CopyTyping.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final boolean compiled = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", outputDirectory.toString()),
                    null, Collections.singletonList(sourceFile)).call();
            assertEquals(expected, compiled, diagnostics.getDiagnostics().toString());
        }
    }

    @Test
    void testCopyThroughInterfaces() throws Exception {
        assertCompiles(true, "Shape shape = Shape.fromNP(10000, 0.01); "
                + "BloomFilter<SimpleBloomFilter> simple = new SimpleBloomFilter(shape); "
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
                + "Decorator(BloomFilter wrapped) { super(wrapped); } "
                + "public Decorator copy() { return new Decorator(getWrapped().copy()); } } "
                + "WrappedBloomFilter<Decorator> wrapped = new Decorator(simple); "
                + "Decorator wrappedCopy = wrapped.copy();");
    }

    @Test
    void testRejectsWrongConcreteType() throws Exception {
        assertCompiles(false, "SparseBloomFilter filter = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy();");
    }

    @Test
    void testRejectsWrongInterfaceType() throws Exception {
        assertCompiles(false, "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)); "
                + "SparseBloomFilter copy = filter.copy();");
    }
}
