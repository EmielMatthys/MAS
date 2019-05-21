package delegate.model;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.Model.AbstractModel;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;
import delegate.ant.pheromone.Pheromone;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 */
public class DMASModel extends AbstractModel<DMASUser> implements TickListener {


//    Map<Point, List<Pheromone>> pheromones;
    List<Pheromone> pheromones;

    public DMASModel() {
        this.pheromones = new ArrayList<>();
    }

    public void dropPheromone(Point location, Pheromone pheromone){
        if(!pheromones.contains(pheromone)){
            pheromones.add(pheromone);
        }
    }

    public <Y extends Pheromone> List<Y> detectPheromone(Point location, Class<Y> type){
        ArrayList<Y> result = new ArrayList<>();

        for(Pheromone ph : pheromones){
            if(type.isInstance(ph) && ph.getLocation().equals(location))
                result.add((Y) ph);
        }
        return result;
    }

    @Override
    public boolean register(DMASUser element) {
        return element.initialize(this);
    }

    @Override
    public boolean unregister(DMASUser element) {

        return true;
    }


    @Override
    public void tick(TimeLapse timeLapse) {

    }

    @Override
    public void afterTick(TimeLapse timeLapse) {
        updatePheromones();
    }

    private void updatePheromones() {
        Set<Pheromone> toRemove = new HashSet<>();
        for(Pheromone ph : pheromones){
            if (ph.disappeared())
                toRemove.add(ph);
        }
        pheromones.removeAll(toRemove);
    }

    public static Builder builder(){
        return new AutoValue_DMASModel_Builder();
    }

    @AutoValue
    public static class Builder extends AbstractModelBuilder<DMASModel, DMASUser>{

        @Override
        public DMASModel build(DependencyProvider dependencyProvider) {
            return new DMASModel();
        }
    }
}
