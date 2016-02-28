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

import static com.google.common.base.Preconditions.checkArgument;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.metadata.CommonMetadataHolder;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.round.Round;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link Team}.
 *
 * @author Max Roncacé
 */
public class CommonTeam extends CommonMetadataHolder implements Team, CommonComponent<Round> {

    private final String id;
    private final Round round;

    private boolean orphan;

    private String name;
    private final List<Challenger> challengers = new ArrayList<>();

    public CommonTeam(String id, Round round) throws IllegalArgumentException {
        assert id != null;
        assert round != null;
        if (round.getTeam(id).isPresent()) {
            throw new IllegalArgumentException("Team \"" + id + "\" already exists");
        }
        this.id = id;
        this.name = id;
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
    public String getId() throws OrphanedComponentException {
        checkState();
        return id;
    }

    @Override
    public String getName() throws OrphanedComponentException {
        checkState();
        return name;
    }

    @Override
    public void setName(String name) throws OrphanedComponentException {
        checkState();
        this.name = name;
    }

    @Override
    public ImmutableList<Challenger> getChallengers() throws OrphanedComponentException {
        checkState();
        return ImmutableList.copyOf(challengers);
    }

    @Override
    public void addChallenger(Challenger challenger) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        checkArgument(challenger.getRound() == getRound(),
                "Cannot add challenger to team: round mismatch");
        if (challenger.getTeam().isPresent()) {
            challenger.getTeam().get().removeChallenger(challenger);
        }
        challengers.add(challenger);
        ((CommonChallenger) challenger).justSetTeam(this);
    }

    @Override
    public void removeChallenger(Challenger challenger) throws IllegalArgumentException, OrphanedComponentException {
        checkState();
        checkArgument(challengers.contains(challenger), "Cannot remove challenger from team: not present");
        challengers.remove(challenger);
        ((CommonChallenger) challenger).justSetTeam(null);
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

}
