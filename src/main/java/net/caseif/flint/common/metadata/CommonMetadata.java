package net.caseif.flint.common.metadata;

import net.caseif.flint.common.event.internal.metadata.MetadataMutateEvent;
import net.caseif.flint.metadata.Metadata;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Metadata}.
 *
 * @author Max Roncacé
 */
public class CommonMetadata implements Metadata {

    private final Map<String, Object> data = new HashMap<>();

    private static final EventBus EVENT_BUS = new EventBus();

    protected CommonMetadata() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) throws ClassCastException {
        return Optional.fromNullable((T)data.get(key));
    }

    @Override
    public <T> void set(String key, T value) {
        data.put(key, value);
        getEventBus().post(new MetadataMutateEvent(this));
    }

    @Override
    public Metadata createStructure(String key) throws IllegalArgumentException {
        Preconditions.checkArgument(!data.containsKey(key), "Metadata key " + key + " is already set");
        Metadata structure = new CommonMetadata();
        data.put(key, structure);
        getEventBus().post(new MetadataMutateEvent(this));
        return structure;
    }

    @Override
    public boolean remove(String key) {
        Object result = data.remove(key);
        getEventBus().post(new MetadataMutateEvent(this));
        return result != null;
    }

    @Override
    public ImmutableSet<String> getAllKeys() {
        return ImmutableSet.copyOf(data.keySet());
    }

    @Override
    public void clear() {
        data.clear();
        getEventBus().post(new MetadataMutateEvent(this));
    }

    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

}
