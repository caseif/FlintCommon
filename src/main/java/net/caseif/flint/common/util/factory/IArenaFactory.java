package net.caseif.flint.common.util.factory;

import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

public interface IArenaFactory {

    CommonArena createArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary);

}
