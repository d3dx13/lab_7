package lab_7.world.base;

import lab_7.world.state.AffectState;

public interface Affecting {
    public void affectOn(Affected object, AffectState state);
}
