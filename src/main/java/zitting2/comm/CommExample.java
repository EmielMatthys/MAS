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
package zitting2.comm;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.CommRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.google.common.collect.ImmutableList;

/**
 * This example shows a possible use case of the {@link CommModel}. It is shown
 * how this model can be used to let agents define several communication
 * properties:
 * <ul>
 * <li>Agent communication range</li>
 * <li>Agent communication reliability</li>
 * </ul>
 * The semi-transparent circle indicates the range, the color indicates the
 * reliability. The number under an agent indicates the number of messages that
 * have been received. Note that the messages that are sent contain no
 * information or purpose.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * @author Rinde van Lon
 */
public final class CommExample {
  private static final int TEST_SPEEDUP = 32;
  private static final long TEST_STOP_TIME = 10 * 60 * 1000;
  private static final int NUM_AGENTS = 5;

  private static final ImmutableList<String> NAMES =
    ImmutableList.of("Tyrion", "Cersei", "Daenerys", "Jon", "Sansa", "Arya",
      "Jaime", "Jorah", "Theon", "Samwell", "Petyr", "Varys", "Brienne",
      "Davos", "Bran", "Bronn", "Missandei", "Sandor", "Pycelle", "Eddison",
      "Podrick", "Melisandre", "Tormund", "Grey", "Tywin", "Margaery",
      "Joffrey", "Catelyn", "Barristan");
  private static final ImmutableList<String> LAST_NAMES =
    ImmutableList.of("Flowers", "Hill", "Pyke", "Rivers", "Sand", "Snow",
      "Stone", "Storm", "Waters");

  private CommExample() {}

  /**
   * Run the example.
   * @param arguments This is ignored.
   */
  public static void main(String[] arguments) {
    run(false);
  }

  /**
   * Run the example.
   * @param testing if <code>true</code> turns on testing mode.
   */
  public static void run(boolean testing) {

    View.Builder viewBuilder = View.builder()
      .withTitleAppendix("Communication example")
      .with(PlaneRoadModelRenderer.builder())
      .with(CommRenderer.builder()
        .withReliabilityColors()
        .withToString()
        .withMessageCount());

    if (testing) {
      viewBuilder = viewBuilder.withSpeedUp(TEST_SPEEDUP)
        .withAutoClose()
        .withAutoPlay()
        .withSimulatorEndTime(TEST_STOP_TIME);
    }

    final Simulator sim = Simulator.builder()
      .addModel(RoadModelBuilders.plane())
      .addModel(CommModel.builder())
      .addModel(viewBuilder)
      .build();

    for (int i = 0; i < NUM_AGENTS; i++) {
      final String first = NAMES.get(i % NAMES.size());
      final String last = LAST_NAMES.get(i % LAST_NAMES.size());
      sim.register(
        new ExampleCommunicatingAgent(sim.getRandomGenerator(),
          first + " " + last));
    }
    sim.start();
  }
}
