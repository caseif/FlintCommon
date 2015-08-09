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
package net.caseif.flint.common.round;

import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.round.challenger.Challenger;
import net.caseif.flint.round.challenger.Team;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.round.challenger.CommonChallenger;
import net.caseif.flint.common.round.challenger.CommonTeam;
import net.caseif.flint.common.event.round.CommonRoundChangeLifecycleStageEvent;
import net.caseif.flint.common.event.round.CommonRoundEndEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerChangeEvent;
import net.caseif.flint.common.metadata.CommonMetadatable;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.config.RoundConfigNode;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
public abstract class CommonRound extends CommonMetadatable implements Round {

    protected CommonArena arena;

    protected BiMap<UUID, Challenger> challengers = HashBiMap.create();
    protected BiMap<String, Team> teams = HashBiMap.create();
    protected HashMap<RoundConfigNode<?>, Object> config = new HashMap<>();

    protected final ImmutableSet<LifecycleStage> stages;
    protected int currentStage = 0;
    protected long time;

    public int spectators;

    public CommonRound(CommonArena arena, ImmutableSet<LifecycleStage> stages) {
        this.arena = arena;
        this.stages = stages;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public Set<Challenger> getChallengers() {
        return ImmutableSet.copyOf(challengers.values());
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) {
        return Optional.fromNullable(challengers.get(uuid));
    }

    @Override
    public void removeChallenger(UUID uuid) throws IllegalArgumentException {
        Challenger c = challengers.get(uuid);
        if (c == null) {
            throw new IllegalArgumentException("Could not get challenger from UUID");
        }
        removeChallenger(c);
    }

    @Override
    public void removeChallenger(Challenger challenger) {
        if (challenger.getRound() == this) {
            challengers.remove(challenger.getUniqueId(), challenger);
            ((CommonChallenger)challenger).invalidate();
        } else {
            throw new IllegalArgumentException("Cannot remove challenger: round mismatch");
        }
    }

    @Override
    public Set<Team> getTeams() {
        return ImmutableSet.copyOf(teams.values());
    }

    @Override
    public Optional<Team> getTeam(String id) {
        return Optional.fromNullable(teams.get(id));
    }

    @Override
    public Team createTeam(String id) throws IllegalArgumentException {
        if (teams.containsKey(id)) {
            throw new IllegalArgumentException("Team \"" + id + "\" already exists");
        }
        return new CommonTeam(id, this);
    }

    @Override
    public Team getOrCreateTeam(String id) {
        Optional<Team> team = getTeam(id);
        if (team.isPresent()) {
            return team.get();
        } else {
            return createTeam(id);
        }
    }

    @Override
    public int getSpectatorCount() {
        return spectators;
    }

    @Override
    public ImmutableSet<LifecycleStage> getLifecycleStages() {
        return stages;
    }

    @Override
    public LifecycleStage getLifecycleStage() {
        return (LifecycleStage)getLifecycleStages().toArray()[currentStage];
    }

    @Override
    public void setLifecycleStage(LifecycleStage stage) {
        if (stages.contains(stage)) {
            if (!stage.equals(getLifecycleStage())) {
                Iterator<LifecycleStage> iterator = stages.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    if (iterator.next().equals(stage)) {
                        break;
                    }
                    i++;
                }
                currentStage = i;
                getMinigame().getEventBus()
                        .post(new CommonRoundChangeLifecycleStageEvent(this, getLifecycleStage(), stage));
            }
        } else {
            throw new IllegalArgumentException("Invalid lifecycle stage");
        }
    }

    @Override
    public Optional<LifecycleStage> getLifecycleStage(String id) {
        for (LifecycleStage stage : stages) {
            if (stage.getId().equals(id)) {
                return Optional.of(stage);
            }
        }
        return Optional.absent();
    }

    @Override
    public LifecycleStage getLifecycleStage(int index) {
        if (index >= stages.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stages.asList().get(index);
    }

    @Override
    public Optional<LifecycleStage> getNextLifecycleStage() {
        return Optional.fromNullable(
                currentStage < stages.size() - 1
                        ? (LifecycleStage) getLifecycleStages().toArray()[currentStage + 1]
                        : null
        );
    }

    @Override
    public void nextLifecycleStage() throws IllegalStateException {
        Optional<LifecycleStage> next = getNextLifecycleStage();
        if (!next.isPresent()) {
            throw new IllegalStateException("Current lifecycle stage is last defined");
        }
        setLifecycleStage(next.get());
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public void setTime(long time) {
        setTime(time, true);
    }

    /**
     * Sets the time of this {@link Round}.
     *
     * @param time The new time of the {@link Round}
     * @param callEvent Whether an event should be posted
     */
    public void setTime(long time, boolean callEvent) {
        this.time = time;
        if (callEvent) {
            getMinigame().getEventBus().post(new CommonRoundTimerChangeEvent(this, this.getTime(), time));
        }
    }

    @Override
    public long getRemainingTime() {
        return getLifecycleStage().getDuration() == -1 ? -1 : getLifecycleStage().getDuration() - time;
    }

    @Override
    public void resetTimer() {
        setTimerTicking(false);
        time = 0;
        setLifecycleStage((LifecycleStage)getLifecycleStages().toArray()[0]);
    }

    @Override
    public void end() {
        end(true);
    }

    @Override
    public void end(boolean rollback) {
        end(rollback, getConfigValue(ConfigNode.ROLLBACK_ON_END));
    }

    public void end(boolean rollback, boolean natural) {
        for (Challenger challenger : getChallengers()) {
            challenger.removeFromRound();
        }
        if (rollback) {
            getArena().rollback();
        }
        getMinigame().getRounds().remove(this);
        getMinigame().getEventBus().post(new CommonRoundEndEvent(this, natural));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(RoundConfigNode<T> node) {
        return config.containsKey(node) ? (T) config.get(node) : node.getDefaultValue();
    }

    @Override
    public <T> void setConfigValue(RoundConfigNode<T> node, T value) {
        config.put(node, value);
    }

    @Override
    public Minigame getMinigame() {
        return getArena().getMinigame();
    }

    @Override
    public String getPlugin() {
        return getArena().getPlugin();
    }

    public Map<UUID, Challenger> getChallengerMap() {
        return challengers;
    }

    public Map<String, Team> getTeamMap() {
        return teams;
    }

}
