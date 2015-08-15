/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.caseif.flint.common.minigame;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.event.FlintSubscriberExceptionHandler;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implements {@link Minigame}.
 *
 * @author Max Roncacé
 */
public abstract class CommonMinigame implements Minigame {

    private final EventBus eventBus;

    private final Map<ConfigNode<?>, Object> config = new HashMap<>();
    private final BiMap<String, Arena> arenas = HashBiMap.create();
    private final BiMap<Arena, Round> rounds = HashBiMap.create(); // guarantees values aren't duplicated

    protected CommonMinigame() {
        EventBus bus;
        try {
            bus = new EventBus(FlintSubscriberExceptionHandler.getInstance());
        } catch (NoClassDefFoundError ex) { // Guava version < 16.0
            CommonCore.logWarning("Guava version is < 16.0 - SubscriberExceptionHandler is not supported. "
                    + "Exceptions occurring in Flint event handlers may not be logged correctly.");
            bus = new EventBus();
        }
        eventBus = bus;
    }

    @Override
    @SuppressWarnings("unchecked") // only mutable through setConfigValue(), which guarantees types match
    public <T> T getConfigValue(ConfigNode<T> node) {
        return config.containsKey(node) ? (T) config.get(node) : node.getDefaultValue();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public <T> void setConfigValue(ConfigNode<T> node, T value) {
        config.put(node, value);
    }

    @Override
    public ImmutableList<Arena> getArenas() {
        return ImmutableList.copyOf(arenas.values());
    }

    @Override
    public Optional<Arena> getArena(String arenaName) {
        return Optional.fromNullable(arenas.get(arenaName.toLowerCase()));
    }

    @Override
    public ImmutableList<Round> getRounds() {
        return ImmutableList.copyOf(rounds.values());
    }

    @Override
    public ImmutableList<Challenger> getChallengers() {
        ImmutableList.Builder<Challenger> builder = ImmutableList.builder();
        for (Round r : getRounds()) { // >tfw no streams
            builder.addAll(r.getChallengers());
        }
        return builder.build();
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) {
        for (Round r : getRounds()) {
            if (r.getChallenger(uuid).isPresent()) {
                return r.getChallenger(uuid);
            }
        }
        return Optional.absent();
    }

    // everything below this line are internal utility methods

    public Map<ConfigNode<?>, Object> getConfigMap() {
        return config;
    }

    public Map<String, Arena> getArenaMap() {
        return arenas;
    }

    public Map<Arena, Round> getRoundMap() {
        return rounds;
    }

}
