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
import java.awt.*;
import java.util.Set;

public class DelegateExample {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        View.Builder viewBuilder = View.builder().withTitleAppendix("Delegate MAS example")
//                .with(WarehouseRenderer.builder()
//                        .withMargin(VEHICLE_LENGTH))
//                .with(AGVRenderer.builder()
//                        .withDifferentColorsForVehicles())
                .with(GraphRoadModelRenderer.builder())
                .with(RoadUserRenderer.builder().withCircleAroundObjects()
                        .withImageAssociation(
                                Package.class, "/graphics/perspective/deliverypackage2.png")
                        .withImageAssociation(
                                Truck.class, "/graphics/flat/flatbed-truck-32.png")
                        .withColorAssociation(
                                ExplorationAnt.class, new RGB(0, 255, 0))
                        .withColorAssociation(
                                FeasibilityAnt.class, new RGB(0, 0, 255))
                        .withColorAssociation(
                                IntentionAnt.class, new RGB(255, 0, 0))
                )
                ;
        ;

        final Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.staticGraph(AGVExample.GraphCreator.createTestGraph())
//                                .withCollisionAvoidance()
                                .withDistanceUnit(SI.METER).withSpeedUnit(NonSI.KILOMETERS_PER_HOUR))
                .addModel(viewBuilder)
                .addModel(DefaultPDPModel.builder())
                .addModel(DMASModel.builder())
                .build();

        RoadModel rm = sim.getModelProvider().getModel(RoadModel.class);
        RandomGenerator rng = sim.getRandomGenerator();

        //sim.getRandomGenerator().nextDouble();
        sim.getRandomGenerator().nextDouble();
        sim.getRandomGenerator().nextDouble();


        //sim.register(new FeasibilityAnt(rm.getRandomPosition(sim.getRandomGenerator())));
        sim.register(new Package(Parcel.builder(rm.getRandomPosition(rng),
                rm.getRandomPosition(rng))
                //.neededCapacity(1 + rng.nextInt(20))
                //.timeWindows(TimeWindow.create(sim.getCurrentTime(), sim.getCurrentTime()+1))
                .buildDTO()));

        sim.register(new Package(Parcel.builder(rm.getRandomPosition(rng),
                rm.getRandomPosition(rng))
                //.neededCapacity(1 + rng.nextInt(20))
                //.timeWindows(TimeWindow.create(sim.getCurrentTime(), sim.getCurrentTime()+1))
                .buildDTO()));

        sim.register(new Truck(rm.getRandomPosition(rng)));


        sim.start();

    }
}
