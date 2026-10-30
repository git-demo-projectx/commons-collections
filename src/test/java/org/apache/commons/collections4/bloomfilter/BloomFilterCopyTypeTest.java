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
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests the compile-time contract of {@link BloomFilter#copy()}. */
public class BloomFilterCopyTypeTest {

    @TempDir
    Path outputDirectory;

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "SimpleBloomFilter copy = new SimpleBloomFilter(shape).copy(); | true",
        "SparseBloomFilter copy = new SparseBloomFilter(shape).copy(); | true",
        "ArrayCountingBloomFilter copy = new ArrayCountingBloomFilter(shape).copy(); | true",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SimpleBloomFilter copy = filter.copy(); | true",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); ArrayCountingBloomFilter copy = filter.copy(); | true",
        "LayeredBloomFilter<SimpleBloomFilter> filter = LayeredBloomFilterTest.fixed(shape, 2); LayeredBloomFilter<SimpleBloomFilter> copy = filter.copy(); | true",
        "WrappedBloomFilter<WrappedBloomFilterTest.Fixture, DefaultBloomFilterTest.AbstractDefaultBloomFilter> filter = new WrappedBloomFilterTest.Fixture(new DefaultBloomFilterTest.SparseDefaultBloomFilter(shape)); WrappedBloomFilterTest.Fixture copy = filter.copy(); | true",
        "SparseBloomFilter copy = new SimpleBloomFilter(shape).copy(); | false",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy(); | false",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); SimpleBloomFilter copy = filter.copy(); | false",
        "LayeredBloomFilter<SimpleBloomFilter> filter = LayeredBloomFilterTest.fixed(shape, 2); LayeredBloomFilter<SparseBloomFilter> copy = filter.copy(); | false"
    })
    public void testCopyAssignment(final String statement, final boolean expected) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "These tests require a JDK");
        final String source = "package org.apache.commons.collections4.bloomfilter; "
                + "class CopyAssignment { void test() { Shape shape = Shape.fromNP(10000, 0.01); "
                + statement + " } }";
        final JavaFileObject sourceFile = new SimpleJavaFileObject(URI.create("string:///CopyAssignment.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        final StringWriter diagnostics = new StringWriter();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            final JavaCompiler.CompilationTask task = compiler.getTask(diagnostics, fileManager, null,
                    Arrays.asList("-classpath", System.getProperty("java.class.path"), "-d", outputDirectory.toString()),
                    null, Collections.singletonList(sourceFile));
            assertEquals(expected, task.call(), diagnostics::toString);
        }
    }
}
