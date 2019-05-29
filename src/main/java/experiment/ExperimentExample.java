package experiment;

import Test.AGVExample;
import Test.CustomAGVRenderer;
import Test.Package;
import Test.SimpleAgent;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.*;
import com.github.rinde.rinsim.scenario.*;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.CommRenderer;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;



import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class ExperimentExample {

    private static final int VEHICLE_CAPACITY = 1;
    private static final double VEHICLE_SPEED = 1000d;
    private static final int NUM_AVGS = 5;
    private static final int NUM_PACKAGES = 20;

    private static final Point RESOLUTION = new Point(700, 600);

    private static final Point P1_PICKUP = new Point(1, 2);
    private static final Point P1_DELIVERY = new Point(4, 2);

    private static long M1 = 60 * 1000L;
    private static final long M4 = 4 * 60 * 1000L;
    private static final long M5 = 5 * 60 * 1000L;
    private static final long M7 = 7 * 60 * 1000L;
    private static final long M60 = 60 * 60 * 1000L;

    private static RoadModel rm;

    private ExperimentExample() {}

    public static void main(String[] args) {
        int uiSpeedUp = 2;
        final int index = Arrays.binarySearch(args, "speedup");
        String[] arguments = args;
        if (index >= 0) {
            checkArgument(arguments.length > index + 1,
                    "speedup option requires an integer indicating the speedup.");
            uiSpeedUp = Integer.parseInt(arguments[index + 1]);
            checkArgument(uiSpeedUp > 0, "speedup must be a positive integer.");
            final List<String> list = new ArrayList<>(Arrays.asList(arguments));
            list.remove(index + 1);
            list.remove(index);
            arguments = list.toArray(new String[] {});
        }
        final Optional<ExperimentResults> results;
        // Starts the experiment builder.
        results = Experiment.builder()
                .addConfiguration(MASConfiguration.builder()
                        // NOTE: this example uses 'namedHandler's for Depots and Parcels, while
                        // very useful for debugging these should not be used in production as
                        // these are not thread safe. Use the 'defaultHandler()' instead.
                        .addEventHandler(AddParcelEvent.class, CustomPackageHandler.INSTANCE)
                        // There is no default handle for vehicle events, here a non functioning
                        // handler is added, it can be changed to add a custom vehicle to the
                        // simulator.
                        .addEventHandler(AddVehicleEvent.class, experiment.ExperimentExample.CustomVehicleHandler.INSTANCE)
                        .addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())
                        // Note: if you multi-agent system requires the aid of a model (e.g.
                        // CommModel) it can be added directly in the configuration. Models that
                        // are only used for the solution side should not be added in the
                        // scenario as they are not part of the problem.
                        .addModel(CommModel.builder())
                        .addModel(StatsTracker.builder())
                        .build())

                // Adds the newly constructed scenario to the experiment. Every
                // configuration will be run on every scenario.
                .addScenario(createScenario())

                // The number of repetitions for each simulation. Each repetition will
                // have a unique random seed that is given to the simulator.
                .repeat(2)

                // The master random seed from which all random seeds for the
                // simulations will be drawn.
                .withRandomSeed(0)

                // The number of threads the experiment will use, this allows to run
                // several simulations in parallel. Note that when the GUI is used the
                // number of threads must be set to 1.
                .withThreads(1)

                // We add a post processor to the experiment. A post processor can read
                // the state of the simulator after it has finished. It can be used to
                // gather simulation results. The objects created by the post processor
                // end up in the ExperimentResults object that is returned by the
                // perform(..) method of Experiment.
                .usePostProcessor(new ExamplePostProcessor(NUM_AVGS))

                // Adds the GUI just like it is added to a Simulator object.
                .showGui(View.builder()
                        .with(GraphRoadModelRenderer.builder())
                        .with(CustomAGVRenderer.builder(CustomAGVRenderer.Language.ENGLISH))
                        .with(RoadUserRenderer.builder()
                                .withImageAssociation(
                                        Package.class, "/graphics/perspective/deliverypackage2.png")
                                .withImageAssociation(
                                        SimpleAgent.class, "/graphics/flat/flatbed-truck-32.png"))
                        .with(CommRenderer.builder())
                        .with(TimeLinePanel.builder())
                        .with(RouteRenderer.builder())
                        .with(RoutePanel.builder().withPositionLeft())
                        .with(StatsPanel.builder())
                        .withResolution((int) RESOLUTION.x, (int) RESOLUTION.y)
                        .withAutoPlay()
                        .withAutoClose()
                        // For testing we allow to change the speed up via the args.
                        .withSpeedUp(uiSpeedUp)
                        .withTitleAppendix("Experiment example"))
                .perform(System.out, arguments);

        if (results.isPresent()) {
            for (final Experiment.SimulationResult sr : results.get().getResults()) {
                // The SimulationResult contains all information about a specific
                // simulation, the result object is the object created by the post
                // processor, a String in this case.
                System.out.println(
                        sr.getSimArgs().getRandomSeed() + " " + sr.getResultObject());
            }
        } else {
            throw new IllegalStateException("Experiment did not complete.");
        }

    }



    public static Scenario createScenario() {

        Scenario.Builder scenario =  Scenario.builder();

        for (int i = 0; i < NUM_AVGS; i++) {
            scenario.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
                            .speed(VEHICLE_SPEED)
                            .capacity(VEHICLE_CAPACITY)
                            .build()));
        }

        for (int i = 0; i < NUM_PACKAGES; i++) {
            scenario.addEvent(AddParcelEvent.create(Parcel.builder(P1_PICKUP, P1_DELIVERY)
                    .neededCapacity(0)
                    .orderAnnounceTime(M1)
                    .pickupTimeWindow(TimeWindow.create(M1, M60))
                    .deliveryTimeWindow(TimeWindow.create(M4, M7))
                    .buildDTO()));
            M1 = M1 + 60 * 1000L;
        }





        scenario.scenarioLength(M60)
                .addModel(RoadModelBuilders.staticGraph(AGVExample.GraphCreator.createTestGraph())
//                                .withCollisionAvoidance()
                        .withDistanceUnit(SI.METER).withSpeedUnit(NonSI.KILOMETERS_PER_HOUR))
                .addModel(DefaultPDPModel.builder())
                .setStopCondition(StopConditions.limitedTime(M60));
        return scenario.build();
    }

    public enum CustomVehicleHandler implements TimedEventHandler<AddVehicleEvent> {
        INSTANCE {
            @Override

            public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
                // add your own vehicle to the simulator here
                RandomGenerator rng = sim.getRandomGenerator();
                Point start = AGVExample.GraphCreator.createTestGraph().getRandomNode(rng);
                SimpleAgent agent = new SimpleAgent(rng, event.getVehicleDTO());
                agent.setLocation(start);
                sim.register(
                        agent);
            }
        }
    }

    public enum CustomPackageHandler implements  TimedEventHandler<AddParcelEvent> {
        INSTANCE {
            @Override
            public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {

                RandomGenerator rng = sim.getRandomGenerator();
                ListenableGraph graph = AGVExample.GraphCreator.createTestGraph();

                ParcelDTO builder = Parcel.builder(graph.getRandomNode(rng), graph.getRandomNode(rng))
                        .neededCapacity(0)
                        .orderAnnounceTime(M1)
                        .pickupTimeWindow(TimeWindow.create(M1, M60))
                        .deliveryTimeWindow(TimeWindow.create(M4, M7))
                        .buildDTO();


                Package pack = new Package(builder);
                sim.register(pack);
            }
        }
    }


}
