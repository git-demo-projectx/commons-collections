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

/**
 * The operations shared by Bloom filters whose concrete copy types may differ.
 * Use {@link BloomFilter} when the type returned by {@link #copy()} is known.
 *
 * @since 4.5.0
 */
public interface BloomFilterState extends IndexExtractor, BitMapExtractor {

    /** @see BloomFilter#cardinality() */
    int cardinality();

    /** @see BloomFilter#characteristics() */
    int characteristics();

    /** @see BloomFilter#clear() */
    void clear();

    /** @see BloomFilter#contains(BitMapExtractor) */
    boolean contains(BitMapExtractor bitMapExtractor);

    /** @see BloomFilter#contains(BloomFilter) */
    boolean contains(BloomFilter<?> other);

    /** @see BloomFilter#contains(Hasher) */
    boolean contains(Hasher hasher);

    /** @see BloomFilter#contains(IndexExtractor) */
    boolean contains(IndexExtractor indexExtractor);

    /**
     * Creates an independent copy with the same properties as this filter.
     *
     * @return a copy of this filter
     */
    BloomFilterState copy();

    /** @see BloomFilter#estimateIntersection(BloomFilter) */
    int estimateIntersection(BloomFilter<?> other);

    /** @see BloomFilter#estimateN() */
    int estimateN();

    /** @see BloomFilter#estimateUnion(BloomFilter) */
    int estimateUnion(BloomFilter<?> other);

    /** @see BloomFilter#getShape() */
    Shape getShape();

    /** @see BloomFilter#isEmpty() */
    boolean isEmpty();

    /** @see BloomFilter#isFull() */
    boolean isFull();

    /** @see BloomFilter#merge(BitMapExtractor) */
    boolean merge(BitMapExtractor bitMapExtractor);

    /** @see BloomFilter#merge(BloomFilter) */
    boolean merge(BloomFilter<?> other);

    /** @see BloomFilter#merge(Hasher) */
    boolean merge(Hasher hasher);

    /** @see BloomFilter#merge(IndexExtractor) */
    boolean merge(IndexExtractor indexExtractor);
}
