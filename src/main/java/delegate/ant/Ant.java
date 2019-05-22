package delegate.ant;

import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import delegate.model.DMASModel;
import delegate.model.DMASUser;

public class Ant implements DMASUser, MovingRoadUser {

    private static double SPEED = 5;

    protected DMASModel dmasModel;
    protected RoadModel roadModel;

    private Point startLocation;


    public Ant(Point startLocation) {
        this.startLocation = startLocation;
    }

    @Override
    public boolean initialize(DMASModel dmasModel) {
        this.dmasModel = dmasModel;
        return true;
    }

    @Override
    public double getSpeed() {
        return SPEED;
    }


    @Override
    public void initRoadUser(RoadModel model) {
        this.roadModel = model;
        roadModel.addObjectAt(this, startLocation);
    }
}
