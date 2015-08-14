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

import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.common.metadata.CommonMetadatable;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;

import java.util.UUID;

/**
 * Implements {@link Challenger}.
 *
 * @author Max Roncacé
 */
public class CommonChallenger extends CommonMetadatable implements Challenger {

    protected UUID uuid;
    protected String name;
    protected CommonRound round;

    protected Team team;
    protected boolean spectating = false;

    @Override
    public String getName() throws OrphanedObjectException {
        checkState();
        return name;
    }

    @Override
    public UUID getUniqueId() throws OrphanedObjectException {
        checkState();
        return uuid;
    }

    @Override
    public Round getRound() throws OrphanedObjectException {
        checkState();
        return round;
    }

    @Override
    public void removeFromRound() throws OrphanedObjectException {
        checkState();
        round.removeChallenger(this);
    }

    @Override
    public Optional<Team> getTeam() throws OrphanedObjectException {
        checkState();
        return Optional.fromNullable(team);
    }

    @Override
    public void setTeam(Team team) throws OrphanedObjectException {
        checkState();
        this.team = team;
    }

    @Override
    public boolean isSpectating() throws OrphanedObjectException {
        checkState();
        return spectating;
    }

    @Override
    public void setSpectating(boolean spectating) throws OrphanedObjectException {
        checkState();
        if (this.spectating != spectating) {
            this.spectating = spectating;
            round.spectators += spectating ? 1 : -1;
        }
    }

    @Override
    public Minigame getMinigame() throws OrphanedObjectException {
        checkState();
        return round.getMinigame();
    }

    @Override
    public String getPlugin() throws OrphanedObjectException {
        checkState();
        return round.getPlugin();
    }

    /**
     * Checks the state of this object.
     *
     * @throws OrphanedObjectException If this object is orphaned
     */
    protected void checkState() throws OrphanedObjectException {
        if (round == null) {
            throw new OrphanedObjectException();
        }
    }

    public void orphan() {
        round = null;
    }

}
