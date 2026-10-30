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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DefaultBloomFilterExtractorTest extends AbstractBloomFilterExtractorTest {

    @Override
    protected BloomFilterExtractor createUnderTest(final BloomFilter<?>... filters) {
        return bloomFilterPredicate -> {
            for (final BloomFilter<?> bf : filters) {
                if (!bloomFilterPredicate.test(bf)) {
                    return false;
                }
            }
            return true;
        };
    }

    @Test
    void testMixedFilterCopies() {
        final SimpleBloomFilter simple = new SimpleBloomFilter(getTestShape());
        final SparseBloomFilter sparse = new SparseBloomFilter(getTestShape());
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2));
        final BloomFilterExtractor extractor = createUnderTest(simple, sparse);
        final BloomFilter<?>[] copies = extractor.asBloomFilterArray();
        assertEquals(2, copies.length);
        assertEquals(SimpleBloomFilter.class, copies[0].getClass());
        assertEquals(SparseBloomFilter.class, copies[1].getClass());
        assertNotSame(simple, copies[0]);
        assertNotSame(sparse, copies[1]);

        final SimpleBloomFilter flattened = extractor.flatten();
        simple.clear();
        sparse.clear();
        assertTrue(copies[0].contains(IndexExtractor.fromIndexArray(1)));
        assertTrue(copies[1].contains(IndexExtractor.fromIndexArray(2)));
        assertTrue(flattened.contains(IndexExtractor.fromIndexArray(1, 2)));
    }
}
