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
package net.caseif.flint.common.round;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.arena.SpawningMode;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.common.challenger.CommonTeam;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.event.round.CommonRoundChangeLifecycleStageEvent;
import net.caseif.flint.common.event.round.CommonRoundEndEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerChangeEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerStartEvent;
import net.caseif.flint.common.event.round.CommonRoundTimerStopEvent;
import net.caseif.flint.common.exception.round.CommonRoundJoinException;
import net.caseif.flint.common.metadata.CommonMetadataHolder;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.config.RoundConfigNode;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.round.JoinResult;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements {@link Round}.
 *
 * @author Max Roncacé
 */
@SuppressWarnings("DuplicateThrows")
public abstract class CommonRound extends CommonMetadataHolder implements Round, CommonComponent<Arena> {

    private AtomicInteger nextSpawn = new AtomicInteger();

    private final CommonArena arena;

    private final BiMap<UUID, Challenger> challengers = HashBiMap.create();
    private final BiMap<String, Team> teams = HashBiMap.create();
    private final HashMap<RoundConfigNode<?>, Object> config = new HashMap<>();
    private final ImmutableSet<LifecycleStage> stages;

    protected boolean orphan = false;

    protected boolean ending;
    protected int currentStage = 0;
    private long time;
    private boolean timerTicking = true;

    public CommonRound(CommonArena arena, ImmutableSet<LifecycleStage> stages) {
        assert arena != null;
        assert stages != null;
        this.arena = arena;
        this.stages = stages;
    }

    @Override
    public Arena getOwner() {
        checkState();
        return arena;
    }

    @Override
    public Arena getArena() {
        return getOwner();
    }

    @Override
    public boolean isEnding() {
        return ending;
    }

    @Override
    public ImmutableList<Challenger> getChallengers() {
        checkState();
        return ImmutableList.copyOf(challengers.values());
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) throws OrphanedComponentException {
        checkState();
        return Optional.fromNullable(challengers.get(uuid));
    }

    @SuppressWarnings({"DuplicateThrows", "deprecation"})
    @Override
    public Challenger _INVALID_addChallenger(UUID uuid) throws IllegalStateException, RoundJoinException,
            OrphanedComponentException {
        JoinResult result = addChallenger(uuid);
        RoundJoinException.Reason reason;
        switch (result.getStatus()) {
            case SUCCESS: {
                return result.getChallenger();
            }
            case INTERNAL_ERROR: {
                throw new CommonRoundJoinException(uuid, this, result.getThrowable());
            }
            case ALREADY_IN_ROUND: {
                reason = RoundJoinException.Reason.ALREADY_ENTERED;
                break;
            }
            case PLAYER_OFFLINE: {
                reason = RoundJoinException.Reason.OFFLINE;
                break;
            }
            case ROUND_FULL: {
                reason = RoundJoinException.Reason.FULL;
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        throw new CommonRoundJoinException(uuid, this, reason);
    }

    @Override
    public void removeChallenger(UUID uuid) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        Challenger c = challengers.get(uuid);
        if (c == null) {
            throw new IllegalArgumentException("Could not get challenger from UUID " + uuid);
        }
        removeChallenger(c);
    }

    /**
     * Removes the given {@link Challenger} from this {@link CommonRound},
     * taking note as to whether they are currently disconnecting from the
     * server and whether lobby signs should be updated.
     *
     * @param challenger The {@link Challenger} to remove
     * @param isDisconnecting Whether the {@link Challenger} is currently
     *     disconnecting from the server
     * @param updateSigns Whether to update the parent {@link Arena}'s
     *     {@link LobbySign}s
     */
    public void removeChallenger(Challenger challenger, boolean isDisconnecting, boolean updateSigns)
            throws OrphanedComponentException {
        checkState();
        if (challenger.getRound() != this) {
            throw new IllegalArgumentException("Cannot remove challenger: round mismatch");
        }
        if (!challenger.getRound().isEnding()) {
            challengers.remove(challenger.getUniqueId());
            if (updateSigns) {
                for (LobbySign sign : getArena().getLobbySigns()) {
                    sign.update();
                }
            }
        }

        challenger.setSpectating(false);
        challenger.setTeam(null);
    }

    public void removeChallenger(Challenger challenger) throws OrphanedComponentException {
        removeChallenger(challenger, false, true);
    }

    @Override
    public Location3D nextSpawnPoint() {
        int spawnIndex;
        switch (getConfigValue(ConfigNode.SPAWNING_MODE)) {
            case RANDOM: {
                return getArena().getSpawnPoints().values().asList()
                        .get((int) Math.floor(Math.random() * getArena().getSpawnPoints().size()));
            }
            case SEQUENTIAL: {
                spawnIndex = nextSpawn.getAndIncrement();
                if (nextSpawn.intValue() == getArena().getSpawnPoints().size()) {
                    nextSpawn.set(0);
                }
                return getArena().getSpawnPoints().values().asList().get(spawnIndex);
            }
            case SHUFFLE: {
                spawnIndex = nextSpawn.getAndIncrement();
                if (nextSpawn.intValue() == getArena().getSpawnPoints().size()) {
                    nextSpawn.set(0);
                }
                return arena.getShuffledSpawnPoints().asList().get(spawnIndex);
            }
            // SpawningMode.PROXIMITY_HIGH is handled by lower-level implementations
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public ImmutableList<Team> getTeams() throws OrphanedComponentException {
        checkState();
        return ImmutableList.copyOf(teams.values());
    }

    @Override
    public Optional<Team> getTeam(String id) throws OrphanedComponentException {
        checkState();
        return Optional.fromNullable(teams.get(id));
    }

    @Override
    public Team createTeam(String id) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        if (teams.containsKey(id)) {
            throw new IllegalArgumentException("Team \"" + id + "\" already exists");
        }
        Team team = new CommonTeam(id, this);
        teams.put(id, team);
        return team;
    }

    @Override
    public Team getOrCreateTeam(String id) throws OrphanedComponentException {
        checkState();
        Optional<Team> team = getTeam(id);
        return team.isPresent() ? team.get() : createTeam(id);
    }

    @Override
    public void removeTeam(String id) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        Team team = teams.get(id);
        if (team == null) {
            throw new IllegalArgumentException("Cannot get team with ID " + id + " in round in " + arena.getId());
        }
        removeTeam(team);
    }

    @Override
    public void removeTeam(Team team) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        if (teams.get(team.getId()) != team) {
            throw new IllegalArgumentException("Team " + team.getId() + " is owned by a different round");
        }
        teams.remove(team.getId());
        ((CommonTeam) team).orphan();
    }

