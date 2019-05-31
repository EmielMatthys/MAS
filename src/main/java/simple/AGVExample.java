package simple;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.*;
import com.github.rinde.rinsim.util.TimeWindow;
import org.apache.commons.math3.random.RandomGenerator;
import graph.GraphCreator;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.Set;

public class AGVExample {

    private static final long SERVICE_DURATION = 60000;
    private static final int MAX_CAPACITY = 1;


    private static final int NUM_AGVS = 7;
    private static final long TEST_END_TIME = 10 * 60 * 1000L;
    private static final int TEST_SPEED_UP = 16;
    private static final int NUM_PARCELS = 7;
    private static final double NEW_PACKAGE_PROB = 0.05;

    private AGVExample() {}

    /**
     * @param args - No args.
     */
    public static void main(String[] args) {
        run(false, false);
    }

    /**
     * Runs the example.
     * @param testing If <code>true</code> the example will run in testing mode,
     *          automatically starting and stopping itself such that it can be run
     *          from a unit test.
     */
    public static void run(boolean testing, boolean bigGraph) {
        View.Builder viewBuilder = View.builder()
//                .with(WarehouseRenderer.builder()
//                        .withMargin(VEHICLE_LENGTH))
//                .with(AGVRenderer.builder()
//                        .withDifferentColorsForVehicles())
                .with(GraphRoadModelRenderer.builder())
                .with(CustomAGVRenderer.builder(CustomAGVRenderer.Language.ENGLISH))
                .with(CommRenderer.builder())
                .withAutoPlay();


        if (testing) {
            viewBuilder = viewBuilder.withAutoPlay()
                    .withAutoClose()
                    .withSimulatorEndTime(TEST_END_TIME)
                    .withTitleAppendix("TESTING")
                    .withSpeedUp(TEST_SPEED_UP)
                    .with(RoadUserRenderer.builder()
                    .withImageAssociation(
                            Package.class, "/graphics/perspective/deliverypackage2.png"));
        } else {
            viewBuilder =
                    viewBuilder.withTitleAppendix("AGV example")
                            .with(RoadUserRenderer.builder()
                                    .withImageAssociation(
                                            Package.class, "/graphics/perspective/deliverypackage2.png")
                                    .withImageAssociation(
                                            SimpleAgent.class, "/graphics/flat/flatbed-truck-32.png"));
            ;
        }


        Graph graph = GraphCreator.createSmallGraph();
        if(bigGraph)
            graph = GraphCreator.createLargeGraph();

        final Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.staticGraph(graph)
                                .withDistanceUnit(SI.METER).withSpeedUnit(NonSI.KILOMETERS_PER_HOUR))
                .addModel(viewBuilder)
                .addModel(DefaultPDPModel.builder())
                .addModel(CommModel.builder())
                .build();

        RoadModel roadModel = sim.getModelProvider().getModel(RoadModel.class);
        sim.getRandomGenerator().nextDouble();
        sim.getRandomGenerator().nextDouble();
        for (int i = 0; i < NUM_AGVS; i++) {
            sim.register(new SimpleAgent(sim.getRandomGenerator(), roadModel.getRandomPosition(sim.getRandomGenerator())));
        }
        RandomGenerator rng = sim.getRandomGenerator();

        for (int i = 0; i < 4; i++){
            sim.register(new Package(
                    Parcel.builder(roadModel.getRandomPosition(rng),
                            roadModel.getRandomPosition(rng))
                            .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
                            .buildDTO()));
        }


        sim.addTickListener(new TickListener() {
            @Override
            public void tick(TimeLapse time) {
                Set<Package> packages = roadModel.getObjectsOfType(Package.class);
                if (packages.size() < NUM_PARCELS && rng.nextDouble() < NEW_PACKAGE_PROB) {
                    sim.register(new Package(
                            Parcel.builder(roadModel.getRandomPosition(rng),
                                    roadModel.getRandomPosition(rng))
                                    .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
                                    .timeWindows(TimeWindow.create(sim.getCurrentTime(), sim.getCurrentTime()+1))
                                    .buildDTO()));
                }
            }

            @Override
            public void afterTick(TimeLapse timeLapse) {}
        });

        sim.start();
    }


}
