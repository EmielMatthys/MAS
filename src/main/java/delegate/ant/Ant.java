package delegate.ant;

import delegate.model.DMASModel;
import delegate.model.DMASUser;

public class Ant implements DMASUser {


    private DMASModel dmasModel;

    @Override
    public boolean initialize(DMASModel dmasModel) {
        this.dmasModel = dmasModel;
        return true;
    }
}
