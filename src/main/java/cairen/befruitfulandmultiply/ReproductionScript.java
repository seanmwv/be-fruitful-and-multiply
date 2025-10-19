package cairen.befruitfulandmultiply;

import script.SCRIPT;
import snake2d.util.misc.ACTION;
import util.info.INFO;
import view.sett.IDebugPanelSett;

public final class ReproductionScript implements SCRIPT {

    private final INFO info = new INFO("Be Fruitful and Multiply", "Adds a crude natural reproduction mechanic.");
    private Reproduction reproduction;

    @Override
    public CharSequence name() {
        return info.name;
    }

    @Override
    public CharSequence desc() {
        return info.desc;
    }


    @Override
    public void initBeforeGameCreated() {
        reproduction = new Reproduction();
    }


    @Override
    public boolean isSelectable() {
        return SCRIPT.super.isSelectable();
    }


    @Override
    public boolean forceInit() {
        return SCRIPT.super.forceInit();
    }


    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new ReproductionInstance(reproduction);
    }
}