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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BloomFilterExtractor}.
 */
public class BloomFilterExtractorTest {

    @Test
    public void testHeterogeneousArrayCopies() {
        final Shape shape = Shape.fromKM(3, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilterExtractor extractor = predicate -> predicate.test(simple) && predicate.test(sparse);
        final BloomFilterBase[] copies = extractor.asBloomFilterArray();
        assertEquals(2, copies.length);
        assertEquals(SimpleBloomFilter.class, copies[0].getClass());
        assertEquals(SparseBloomFilter.class, copies[1].getClass());
        assertNotSame(simple, copies[0]);
        assertNotSame(sparse, copies[1]);
        assertArrayEquals(simple.asBitMapArray(), copies[0].asBitMapArray());
        assertArrayEquals(sparse.asBitMapArray(), copies[1].asBitMapArray());
        copies[0].clear();
        copies[1].clear();
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());
    }

    @Test
    public void testHeterogeneousArrayReferences() {
        final Shape shape = Shape.fromKM(3, 72);
        final BloomFilter<?>[] filters = {new SimpleBloomFilter(shape), new SparseBloomFilter(shape)};
        final BloomFilterBase[] result = BloomFilterExtractor.fromBloomFilterArray(filters).asBloomFilterArray();
        assertNotSame(filters, result);
        assertSame(filters[0], result[0]);
        assertSame(filters[1], result[1]);
        result[0].merge(IndexExtractor.fromIndexArray(1));
        result[1].merge(IndexExtractor.fromIndexArray(2, 3));
        assertEquals(1, filters[0].cardinality());
        assertEquals(2, filters[1].cardinality());
    }

    @Test
    public void testFlattenEmpty() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray().flatten());
    }

    @Test
    public void testFromBloomFilterArray() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray((BloomFilter<?>[]) null));
    }

}
