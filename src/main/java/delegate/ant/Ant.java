package delegate.ant;

import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.LocationAgent;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.model.DMASModel;
import delegate.model.DMASUser;

public abstract class Ant implements DMASUser, MovingRoadUser, TickListener {

    protected static double SPEED = 6;
    protected int LIFETIME = 300;

    protected DMASModel dmasModel;
    protected RoadModel roadModel;

    protected Point startLocation;


    public Ant(Point startLocation, int lifetime) {
        this.startLocation = startLocation;
        this.LIFETIME = lifetime;
    }

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

    protected boolean deathMark = false;

    public void markDead(){
        deathMark = true;
    }



    @Override
    public final void afterTick(TimeLapse timeLapse) {
        if(LIFETIME <= 0)
            return;
        else if(deathMark)
            LIFETIME = 0;
        LIFETIME--;
    }


    public void visit(Package t){}
    public void visit(LocationAgent t){}

    protected RoadModel getRoadModel() {
        return roadModel;
    }

    protected DMASModel getDmasModel() {
        return dmasModel;
    }
}
