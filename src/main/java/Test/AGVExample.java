package Test;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.*;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.apache.commons.math3.random.RandomGenerator;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.Map;
import java.util.Set;

public class AGVExample {

    private static final long SERVICE_DURATION = 60000;
    private static final int MAX_CAPACITY = 1;

    private static final double VEHICLE_LENGTH = 2d;
    private static final int NUM_AGVS = 3;
    private static final long TEST_END_TIME = 10 * 60 * 1000L;
    private static final int TEST_SPEED_UP = 16;
    private static final int NUM_PARCELS = 3;
    private static final double NEW_PACKAGE_PROB = 0.007;
    private static final int PACKAGE_NUM_MAX = 8;

    private AGVExample() {}

    /**
     * @param args - No args.
     */
    public static void main(String[] args) {
        run(false);
    }

    /**
     * Runs the example.
     * @param testing If <code>true</code> the example will run in testing mode,
     *          automatically starting and stopping itself such that it can be run
     *          from a unit test.
     */
    public static void run(boolean testing) {
        View.Builder viewBuilder = View.builder()
//                .with(WarehouseRenderer.builder()
//                        .withMargin(VEHICLE_LENGTH))
//                .with(AGVRenderer.builder()
//                        .withDifferentColorsForVehicles())
                .with(GraphRoadModelRenderer.builder())
                .with(CustomRenderer.builder(CustomRenderer.Language.ENGLISH))
                .with(CommRenderer.builder());


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
                                            SimpleAgent.class, "/graphics/flat/taxi-32.png"));
            ;
        }

        final Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.staticGraph(AGVExample.GraphCreator.createTestGraph())
//                                .withCollisionAvoidance()
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

        for (int i = 0; i < NUM_PARCELS; i++){
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
                if (packages.size() < PACKAGE_NUM_MAX && rng.nextDouble() < NEW_PACKAGE_PROB) {
                    sim.register(new Package(
                            Parcel.builder(roadModel.getRandomPosition(rng),
                                    roadModel.getRandomPosition(rng))
                                    .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
                                    .buildDTO()));
                }
            }

            @Override
            public void afterTick(TimeLapse timeLapse) {}
        });

        sim.start();
    }

    public static class GraphCreator {
        static final int LEFT_CENTER_U_ROW = 4;
        static final int LEFT_CENTER_L_ROW = 5;
        static final int LEFT_COL = 4;
        static final int RIGHT_CENTER_U_ROW = 2;
        static final int RIGHT_CENTER_L_ROW = 4;
        static final int RIGHT_COL = 0;

        GraphCreator() {}

        static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
                                                                    int rows, Point offset) {
            final ImmutableTable.Builder<Integer, Integer, Point> builder =
                    ImmutableTable.builder();
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    builder.put(r, c, new Point(
                            offset.x + c * VEHICLE_LENGTH * 2,
                            offset.y + r * VEHICLE_LENGTH * 2));
                }
            }
            return builder.build();
        }

        static ListenableGraph<LengthData> createGraph() {
            final Graph<LengthData> g = new TableGraph<>();

            final Table<Integer, Integer, Point> leftMatrix = createMatrix(5, 10,
                    new Point(0, 0));
            for (final Map<Integer, Point> column : leftMatrix.columnMap().values()) {
                Graphs.addBiPath(g, column.values());
            }
            Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_U_ROW).values());
            Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_L_ROW).values());

            final Table<Integer, Integer, Point> rightMatrix = createMatrix(10, 7,
                    new Point(30, 6));
            for (final Map<Integer, Point> row : rightMatrix.rowMap().values()) {
                Graphs.addBiPath(g, row.values());
            }
            Graphs.addBiPath(g, rightMatrix.column(0).values());
            Graphs.addBiPath(g, rightMatrix.column(rightMatrix.columnKeySet().size()
                    - 1).values());

            Graphs.addPath(g,
                    rightMatrix.get(RIGHT_CENTER_U_ROW, RIGHT_COL),
                    leftMatrix.get(LEFT_CENTER_U_ROW, LEFT_COL));
            Graphs.addPath(g,
                    leftMatrix.get(LEFT_CENTER_L_ROW, LEFT_COL),
                    rightMatrix.get(RIGHT_CENTER_L_ROW, RIGHT_COL));

            return new ListenableGraph<>(g);
        }

        public static ListenableGraph<LengthData> createTestGraph() {
            final Graph<LengthData> g = new TableGraph<>();
            //UPPERLEFTMATRIX
            final Table<Integer, Integer, Point> upperLeftMatrix = createMatrix(5, 10,
                    new Point(0, 0));

            for (final Map<Integer, Point> column : upperLeftMatrix.columnMap().values()) {
                Graphs.addBiPath(g, column.values());
            }

            Graphs.addBiPath(g, upperLeftMatrix.row(RIGHT_COL).values());
            Graphs.addBiPath(g, upperLeftMatrix.row(9).values());

            //LOWERLEFTMATRIX
            final Table<Integer, Integer, Point> lowerLeftMatrix = createMatrix(5, 9,
                    new Point(0, 44));

            for (final Map<Integer, Point> column : lowerLeftMatrix.columnMap().values()) {
                Graphs.addBiPath(g, column.values());
            }

            Graphs.addBiPath(g, lowerLeftMatrix.row(0).values());
            Graphs.addBiPath(g, lowerLeftMatrix.row(8).values());


            //UPPERRIGHTMATRIX
            final Table<Integer, Integer, Point> rightMatrix = createMatrix(5, 20,
                    new Point(24, 0));

            for (final Map<Integer, Point> column : rightMatrix.columnMap().values()) {
                Graphs.addBiPath(g, column.values());
            }

            Graphs.addBiPath(g, rightMatrix.row(RIGHT_COL).values());
            Graphs.addBiPath(g, rightMatrix.row(9).values());
            //FROM RIGHT TO LEFT
            Graphs.addPath(g,
                    rightMatrix.get(0, 0),
                    upperLeftMatrix.get(0, 4));
            //FROM LEFT TO RIGHT
            Graphs.addPath(g,
                    upperLeftMatrix.get(9, 4),
                    rightMatrix.get(7, 0));
            //FROM UPPER TO LOWER
            Graphs.addPath(g,
                    upperLeftMatrix.get(9, 0),
                    lowerLeftMatrix.get(0, 0));
            //FROM LOWER LEFT TO RIGHT
            Graphs.addPath(g,
                    lowerLeftMatrix.get(7, 4),
                    rightMatrix.get(19, 0));

            Graphs.addPath(g,
                    rightMatrix.get(11, 0),
                    lowerLeftMatrix.get(0, 4));
            return new ListenableGraph<>(g);
        }
    }
}
