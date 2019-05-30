package experiment;


import com.github.rinde.rinsim.scenario.StopCondition;
import com.google.common.collect.ImmutableSet;
import experiment.common.StatisticsDTO;
import experiment.common.StatsProvider;

public final class CustomStopCondition {

    public int TOTAL_PARCELS;

    private CustomStopCondition(int totalParcels) {
        TOTAL_PARCELS = totalParcels;
    }

    public static StopCondition vehiclesDone() {
        return CustomStopCondition.Instances.VEHICLES_DONE;
    }

    public enum Instances implements StopCondition {

        VEHICLES_DONE {
            public boolean evaluate(TypeProvider provider) {
                StatisticsDTO stats = ((StatsProvider)provider.get(StatsProvider.class)).getStatistics();
                return 20 == stats.totalDeliveries;
            }
        };


        public ImmutableSet<Class<?>> getTypes() {
            return ImmutableSet.of(StatsProvider.class);
        }
    }
}