    @Override
    public ImmutableList<Challenger> getSpectators() throws OrphanedComponentException {
        checkState();
        // I really wish I could use streams right now
        return ImmutableList.copyOf(Collections2.filter(getChallengers(), new Predicate<Challenger>() {
            @Override
            public boolean apply(Challenger challenger) {
                return challenger.isSpectating();
            }
        }));
    }

    @Override
    public ImmutableSet<LifecycleStage> getLifecycleStages() throws OrphanedComponentException {
        checkState();
        return stages;
    }

    @Override
    public LifecycleStage getLifecycleStage() throws OrphanedComponentException {
        checkState();
        return (LifecycleStage)getLifecycleStages().toArray()[currentStage];
    }

    @Override
    public void setLifecycleStage(LifecycleStage stage, boolean resetTimer) throws IllegalArgumentException,
            OrphanedComponentException {
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
                if (resetTimer) {
                    time = 0;
                }
                getArena().getMinigame().getEventBus()
                        .post(new CommonRoundChangeLifecycleStageEvent(this, getLifecycleStage(), stage));
            }
        } else {
            throw new IllegalArgumentException("Invalid lifecycle stage");
        }
    }

    @Override
    public void setLifecycleStage(LifecycleStage stage) throws IllegalArgumentException, OrphanedComponentException {
        setLifecycleStage(stage, false);
    }

    @Override
    public Optional<LifecycleStage> getLifecycleStage(String id) throws OrphanedComponentException {
        checkState();
        for (LifecycleStage stage : stages) {
            if (stage.getId().equals(id)) {
                return Optional.of(stage);
            }
        }
        return Optional.absent();
    }

    @Override
    public LifecycleStage getLifecycleStage(int index) throws OrphanedComponentException {
        checkState();
        if (index >= stages.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stages.asList().get(index);
    }

    @Override
    public Optional<LifecycleStage> getNextLifecycleStage() throws OrphanedComponentException {
        checkState();
        return Optional.fromNullable(
                currentStage < stages.size() - 1
                        ? (LifecycleStage) getLifecycleStages().toArray()[currentStage + 1]
                        : null
        );
    }

    @Override
    public void nextLifecycleStage() throws IllegalStateException, OrphanedComponentException {
        checkState();
        Optional<LifecycleStage> next = getNextLifecycleStage();
        if (!next.isPresent()) {
            throw new IllegalStateException("Current lifecycle stage is last defined");
        }
        setLifecycleStage(next.get());
        setTime(0);
    }

    @Override
    public long getTime() throws OrphanedComponentException {
        checkState();
        return time;
    }

    @Override
    public void setTime(long time) throws OrphanedComponentException {
        checkState();
        setTime(time, true);
    }

    /**
     * Sets the time of this {@link Round}.
     *
     * @param time The new time of the {@link Round}
     * @param callEvent Whether an event should be posted
     * @throws OrphanedComponentException If this object is orphaned
     */
    public void setTime(long time, boolean callEvent) throws OrphanedComponentException {
        checkState();
        this.time = time;
        if (callEvent) {
            getArena().getMinigame().getEventBus().post(new CommonRoundTimerChangeEvent(this, this.getTime(), time));
        }
    }

    @Override
    public long getRemainingTime() throws OrphanedComponentException {
        checkState();
        return getLifecycleStage().getDuration() == -1 ? -1 : getLifecycleStage().getDuration() - time;
    }

    @Override
    public void resetTimer() throws OrphanedComponentException {
        checkState();
        setTimerTicking(false);
        time = 0;
        setLifecycleStage(getLifecycleStages().asList().get(0));
    }

    @Override
    public void end() throws IllegalStateException, OrphanedComponentException {
        checkState();
        end(getConfigValue(ConfigNode.ROLLBACK_ON_END));
    }

    @Override
    public void end(boolean rollback) throws IllegalStateException, OrphanedComponentException {
        checkState();
        end(rollback, false);
    }

    @Override
    public boolean isTimerTicking() throws OrphanedComponentException {
        checkState();
        return this.timerTicking;
    }

    @Override
    public void setTimerTicking(boolean ticking) throws OrphanedComponentException {
        checkState();
        if (ticking != isTimerTicking()) {
            timerTicking = ticking;
            getArena().getMinigame().getEventBus()
                    .post(ticking ? new CommonRoundTimerStartEvent(this) : new CommonRoundTimerStopEvent(this));
        }
    }

    public void end(boolean rollback, boolean natural) throws IllegalStateException, OrphanedComponentException {
        checkState();
        if (ending) {
            throw new IllegalStateException("Cannot invoke end() on a round more than once");
        }
        ending = true;
        ((CommonMinigame) getArena().getMinigame()).getRoundMap().remove(getArena());

        for (Challenger challenger : getChallengers()) {
            removeChallenger(challenger, false, false);
        }
        if (rollback) {
            getArena().rollback();
        }
        getArena().getMinigame().getEventBus().post(new CommonRoundEndEvent(this, natural));
        for (Challenger challenger : getChallengers()) {
            ((CommonChallenger) challenger).orphan();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(RoundConfigNode<T> node) throws OrphanedComponentException {
        checkState();
        return config.containsKey(node) ? (T) config.get(node) : getArena().getMinigame().getConfigValue(node);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <T> void setConfigValue(RoundConfigNode<T> node, T value) throws OrphanedComponentException {
        checkState();
        config.put(node, value);

        // compatibility
        if (node == ConfigNode.RANDOM_SPAWNING) {
            config.put(ConfigNode.SPAWNING_MODE, (Boolean) value ? SpawningMode.RANDOM : SpawningMode.SEQUENTIAL);
        }
    }

    public Map<UUID, Challenger> getChallengerMap() {
        checkState();
        return challengers;
    }

    public Map<String, Team> getTeamMap() {
        checkState();
        return teams;
    }

    @Override
    public void checkState() throws OrphanedComponentException {
        if (orphan) {
            throw new OrphanedComponentException(this);
        }
    }

    @Override
    public void orphan() {
        CommonCore.orphan(this);
    }

    @Override
    public void setOrphanFlag() {
        this.orphan = true;
    }

    /**
     * Return whether this {@link SteelRound} object is orphaned.
     *
     * @return Whether this {@link SteelRound} object is orphaned
     */
    public boolean isOrphaned() {
        return orphan;
    }

}
