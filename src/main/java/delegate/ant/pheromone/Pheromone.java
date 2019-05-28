package delegate.ant.pheromone;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;


public class Pheromone implements TickListener {

    static long DEFAULT_LIFETIME = 50;

    long lifetime;

//    RoadModel roadModel;


    public Pheromone(long lifetime) {
        this.lifetime = lifetime;
    }

    public Pheromone() {
        this(DEFAULT_LIFETIME);
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



}
