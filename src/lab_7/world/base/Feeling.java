package lab_7.world.base;

import lab_7.world.state.FeelState;

public interface Feeling {
    public FeelState feel();
    public FeelState feel(FeelState newState);
}
