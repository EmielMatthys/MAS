package delegate.ant;

import com.github.rinde.rinsim.cli.Option;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import delegate.agent.Truck;
import delegate.ant.pheromone.IntentionPheromone;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class IntentionAnt extends Ant implements TickListener {

    private Truck originator;
    private Optional<Point> destination;

    private Optional<Point> packagePos;

    private Queue<Point> path;



    public IntentionAnt(Point startLocation, Truck originator, Point packagePos) {
        super(startLocation);
        this.originator = originator;
        this.destination = Optional.of(packagePos);
    }



    @Override
    public void tick(TimeLapse timeLapse) {
        RoadModel rm = roadModel;

        // MSS NOG AANPASSEN NAAR PATH BEPAALD DOOR EXPLORATION ANTS
        rm.moveTo(this, destination.get(), timeLapse);

        if (rm.getPosition(this).equals(destination.get())) {
            dmasModel.dropPheromone(new IntentionPheromone(100, rm.getPosition(this)));
            System.out.println("DROPPED PHEROMONE, CALLING TRUCK");
            originator.notify(true, this);

            //Check for other pheromones
            List<IntentionPheromone> ph = dmasModel.detectPheromone(rm.getPosition(this), IntentionPheromone.class);

            if (ph.isEmpty() || ph.get(0).getOriginator().equals(originator)) {
                dmasModel.dropPheromone(new IntentionPheromone(100, rm.getPosition(this), originator));
                System.out.println("DROPPED PHEROMONE, CALLING TRUCK");
                originator.notify(true);
            } else {
                originator.notify(false);
            }

            LIFETIME = 0;

        }
    }

}
