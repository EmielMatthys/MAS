package experiment;

import com.github.rinde.rinsim.pdptw.common.*;
import delegate.agent.Truck;
import delegate.model.DMASModel;
import delegate.renderer.CustomPDPRenderer;
import experiment.common.*;
import simple.CustomAGVRenderer;
import simple.Package;
import simple.SimpleAgent;
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
import com.github.rinde.rinsim.scenario.*;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.CommRenderer;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import graph.GraphCreator;
import org.apache.commons.math3.random.RandomGenerator;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

public class ExperimentExample {

    /**
     * To change between simple and delegate
     *
     * line 51 -> Simple : VEHICLE_SPEED = 1000d / Delegate VEHICLE_SPEED = 0.2d
     * line 85 -> Choose one of the two
     * line 89 -> Choose one of the two
     * line 99 -> Comment out if you want to run simple example
     */

    private static final int VEHICLE_CAPACITY = 1;
    private static final double VEHICLE_SPEED = 1000d;
    private static final int NUM_AGVS = 12;
    private static final int NUM_PACKAGES = 48;

    private static final Point RESOLUTION = new Point(700, 600);

    private static final Point P1_PICKUP = new Point(1, 2);
    private static final Point P1_DELIVERY = new Point(4, 2);

    private static long M1 = 60 * 1000L;
    private static final long M60 = 60 * 60 * 1000L;

    private static RoadModel rm;

    static Random rnd = new Random();

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
        Optional<ExperimentResults> results; // = Optional.absent();
        // Starts the experiment builder.
        Experiment.Builder exp = Experiment.builder()
                .addConfiguration(MASConfiguration.builder()
                        //Choose one of two
                        .addEventHandler(AddParcelEvent.class, SimplePackageHandler.INSTANCE)
                        //.addEventHandler(AddParcelEvent.class, DelegatePackageHandler.INSTANCE)

                        //Choose one of two
                        .addEventHandler(AddVehicleEvent.class, SimpleAgentHandler.INSTANCE)
                        //.addEventHandler(AddVehicleEvent.class, DelegateTruckHandler.INSTANCE)


                        .addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())

                        .addModel(CommModel.builder())
                        .addModel(StatsTracker.builder())

                        //Comment out if you want to run simple
                        //.addModel(DMASModel.builder())
                        //.addModel(CustomPDPRenderer.builder())

                        .build())


                // Adds the newly constructed scenario to the experiment. Every
                // configuration will be run on every scenario.
                .addScenario(createScenario())

                // The number of repetitions for each simulation. Each repetition will
                // have a unique random seed that is given to the simulator.
                .repeat(1)

                // The master random seed from which all random seeds for the
                // simulations will be drawn.
                .withRandomSeed(1)

                // The number of threads the experiment will use, this allows to run
                // several simulations in parallel. Note that when the GUI is used the
                // number of threads must be set to 1.
                .withThreads(1)

                // We add a post processor to the experiment. A post processor can read
                // the state of the simulator after it has finished. It can be used to
                // gather simulation results. The objects created by the post processor
                // end up in the ExperimentResults object that is returned by the
                // perform(..) method of Experiment.
                .usePostProcessor(new ExamplePostProcessor(NUM_AGVS))

                // Adds the GUI just like it is added to a Simulator object.
                .showGui(View.builder()

                        .with(GraphRoadModelRenderer.builder())
                        .with(CustomAGVRenderer.builder(CustomAGVRenderer.Language.ENGLISH))
                        .with(RoadUserRenderer.builder()
                                .withImageAssociation(
                                        simple.Package.class, "/graphics/perspective/deliverypackage2.png")
                                .withImageAssociation(
                                        simple.SimpleAgent.class, "/graphics/flat/flatbed-truck-32.png"))
                        .with(CommRenderer.builder())
                        .with(TimeLinePanel.builder())
                        .with(RouteRenderer.builder())
                        //.with(RoutePanel.builder().withPositionLeft())
                        .with(StatsPanel.builder())
                        .withResolution((int) RESOLUTION.x, (int) RESOLUTION.y)
                        .withAutoPlay()
                        .withAutoClose()
                        // For testing we allow to change the speed up via the args.
                        .withSpeedUp(uiSpeedUp)
                        .withTitleAppendix("Experiment example"));
                //.perform(System.out, arguments);

