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
    public void testAsBloomFilterArrayMixedTypes() {
        final Shape shape = Shape.fromKM(2, 64);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilter<?>[] originals = {simple, sparse};
        final BloomFilterExtractor extractor = BloomFilterExtractor.fromBloomFilterArray(originals);
        final BloomFilter<?>[] references = extractor.asBloomFilterArray();
        assertNotSame(originals, references);
        assertSame(simple, references[0]);
        assertSame(sparse, references[1]);

        final BloomFilterExtractor copyingExtractor = extractor::processBloomFilters;
        final BloomFilter<?>[] copies = copyingExtractor.asBloomFilterArray();
        assertEquals(2, copies.length);
        assertEquals(SimpleBloomFilter.class, copies[0].getClass());
        assertEquals(SparseBloomFilter.class, copies[1].getClass());
        for (int i = 0; i < copies.length; i++) {
            assertNotSame(originals[i], copies[i]);
            assertArrayEquals(originals[i].asBitMapArray(), copies[i].asBitMapArray());
            copies[i].clear();
            assertEquals(i + 1, originals[i].cardinality());
        }
        final SimpleBloomFilter flattened = copyingExtractor.flatten();
        assertArrayEquals(new int[] {1, 2, 3}, flattened.asIndexArray());
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
