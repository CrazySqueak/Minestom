package net.minestom.server.entity.fakeplayer;

import net.minestom.server.network.ConnectionManager;

public class FakePlayerOption {

    private boolean registered = false;
    private boolean inTabList = true;

    /**
     * Get if the player is registered internally as a Player
     *
     * @return true if the player is registered in {@link ConnectionManager}, false otherwise
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Set the FakePlayer as registered or not
     * <p>
     * WARNING: this can't be changed halfway
     *
     * @param registered should the fake player be registered internally
     */
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * Get if the player is visible in the tab-list or not
     *
     * @return true if the player is in the tab-list, false otherwise
     */
    public boolean isInTabList() {
        return inTabList;
    }

    /**
     * Set the player in the tab-list or not
     * <p>
     * WARNING: this can't be changed halfway
     *
     * @param inTabList should the player be in the tab-list
     */
    public void setInTabList(boolean inTabList) {
        throw new UnsupportedOperationException("Tablist option doesn't work (yet?), need to find how to do it");
        //this.inTabList = inTabList;
    }
}
