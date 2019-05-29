package delegate.ant;

import com.github.rinde.rinsim.cli.Option;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.pheromone.IntentionPheromone;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class IntentionAnt extends Ant implements TickListener {

    private Truck originator;


    private Queue<Point> path;
    private Point packagePos;
    private Package pack;


    public IntentionAnt(Point startLocation, Truck originator, Point packagePos) {
        super(startLocation, Integer.MAX_VALUE);
        //this.SPEED = 10;
        this.originator = originator;
        this.packagePos = packagePos;
    }



    @Override
    public void tick(TimeLapse timeLapse) {
        if(roadModel.containsObjectAt(this, packagePos)){
            markDead();
        }else{
            roadModel.moveTo(this, packagePos, timeLapse);
        }
    }



    @Override
    public void visit(Package t) {
        if(t.getPickupLocation().equals(packagePos)){

            List<IntentionPheromone> ph = getDmasModel().detectPheromone(t, IntentionPheromone.class);

            if(ph.stream().anyMatch(pheromone -> !pheromone.getOriginator().equals(originator))){
                originator.intentionCallback();
            }


                getDmasModel().dropPheromone(t, new IntentionPheromone(100, t.getPickupLocation(), originator));
            LIFETIME = 0;
        }

    }
}


/*
RoadModel rm = roadModel;

        // MSS NOG AANPASSEN NAAR PATH BEPAALD DOOR EXPLORATION ANTS
        rm.moveTo(this, destination.get(), timeLapse);

        if (rm.getPosition(this).equals(destination.get())) {

            //Check for other pheromones
            List<IntentionPheromone> ph = dmasModel.detectPheromone(rm.getPosition(this), IntentionPheromone.class);

            if (ph.isEmpty() || ph.get(0).getOriginator().equals(originator)) {
                dmasModel.dropPheromone(new IntentionPheromone(100, rm.getPosition(this), originator));
                originator.notify(true);
            } else {
                originator.notify(false);
            }

            LIFETIME = 0;

        }
 */