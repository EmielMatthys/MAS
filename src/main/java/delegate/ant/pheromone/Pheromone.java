package delegate.ant.pheromone;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;


public class Pheromone implements TickListener, RoadUser {

    static long DEFAULT_LIFETIME = 2000;

    long lifetime;

    RoadModel roadModel;

    Point location;

    public Pheromone(long lifetime, Point location) {
        this.lifetime = lifetime;
        this.location = location;
    }

    public Pheromone(Point location) {
        this(DEFAULT_LIFETIME, location);
    }

    public boolean disappeared(){
        return lifetime <= 0;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if(lifetime > 0)
            lifetime--;
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

    @Override
    public void initRoadUser(RoadModel model) {
        this.roadModel = roadModel;
        roadModel.addObjectAt(this, location);
    }

    public Point getLocation() {
        return location;
    }
}
