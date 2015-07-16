package net.caseif.flint.common.event.internal.metadata;

import net.caseif.flint.metadata.Metadata;

/**
 * Called when a {@link Metdata} object is mutated.
 */
public class MetadataMutateEvent {

    private Metadata subject;

    /**
     * Constructs a new {@link MetadataMutateEvent}.
     *
     * @param subject The {@link Metadata} object which is mutated in this event
     */
    public MetadataMutateEvent(Metadata subject) {
        this.subject = subject;
    }

    /**
     * Returns the {@link Metadata} ojbect associated with this event.
     *
     * @return The {@link Metadata} object associated with this event
     */
    public Metadata getMetadata() {
        return subject;
    }

}
