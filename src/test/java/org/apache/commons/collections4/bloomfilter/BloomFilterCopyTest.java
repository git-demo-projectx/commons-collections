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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BloomFilterCopyTest {
    @TempDir
    Path outputDirectory;

    private boolean compiles(final String assignment) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A JDK is required to verify copy typing");
        final String source = "import org.apache.commons.collections4.bloomfilter.*;"
                + "class CopyTyping { void copy(BloomFilter<SimpleBloomFilter> filter) {"
                + assignment + "; } }";
        final JavaFileObject sourceFile = new SimpleJavaFileObject(URI.create("string:///CopyTyping.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        return compiler.getTask(null, null, new DiagnosticCollector<>(),
                Arrays.asList("-proc:none", "-d", outputDirectory.toString()), null,
                Collections.singletonList(sourceFile)).call();
    }

    @Test
    void testCopyTyping() {
        assertTrue(compiles("SimpleBloomFilter copy = filter.copy()"));
        assertFalse(compiles("SparseBloomFilter copy = filter.copy()"));
        assertFalse(compiles("SparseBloomFilter copy = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy()"));
    }
}
