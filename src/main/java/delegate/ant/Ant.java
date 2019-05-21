package delegate.ant;

import delegate.model.DMASModel;
import delegate.model.DMASUser;

public class Ant implements DMASUser {


    private DMASModel dmasModel;

    @Override
    public void initialize(DMASModel dmasModel) {
        dmasModel = dmasModel;
    }
}
