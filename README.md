run jar with optional flag:
-b to run with big map

Running will first start a simple AGV simulation example using communication messages and contract net. After the frst simulation finishes and is closed, a second simulation starts, showing our Delegate MAS implementation. Feasibility ants are not displayed to reduce visual clutter. Intention ants are shown in red and ExplorationAnts are shown in green. Green numbers indicate distinct feasibility pheromone count per location.

Both simulation will start automatically. In the second simulation, extra trucks will appear shortly after it starts.

The hop count is (currently) static and equal to 2.

The test suite can not currently be run from the command line, because of recurring problems regarding rendering all components. It can be run directly from the code, though (see ExperimentExample class).

In a small amount of (seemingly random) cases, a runtime error causes the program to crash. You should be able to just run it again.
