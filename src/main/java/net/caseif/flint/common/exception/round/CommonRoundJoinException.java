package net.caseif.flint.common.exception.round;

import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;

import java.util.UUID;

/**
 * Implements {@link RoundJoinException}.
 */
public class CommonRoundJoinException extends RoundJoinException {

    public CommonRoundJoinException(UUID player, Round round, Throwable cause, String message) {
        super(player, round, cause, message);
    }

    public CommonRoundJoinException(UUID player, Round round, Throwable cause) {
        super(player, round, cause);
    }

    public CommonRoundJoinException(UUID player, Round round, Reason reason, String message) {
        super(player, round, reason, message);
    }

}
