package ksucapproj.blockstowerdefense1.logic.game_logic;

public enum UpgradeType {
    DAMAGE(0),
    SPEED(1),
    SLOWNESS(2),
    SWEEPING_EDGE(3);

    private final int index;

    UpgradeType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
