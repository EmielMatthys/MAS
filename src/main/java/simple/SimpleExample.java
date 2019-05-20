/*
 * Copyright (C) 2011-2018 Rinde R.S. van Lon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simple;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.graph.GraphBuilder;
import org.apache.commons.math3.random.RandomGenerator;

import javax.measure.unit.SI;
import java.awt.*;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * This is a very simple example of the RinSim simulator that shows how a
 * simulation is set up. It is heavily documented to provide a sort of
 * 'walk-through' experience for new users of the simulator.<br>
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 *
 * @author Rinde van Lon
 */
public final class SimpleExample {

  static final double VEHICLE_SPEED_KMH = 50d;
  static final Point MIN_POINT = new Point(0, 0);
  static final Point MAX_POINT = new Point(10, 10);
  static final long TICK_LENGTH = 1000L;
  static final long RANDOM_SEED = 123L;
  static final int NUM_VEHICLES = 10;

  static final int TEST_SPEEDUP = 16;
  static final long TEST_STOP_TIME = 10 * 60 * 1000;

  private static final double VEHICLE_LENGTH = 2d;

  private SimpleExample() {}

  /**
   * Starts the example.
   * @param args This is ignored.
   */
  public static void main(String[] args) {
    run(false);
  }

  /**
   * Run the example.
   * @param testing if <code>true</code> turns on testing mode.
   */
  public static void run(boolean testing) {
    // configure the GUI. We use separate renderers for the road model and
    // for the drivers. By default the road model is rendered as a square
    // (indicating its boundaries), and the drivers are rendered as red
    // dots.
    View.Builder viewBuilder = View.builder()
            .with(WarehouseRenderer.builder()
                    .withNodes()
                    .withOneWayStreetArrows()
                    .withNodeOccupancy())
            .withTitleAppendix("Custom example")
            .with(AGVRenderer.builder().withDifferentColorsForVehicles().withVehicleOrigin());

    if (testing) {
      viewBuilder = viewBuilder
              .withSpeedUp(TEST_SPEEDUP)
              .withAutoClose()
              .withAutoPlay()
              .withSimulatorEndTime(TEST_STOP_TIME);
    }

    // initialize a new Simulator instance
    final Simulator sim = Simulator.builder()
            // set the length of a simulation 'tick'
            .setTickLength(TICK_LENGTH)
            // set the random seed we use in this 'experiment'
            .setRandomSeed(RANDOM_SEED)
            // add a PlaneRoadModel, a model which facilitates the moving of
            // RoadUsers on a plane. The plane is bounded by two corner points:
            // (0,0) and (10,10)
            .addModel(
                    RoadModelBuilders.dynamicGraph(GraphCreator.createGraph())
                    .withCollisionAvoidance()
                    .withVehicleLength(VEHICLE_LENGTH)
                    .withDistanceUnit(SI.METER)

            )
            // in case a GUI is not desired simply don't add it.
            .addModel(viewBuilder)
            .build();

    // add a number of drivers on the road
    for (int i = 0; i < NUM_VEHICLES; i++) {
      // when an object is registered in the simulator it gets
      // automatically 'hooked up' with models that it's interested in. An
      // object declares to be interested in an model by implementing an
      // interface.
      sim.register(new AgvAgent(sim.getRandomGenerator()));
    }

    // if a GUI is added, it starts it, if no GUI is specified it will
    // run the simulation without visualization.
    sim.start();
  }

  static class Driver implements MovingRoadUser, TickListener {
    // the MovingRoadUser interface indicates that this class can move on a
    // RoadModel. The TickListener interface indicates that this class wants
    // to keep track of time. The RandomUser interface indicates that this class
    // wants to get access to a random generator

    RoadModel roadModel;
    final RandomGenerator rnd;

    @SuppressWarnings("null")
    Driver(RandomGenerator r) {
      rnd = r;
    }

    @Override
    public void initRoadUser(RoadModel model) {
      // this is where we receive an instance to the model. we store the
      // reference and add ourselves to the model on a random position.
      roadModel = model;
    }

    @Override
    public void tick(TimeLapse timeLapse) {
      // every time step (tick) this gets called. Each time we chose a
      // different destination and move in that direction using the time
      // that was made available to us.
      if (!roadModel.containsObject(this)) {
        roadModel.addObjectAt(this, roadModel.getRandomPosition(rnd));
      }
      roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {
      // we don't need this in this example. This method is called after
      // all TickListener#tick() calls, hence the name.
    }

    @Override
    public double getSpeed() {
      // the drivers speed
      return VEHICLE_SPEED_KMH;
    }

  }
  public static class GraphCreator {
    static final int LEFT_CENTER_U_ROW = 4;
    static final int LEFT_CENTER_L_ROW = 5;
    static final int LEFT_COL = 4;
    static final int RIGHT_CENTER_U_ROW = 2;
    static final int RIGHT_CENTER_L_ROW = 4;
    static final int RIGHT_COL = 0;

    GraphCreator() {}

    public static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
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

    public static ListenableGraph<LengthData> createSimpleGraph() {
      final Graph<LengthData> g = new TableGraph<>();

      final Table<Integer, Integer, Point> matrix = createMatrix(8, 6,
              new Point(0, 0));

      for (int i = 0; i < matrix.columnMap().size(); i++) {

        final Iterable<Point> path;
        if (i % 2 == 0) {
          path = Lists.reverse(newArrayList(matrix.column(i).values()));
        } else {
          path = matrix.column(i).values();
        }
        Graphs.addPath(g, path);
      }

      Graphs.addPath(g, matrix.row(0).values());
      Graphs.addPath(g, Lists.reverse(newArrayList(matrix.row(
              matrix.rowKeySet().size() - 1).values())));

      return new ListenableGraph<>(g);
    }

    public static ListenableGraph<LengthData> createGraph() {
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
  }
}
