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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BloomFilterExtractor}.
 */
public class BloomFilterExtractorTest {

    @Test
    void testDefaultArrayCopiesMixedFilters() {
        final Shape shape = Shape.fromKM(17, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilterExtractor extractor = predicate -> predicate.test(simple) && predicate.test(sparse);

        final BloomFilter<?>[] copies = extractor.asBloomFilterArray();
        assertEquals(2, copies.length);
        assertEquals(SimpleBloomFilter.class, copies[0].getClass());
        assertEquals(SparseBloomFilter.class, copies[1].getClass());
        assertNotSame(simple, copies[0]);
        assertNotSame(sparse, copies[1]);
        copies[0].clear();
        copies[1].clear();
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());

        final SimpleBloomFilter flattened = extractor.flatten();
        assertEquals(shape, flattened.getShape());
        assertEquals(3, flattened.cardinality());
        assertTrue(flattened.contains(simple));
        assertTrue(flattened.contains(sparse));
        flattened.clear();
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());
    }

    @Test
    public void testFlattenEmpty() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray().flatten());
    }

    @Test
    public void testFromBloomFilterArray() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray((BloomFilter<?>[]) null));
    }

    @Test
    void testFromMixedBloomFilterArray() {
        final Shape shape = Shape.fromKM(17, 72);
        final BloomFilter<?>[] filters = {new SimpleBloomFilter(shape), new SparseBloomFilter(shape)};
        final BloomFilter<?>[] result = BloomFilterExtractor.fromBloomFilterArray(filters).asBloomFilterArray();
        assertNotSame(filters, result);
        assertEquals(filters.length, result.length);
        assertSame(filters[0], result[0]);
        assertSame(filters[1], result[1]);
    }

}
