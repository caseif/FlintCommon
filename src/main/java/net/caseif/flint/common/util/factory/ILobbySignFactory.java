package net.caseif.flint.common.util.factory;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.util.physical.Location3D;

import com.google.gson.JsonObject;

public interface ILobbySignFactory {

    LobbySign createLobbySign(Location3D location, Arena arena, JsonObject json) throws IllegalArgumentException;

}
