package delegate.ant;

import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.model.DMASModel;
import delegate.model.DMASUser;

public abstract class Ant implements DMASUser, MovingRoadUser, TickListener {

    private static double SPEED = 2;
    protected int LIFETIME = 200;

    protected DMASModel dmasModel;
    protected RoadModel roadModel;

    protected Point startLocation;


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

    public int getLIFETIME() {
        return LIFETIME;
    }

    public boolean died() {
        return getLIFETIME() <= 0;
    }

//    @Override
//    public void tick(TimeLapse timeLapse) {
//
//    }

    @Override
    public final void afterTick(TimeLapse timeLapse) {
        if(LIFETIME <= 0)
            return;
        LIFETIME--;
    }


    protected RoadModel getRoadModel() {
        return roadModel;
    }

    protected DMASModel getDmasModel() {
        return dmasModel;
    }
}
