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
package net.caseif.flint.common.lobby.wizard;

import net.caseif.flint.util.physical.Location3D;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a player currently in the lobby wizard.
 *
 * @author Max Roncacé
 */
public abstract class CommonWizardPlayer implements IWizardPlayer {

    protected final UUID uuid;
    protected final Location3D location;
    protected final IWizardManager manager;

    protected WizardStage stage;
    protected final IWizardCollectedData wizardData;

    protected final List<String[]> withheldMessages = new ArrayList<>();

    /**
     * Creates a new {@link WizardPlayer} with the given {@link UUID} for the
     * given {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player backing this
     *     {@link WizardPlayer}
     * @param manager The parent {@link WizardManager} of the new
     *     {@link WizardManager}
     */
    @SuppressWarnings("deprecation")
    protected CommonWizardPlayer(UUID uuid, Location3D location, IWizardManager manager) {
        this.uuid = uuid;
        this.location = location;
        this.manager = manager;
        this.stage = WizardStage.GET_ARENA;
        this.wizardData = new WizardCollectedData();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Location3D getLocation() {
        return location;
    }

    @Override
    public IWizardManager getParent() {
        return manager;
    }

}