//        if (results.isPresent()) {
//            for (final Experiment.SimulationResult sr : results.get().getResults()) {
//                // The SimulationResult contains all information about a specific
//                // simulation, the result object is the object created by the post
//                // processor, a String in this case.
//                System.out.println(
//                        sr.getSimArgs().getRandomSeed() + " " + sr.getResultObject());
//            }
//        } else {
//            throw new IllegalStateException("Experiment did not complete.");
//        }

        for (int k = 0; k < 15; k++) {
            try {
                results = exp.perform(System.out, arguments);
            } catch (Exception e) {
                System.out.println("FAILED, TRYING AGAIN");
                k--;
                results = Optional.absent();
            }
            if (results.isPresent()) {
                for (final Experiment.SimulationResult sr : results.get().getResults()) {
                    // The SimulationResult contains all information about a specific
                    // simulation, the result object is the object created by the post
                    // processor, a String in this case.
                    System.out.println(
                            sr.getSimArgs().getRandomSeed() + " " + sr.getResultObject());
                }
            }
        }



    }



    public static Scenario createScenario() {

        Scenario.Builder scenario =  Scenario.builder();

        for (int i = 0; i < NUM_AGVS; i++) {
            scenario.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
                    .speed(VEHICLE_SPEED)
                    .capacity(VEHICLE_CAPACITY)
                    .build()));
        }

        for (int i = 0; i < NUM_PACKAGES; i++) {
            scenario.addEvent(AddParcelEvent.create(Parcel.builder(P1_PICKUP, P1_DELIVERY)
                    .neededCapacity(0)
                    .orderAnnounceTime(M1)
                    .pickupTimeWindow(TimeWindow.create(M1, M1))
                    .deliveryTimeWindow(TimeWindow.create(M1, M1))
                    .buildDTO()));
            M1 = M1 + 60 * 1000L;
        }





        scenario.scenarioLength(M60)
                .addModel(RoadModelBuilders.staticGraph(GraphCreator.createSmallGraph())
//                                .withCollisionAvoidance()
                        .withDistanceUnit(SI.METER).withSpeedUnit(NonSI.KILOMETERS_PER_HOUR))
                .addModel(DefaultPDPModel.builder())
                .setStopCondition(CustomStopCondition.vehiclesDone());
        return scenario.build();
    }

    public enum SimpleAgentHandler implements TimedEventHandler<AddVehicleEvent> {
        INSTANCE {
            @Override

            public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
                // add your own vehicle to the simulator here
                RandomGenerator rng = sim.getRandomGenerator();
                Point start = GraphCreator.createSmallGraph().getRandomNode(rng);
                SimpleAgent agent = new SimpleAgent(rng, event.getVehicleDTO());
                agent.setLocation(start);
                sim.register(
                        agent);
            }
        }
    }

    public enum SimplePackageHandler implements  TimedEventHandler<AddParcelEvent> {
        INSTANCE {
            @Override
            public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {

                RandomGenerator rng = sim.getRandomGenerator();

                ListenableGraph graph = GraphCreator.createSmallGraph();

                Package p = new Package(event.getParcelDTO());
                int point = rnd.nextInt(50);
                for (int i = 0; i < point; i++) rng.nextInt();

                ParcelDTO builder = Parcel.builder(graph.getRandomNode(rng), graph.getRandomNode(rng))
                        .neededCapacity(0)
                        .orderAnnounceTime(p.getOrderAnnounceTime())
                        .pickupTimeWindow(p.getPickupTimeWindow())
                        .deliveryTimeWindow(p.getDeliveryTimeWindow())
                        .buildDTO();


                Package pack = new Package(builder);
                sim.register(pack);
            }
        }
    }

    public enum DelegateTruckHandler implements TimedEventHandler<AddVehicleEvent> {
        INSTANCE {
            @Override

            public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
                // add your own vehicle to the simulator here
                RandomGenerator rng = sim.getRandomGenerator();
                Point start = GraphCreator.createSmallGraph().getRandomNode(rng);
                Truck agent = new Truck(rng, event.getVehicleDTO());
                agent.setStartLocation(start);
                sim.register(
                        agent);
            }
        }
    }

    public enum DelegatePackageHandler implements  TimedEventHandler<AddParcelEvent> {
        INSTANCE {
            @Override
            public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {

                RandomGenerator rng = sim.getRandomGenerator();
                ListenableGraph graph = GraphCreator.createSmallGraph();

                delegate.agent.Package p = new delegate.agent.Package(event.getParcelDTO());

                int point = rnd.nextInt(50);
                for (int i = 0; i < point; i++) rng.nextInt();

                ParcelDTO builder = Parcel.builder(graph.getRandomNode(rng), graph.getRandomNode(rng))
                        .neededCapacity(0)
                        .orderAnnounceTime(p.getOrderAnnounceTime())
                        .pickupTimeWindow(p.getPickupTimeWindow())
                        .deliveryTimeWindow(p.getDeliveryTimeWindow())
                        .buildDTO();


                delegate.agent.Package pack = new delegate.agent.Package(builder);
                sim.register(pack);
            }
        }
    }

}
