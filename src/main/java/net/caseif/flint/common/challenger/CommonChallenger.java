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
package net.caseif.flint.common.challenger;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.metadata.CommonMetadataHolder;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;

import java.util.UUID;

/**
 * Implements {@link Challenger}.
 *
 * @author Max Roncacé
 */
public abstract class CommonChallenger extends CommonMetadataHolder implements Challenger, CommonComponent<Round> {

    private final UUID uuid;
    private final String name;
    private final CommonRound round;

    private boolean orphan;

    private Team team;
    private boolean spectating = false;

    private boolean leaving = false;

    protected CommonChallenger(UUID playerUuid, String playerName, CommonRound round) {
        assert playerUuid != null;
        assert playerName != null;
        assert round != null;
        this.uuid = playerUuid;
        this.name = playerName;
        this.round = round;
    }

    @Override
    public Round getOwner() throws OrphanedComponentException {
        checkState();
        return round;
    }

    @Override
    public Round getRound() throws OrphanedComponentException {
        return getOwner();
    }

    @Override
    public String getName() throws OrphanedComponentException {
        checkState();
        return name;
    }

    @Override
    public UUID getUniqueId() throws OrphanedComponentException {
        checkState();
        return uuid;
    }

    @Override
    public void removeFromRound() throws OrphanedComponentException {
        checkState();
        round.removeChallenger(this);
    }

    @Override
    public Optional<Team> getTeam() throws OrphanedComponentException {
        checkState();
        return Optional.fromNullable(team);
    }

    public void setTeam(Team team) {
        if (team == null) {
            if (getTeam().isPresent()) {
                getTeam().get().removeChallenger(this);
            }
            return;
        }

        team.addChallenger(this);
    }

    public void justSetTeam(Team team) {
        this.team = team;
    }

    @Override
    public boolean isSpectating() throws OrphanedComponentException {
        checkState();
        return spectating;
    }

    @Override
    public void setSpectating(boolean spectating) throws OrphanedComponentException {
        checkState();
        if (this.spectating != spectating) {
            this.spectating = spectating;
        }
    }

    public boolean isLeaving() {
        return leaving;
    }

    public void setLeavingFlag() {
        leaving = true;
    }

    @Override
    public void checkState() throws OrphanedComponentException {
        if (orphan) {
            throw new OrphanedComponentException();
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

}
