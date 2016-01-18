/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.flint.common.event.internal.metadata;

import net.caseif.flint.metadata.persist.PersistentMetadata;

/**
 * Called when a {@link PersistableMetdata} object is mutated.
 */
public class PersistableMetadataMutateEvent {

    private final PersistentMetadata subject;

    /**
     * Constructs a new {@link PersistableMetadataMutateEvent}.
     *
     * @param subject The {@link PersistentMetadata} object which is mutated in
     *     this event
     */
    public PersistableMetadataMutateEvent(PersistentMetadata subject) {
        this.subject = subject;
    }

    /**
     * Returns the {@link PersistentMetadata} ojbect associated with this
     * event.
     *
     * @return The {@link PersistentMetadata} object associated with this event
     */
    public PersistentMetadata getMetadata() {
        return subject;
    }

}
