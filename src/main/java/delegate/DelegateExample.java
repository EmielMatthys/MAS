package delegate;

import Test.AGVExample;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import delegate.agent.Package;
import delegate.agent.Truck;
import delegate.ant.ExplorationAnt;
import delegate.ant.FeasibilityAnt;
import delegate.ant.IntentionAnt;
import delegate.model.DMASModel;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;


import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.Set;

public class DelegateExample {

    private static final int MAX_PACKAGES = 3;

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        View.Builder viewBuilder = View.builder().withTitleAppendix("Delegate MAS example")
//                .with(WarehouseRenderer.builder()
//                        .withMargin(VEHICLE_LENGTH))
//                .with(AGVRenderer.builder()
//                        .withDifferentColorsForVehicles())
                .with(GraphRoadModelRenderer.builder().withNodeCircles())
                .with(RoadUserRenderer.builder()
                        .withImageAssociation(
                                Package.class, "/graphics/perspective/deliverypackage2.png")
                        .withImageAssociation(
                                Truck.class, "/graphics/flat/flatbed-truck-32.png")
                        .withColorAssociation(
                                ExplorationAnt.class, new RGB(0, 255, 0))
                        //.withColorAssociation(
                        //        FeasibilityAnt.class, new RGB(0, 0, 255))
                        .withColorAssociation(
                                IntentionAnt.class, new RGB(255, 0, 0))
                        .withColorAssociation(
                                LocationAgent.class, new RGB( 125, 125, 0))
                )
                ;


        final Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.dynamicGraph(AGVExample.GraphCreator.createTestGraph())
//                                .withCollisionAvoidance()
                                .withDistanceUnit(SI.METER).withSpeedUnit(NonSI.KILOMETERS_PER_HOUR))
                .addModel(viewBuilder)
                .addModel(DefaultPDPModel.builder())
                .addModel(DMASModel.builder())
                .build();

        RoadModel rm = sim.getModelProvider().getModel(RoadModel.class);
        RandomGenerator rng = sim.getRandomGenerator();

        //sim.getRandomGenerator().nextDouble();
        //sim.getRandomGenerator().nextDouble();
        sim.getRandomGenerator().nextDouble();

        Point rand = rm.getRandomPosition(sim.getRandomGenerator());
        Point pos1 = new Point(32, 76);
        Point pos2 = new Point(32, 77);
        Point pos3 = new Point(32, 78);

        Package p1 = new Package(Parcel.builder(rand,
                rm.getRandomPosition(rng))
                .buildDTO());
//
//        Package p2 = new Package(Parcel.builder(pos2,
//                rm.getRandomPosition(rng))
//                .buildDTO());
//
//        Package p3 = new Package(Parcel.builder(pos2,
//                rm.getRandomPosition(rng))
//                .buildDTO());

        sim.register(p1);
//        sim.register(p2);
//        sim.register(p3);

        sim.register(new Truck(rng, rm.getRandomPosition(rng)));

        for(int i = 0; i < MAX_PACKAGES; i++){
            sim.register(new Package(Parcel.builder(rm.getRandomPosition(rng),
                    rm.getRandomPosition(rng))
                    .buildDTO()));
            sim.getRandomGenerator().nextDouble();
            sim.getRandomGenerator().nextDouble();
            sim.getRandomGenerator().nextDouble();
            sim.getRandomGenerator().nextDouble();
        }

        sim.start();

    }
}
