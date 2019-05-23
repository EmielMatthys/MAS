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


    public IntentionAnt(Point startLocation, Truck originator, Point packagePos, Queue<Point> path, Package pack) {
        super(startLocation, Integer.MAX_VALUE);
        this.originator = originator;
        this.path = path;
        this.packagePos = packagePos;
        this.pack = pack;
    }



    @Override
    public void tick(TimeLapse timeLapse) {
        if(roadModel.containsObjectAt(this, packagePos)){
            markDead();
        }else{
            roadModel.followPath(this, path, timeLapse);
        }
    }



    @Override
    public void visit(Package t) {
        if(t.equals(this.pack)){
            getDmasModel().dropPheromone(t, new IntentionPheromone(100, packagePos, originator));
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