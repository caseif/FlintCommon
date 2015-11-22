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
            this.team = null;
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
