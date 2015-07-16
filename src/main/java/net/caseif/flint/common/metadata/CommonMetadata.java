package net.caseif.flint.common.metadata;

import net.caseif.flint.metadata.Metadata;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Metadata}.
 *
 * @author Max Roncacé
 */
public class CommonMetadata implements Metadata {

    private final Map<String, Object> data = new HashMap<>();

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
    }

    @Override
    public Metadata createStructure(String key) throws IllegalArgumentException {
        Preconditions.checkArgument(!data.containsKey(key), "Metadata key " + key + " is already set");
        Metadata structure = new CommonMetadata();
        data.put(key, structure);
        return structure;
    }

    @Override
    public boolean remove(String key) {
        return data.remove(key) != null;
    }

    @Override
    public ImmutableSet<String> getAllKeys() {
        return ImmutableSet.copyOf(data.keySet());
    }

}
