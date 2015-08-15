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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.common.challenger.CommonTeam;
import net.caseif.flint.common.event.round.CommonRoundChangeLifecycleStageEvent;
import net.caseif.flint.common.event.round.CommonRoundEndEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerChangeEvent;
import net.caseif.flint.common.metadata.CommonMetadatable;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.config.RoundConfigNode;
import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
@SuppressWarnings("ALL")
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
        assert arena != null;
        assert stages != null;
        this.arena = arena;
        this.stages = stages;
    }

    @Override
    public Arena getArena() {
        checkState();
        return arena;
    }

    @Override
    public ImmutableList<Challenger> getChallengers() {
        checkState();
        return ImmutableList.copyOf(challengers.values());
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) throws OrphanedObjectException {
        checkState();
        return Optional.fromNullable(challengers.get(uuid));
    }

    @Override
    public void removeChallenger(UUID uuid) throws IllegalArgumentException, OrphanedObjectException {
        checkState();
        Challenger c = challengers.get(uuid);
        if (c == null) {
            throw new IllegalArgumentException("Could not get challenger from UUID " + uuid);
        }
        removeChallenger(c);
    }

    @Override
    public void removeChallenger(Challenger challenger) throws OrphanedObjectException {
        checkState();
        if (challenger.getRound() != this) {
            throw new IllegalArgumentException("Cannot remove challenger: round mismatch");
        }
        challengers.remove(challenger.getUniqueId(), challenger);
        ((CommonChallenger) challenger).orphan();
    }

    @Override
    public ImmutableList<Team> getTeams() throws OrphanedObjectException {
        checkState();
        return ImmutableList.copyOf(teams.values());
    }

    @Override
    public Optional<Team> getTeam(String id) throws OrphanedObjectException {
        checkState();
        return Optional.fromNullable(teams.get(id));
    }

    @Override
    public Team createTeam(String id) throws IllegalArgumentException, OrphanedObjectException {
        checkState();
        if (teams.containsKey(id)) {
            throw new IllegalArgumentException("Team \"" + id + "\" already exists");
        }
        return new CommonTeam(id, this);
    }

    @Override
    public Team getOrCreateTeam(String id) throws OrphanedObjectException {
        checkState();
        Optional<Team> team = getTeam(id);
        return team.isPresent() ? team.get() : createTeam(id);
    }

    @Override
    public void removeTeam(String id) throws IllegalArgumentException, OrphanedObjectException {
        checkState();
        Team team = teams.get(id);
        if (team == null) {
            throw new IllegalArgumentException("Cannot get team with ID " + id + " in round in " + arena.getId());
        }
        removeTeam(team);
    }

    @Override
    public void removeTeam(Team team) throws IllegalArgumentException, OrphanedObjectException {
        checkState();
        if (teams.get(team.getId()) != team) {
            throw new IllegalArgumentException("Team " + team.getId() + " is owned by a different round");
        }
        teams.remove(team.getId());
        ((CommonTeam) team).orphan();
    }

    @Override
    public int getSpectatorCount() throws OrphanedObjectException {
        checkState();
        return spectators;
    }

    @Override
    public ImmutableSet<LifecycleStage> getLifecycleStages() throws OrphanedObjectException {
        checkState();
        return stages;
    }

    @Override
    public LifecycleStage getLifecycleStage() throws OrphanedObjectException {
        checkState();
        return (LifecycleStage)getLifecycleStages().toArray()[currentStage];
    }

    @Override
    public void setLifecycleStage(LifecycleStage stage) throws OrphanedObjectException {
        checkState();
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
    public Optional<LifecycleStage> getLifecycleStage(String id) throws OrphanedObjectException {
        checkState();
        for (LifecycleStage stage : stages) {
            if (stage.getId().equals(id)) {
                return Optional.of(stage);
            }
        }
        return Optional.absent();
    }

    @Override
    public LifecycleStage getLifecycleStage(int index) throws OrphanedObjectException {
        checkState();
        if (index >= stages.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stages.asList().get(index);
    }

    @Override
    public Optional<LifecycleStage> getNextLifecycleStage() throws OrphanedObjectException {
        checkState();
        return Optional.fromNullable(
                currentStage < stages.size() - 1
                        ? (LifecycleStage) getLifecycleStages().toArray()[currentStage + 1]
                        : null
        );
    }

    @Override
    public void nextLifecycleStage() throws IllegalStateException, OrphanedObjectException {
        checkState();
        Optional<LifecycleStage> next = getNextLifecycleStage();
        if (!next.isPresent()) {
            throw new IllegalStateException("Current lifecycle stage is last defined");
        }
        setLifecycleStage(next.get());
        setTime(0);
    }

    @Override
    public long getTime() throws OrphanedObjectException {
        checkState();
        return time;
    }

    @Override
    public void setTime(long time) throws OrphanedObjectException {
        checkState();
        setTime(time, true);
    }

    /**
     * Sets the time of this {@link Round}.
     *
     * @param time The new time of the {@link Round}
     * @param callEvent Whether an event should be posted
     * @throws OrphanedObjectException If this object is orphaned
     */
    public void setTime(long time, boolean callEvent) throws OrphanedObjectException {
        checkState();
        this.time = time;
        if (callEvent) {
            getMinigame().getEventBus().post(new CommonRoundTimerChangeEvent(this, this.getTime(), time));
        }
    }

    @Override
    public long getRemainingTime() throws OrphanedObjectException {
        checkState();
        return getLifecycleStage().getDuration() == -1 ? -1 : getLifecycleStage().getDuration() - time;
    }

    @Override
    public void resetTimer() throws OrphanedObjectException {
        checkState();
        setTimerTicking(false);
        time = 0;
        setLifecycleStage(getLifecycleStages().asList().get(0));
    }

    @Override
    public void end() throws OrphanedObjectException {
        checkState();
        end(getConfigValue(ConfigNode.ROLLBACK_ON_END));
    }

    @Override
    public void end(boolean rollback) throws OrphanedObjectException {
        checkState();
        end(rollback, false);
    }

    public void end(boolean rollback, boolean natural) throws OrphanedObjectException {
        checkState();
        ((CommonMinigame) getMinigame()).getRoundMap().remove(getArena());

        for (Challenger challenger : getChallengers()) {
            challenger.removeFromRound();
        }
        if (rollback) {
            getArena().rollback();
        }
        getMinigame().getEventBus().post(new CommonRoundEndEvent(this, natural));
        this.orphan();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(RoundConfigNode<T> node) throws OrphanedObjectException {
        checkState();
        return config.containsKey(node) ? (T) config.get(node) : node.getDefaultValue();
    }

    @Override
    public <T> void setConfigValue(RoundConfigNode<T> node, T value) throws OrphanedObjectException {
        checkState();
        config.put(node, value);
    }

    @Override
    public Minigame getMinigame() throws OrphanedObjectException {
        checkState();
        return getArena().getMinigame();
    }

    @Override
    public String getPlugin() throws OrphanedObjectException {
        checkState();
        return getArena().getPlugin();
    }

    public Map<UUID, Challenger> getChallengerMap() {
        checkState();
        return challengers;
    }

    public Map<String, Team> getTeamMap() {
        checkState();
        return teams;
    }

    /**
     * Checks the state of this object.
     *
     * @throws OrphanedObjectException If this object is orphaned
     */
    protected void checkState() throws OrphanedObjectException {
        if (arena == null) {
            throw new OrphanedObjectException(this);
        }
    }

    /**
     * Orphans this object.
     */
    public void orphan() {
        arena = null;
    }

}
