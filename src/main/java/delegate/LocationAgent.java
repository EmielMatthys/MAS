package delegate;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import delegate.ant.Ant;
import delegate.model.DMASModel;

public class LocationAgent implements AntAcceptor, RoadUser {

    Point location;
    RoadModel rm;

    public LocationAgent(Point location) {
        this.location = location;
    }

    @Override
    public void accept(Ant ant) {
        ant.visit(this);
    }

    @Override
    public boolean initialize(DMASModel dmasModel) {
        return dmasModel.addAntAcceptor(this);
    }

    @Override
    public void initRoadUser(RoadModel model) {
        model.addObjectAt(this, location);
        rm = model;
    }
}
