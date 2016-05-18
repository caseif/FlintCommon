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

import static net.caseif.flint.common.lobby.wizard.WizardMessages.EM_COLOR;
import static net.caseif.flint.common.lobby.wizard.WizardMessages.ERROR_COLOR;
import static net.caseif.flint.common.lobby.wizard.WizardMessages.INFO_COLOR;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;

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
     * Creates a new {@link IWizardPlayer} with the given {@link UUID} for the
     * given {@link IWizardManager}.
     *
     * @param uuid The {@link UUID} of the player backing this
     *     {@link IWizardPlayer}
     * @param manager The parent {@link IWizardManager} of the new
     *     {@link IWizardManager}
     */
    @SuppressWarnings("deprecation")
    protected CommonWizardPlayer(UUID uuid, Location3D location, IWizardManager manager) {
        this.uuid = uuid;
        this.location = location;
        this.manager = manager;
        this.stage = WizardStage.GET_ARENA;
        this.wizardData = new WizardCollectedData();
        recordTargetBlockState();
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

    @Override
    @SuppressWarnings("deprecation")
    public String[] accept(String input) {
        if (input.equalsIgnoreCase("cancel")) {
            getParent().removePlayer(getUniqueId());
            playbackWithheldMessages();
            return new String[]{WizardMessages.CANCELLED};
        }
        switch (stage) {
            case GET_ARENA: {
                Optional<Arena> arena = getParent().getOwner().getArena(input);
                if (arena.isPresent()) {
                    wizardData.setArena(input);
                    stage = WizardStage.GET_TYPE;
                    return new String[]{WizardMessages.DIVIDER, WizardMessages.GET_TYPE,
                            WizardMessages.GET_TYPE_STATUS, WizardMessages.GET_TYPE_LISTING};
                } else {
                    return new String[]{WizardMessages.BAD_ARENA};
                }
            }
            case GET_TYPE: {
                try {
                    int i = Integer.parseInt(input);
                    switch (i) {
                        case 1: {
                            wizardData.setSignType(LobbySign.Type.STATUS);
                            stage = WizardStage.CONFIRMATION;
                            return constructConfirmation();
                        }
                        case 2: {
                            wizardData.setSignType(LobbySign.Type.CHALLENGER_LISTING);
                            stage = WizardStage.GET_INDEX;
                            return new String[]{WizardMessages.DIVIDER, WizardMessages.GET_INDEX};
                        }
                        default: {
                            break; // continue to block end
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // continue to block end
                }
                return new String[]{WizardMessages.BAD_TYPE};
            }
            case GET_INDEX: {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        wizardData.setIndex(i - 1);
                        stage = WizardStage.CONFIRMATION;
                        return constructConfirmation();
                    } // else: continue to block end
                } catch (NumberFormatException ex) {
                    // continue to block end
                }
                return new String[]{WizardMessages.BAD_INDEX};
            }
            case CONFIRMATION: {
                if (input.equalsIgnoreCase("yes")) {
                    Optional<Arena> arena = getParent().getOwner().getArena(wizardData.getArena());
                    if (arena.isPresent()) {
                        restoreTargetBlock();
                        switch (wizardData.getSignType()) {
                            case STATUS: {
                                try {
                                    Optional<StatusLobbySign> sign
                                            = arena.get().createStatusLobbySign(getLocation());
                                    if (sign.isPresent()) {
                                        getParent().removePlayer(getUniqueId());
                                        playbackWithheldMessages();
                                        return new String[]{WizardMessages.DIVIDER, WizardMessages.FINISH};
                                    } else {
                                        CommonCore.logSevere("Failed to register lobby sign via wizard");
                                        return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR};
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR,
                                            ERROR_COLOR + ex.getMessage()};
                                }
                            }
                            case CHALLENGER_LISTING: {
                                Optional<ChallengerListingLobbySign> sign = arena.get()
                                        .createChallengerListingLobbySign(getLocation(), wizardData.getIndex());
                                if (sign.isPresent()) {
                                    getParent().removePlayer(getUniqueId());
                                    playbackWithheldMessages();
                                    return new String[]{WizardMessages.DIVIDER, WizardMessages.FINISH};
                                } else {
                                    return new String[]{WizardMessages.DIVIDER, WizardMessages.GENERIC_ERROR};
                                }
                            }
                            default: {
                                throw new AssertionError("Invalid sign type in wizard data. "
                                        + "Report this immediately.");
                            }
                        }
                    } else {
                        getParent().removePlayer(getUniqueId());
                        playbackWithheldMessages();
                        return new String[]{WizardMessages.DIVIDER, WizardMessages.ARENA_REMOVED};
                    }
                } else if (input.equalsIgnoreCase("no")) {
                    stage = WizardStage.GET_ARENA;
                    return new String[]{WizardMessages.DIVIDER, WizardMessages.RESET, WizardMessages.DIVIDER,
                            WizardMessages.GET_ARENA};
                } else {
                    return new String[]{WizardMessages.BAD_CONFIRMATION};
                }
            }
            default: {
                throw new AssertionError("Cannot process input for wizard player. Report this immediately.");
            }
        }
    }

    @Override
    public void withholdMessage(String sender, String message) {
        withheldMessages.add(new String[]{sender, message});
    }

    private String[] constructConfirmation() {
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add(WizardMessages.DIVIDER);
        msgs.add(WizardMessages.CONFIRM_1);
        msgs.add(INFO_COLOR + "Arena ID: " + EM_COLOR + wizardData.getArena());
        msgs.add(INFO_COLOR + "Sign type: " + EM_COLOR + wizardData.getSignType().toString());
        if (wizardData.getSignType() == LobbySign.Type.CHALLENGER_LISTING) {
            msgs.add(INFO_COLOR + "Sign index: " + EM_COLOR + (wizardData.getIndex() + 1));
        }
        msgs.add(WizardMessages.CONFIRM_2);
        String[] arr = new String[msgs.size()];
        msgs.toArray(arr);
        return arr;
    }

    protected abstract void recordTargetBlockState();

    protected abstract void restoreTargetBlock();


}
