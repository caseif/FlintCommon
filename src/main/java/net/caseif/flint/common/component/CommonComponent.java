package net.caseif.flint.common.component;

import net.caseif.flint.component.Component;
import net.caseif.flint.component.ComponentOwner;
import net.caseif.flint.component.exception.OrphanedComponentException;

public interface CommonComponent<T extends ComponentOwner> extends Component<T> {

    /**
     * Orphans this {@link Component}.
     */
    void orphan();

    /**
     * Checks the state of this object.
     *
     * @throws OrphanedComponentException If this object is orphaned
     */
    void checkState() throws OrphanedComponentException;

    /**
     * Sets the orphan flag on this {@link CommonComponent}.
     */
    void setOrphanFlag();

}
