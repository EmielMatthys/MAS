package delegate.renderer;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.google.auto.value.AutoValue;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import delegate.LocationAgent;
import delegate.agent.Truck;
import delegate.ant.pheromone.FeasibilityPheromone;
import delegate.model.DMASModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import java.util.List;
import java.util.Map;

public class CustomPDPRenderer extends CanvasRenderer.AbstractCanvasRenderer {

    private static final int ROUND_RECT_ARC_HEIGHT = 5;
    private static final int X_OFFSET = -5;
    private static final int Y_OFFSET = -30;

    private final RoadModel rm;
    private final PDPModel pm;
    private final DMASModel dm;

    public CustomPDPRenderer(RoadModel rm, PDPModel pm, DMASModel dm) {

        this.rm = rm;
        this.pm = pm;
        this.dm = dm;
    }

    enum Pred implements Predicate<Map.Entry<RoadUser, Point>> {
        INSTANCE_TRUCK {

            @Override
            public boolean apply(Map.Entry<RoadUser, Point> input) {
                return input.getKey() instanceof Truck;
            }

        },

        INSTANCE_LOC_AGENT{

            @Override
            public boolean apply(Map.Entry<RoadUser, Point> input) {
                return input.getKey() instanceof LocationAgent;
            }
        }
    }

    @Override
    public void renderStatic(GC gc, ViewPort vp) {

    }

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        final Map<RoadUser, Point> map =
                Maps.filterEntries(rm.getObjectsAndPositions(), Pred.INSTANCE_TRUCK);

        final Map<RoadUser, Point> map_loc =
                Maps.filterEntries(rm.getObjectsAndPositions(), Pred.INSTANCE_LOC_AGENT);


        for (final Map.Entry<RoadUser, Point> entry : map.entrySet()) {
            final Truck t = (Truck) entry.getKey();
            final Point p = entry.getValue();
            final int x = vp.toCoordX(p.x) + X_OFFSET;
            final int y = vp.toCoordY(p.y) + Y_OFFSET;

            final PDPModel.VehicleState vs = pm.getVehicleState(t);

            String text = null;
            final int size = (int) pm.getContentsSize(t);
            if (size > 0) {
                text = Integer.toString(size);
            }

            if (text != null) {
                final org.eclipse.swt.graphics.Point extent = gc.textExtent(text);

                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
                gc.fillRoundRectangle(x - extent.x / 2, y - extent.y / 2,
                        extent.x + 2, extent.y + 2, ROUND_RECT_ARC_HEIGHT,
                        ROUND_RECT_ARC_HEIGHT);
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));

                gc.drawText(text, x - extent.x / 2 + 1, y - extent.y / 2 + 1,
                        true);
            }
        }

        for(final Map.Entry<RoadUser, Point> entry : map_loc.entrySet()){
            final LocationAgent l = (LocationAgent) entry.getKey();
            final Point p = entry.getValue();

            final int x = vp.toCoordX(p.x) + X_OFFSET;
            final int y = vp.toCoordY(p.y) + Y_OFFSET;

            final int size = (int) dm.detectPheromone(l, FeasibilityPheromone.class)
                    .stream()
                    .map(FeasibilityPheromone::getSourcePackage)
                    .distinct()
                    .count();

            String text = null;
            if(size >= 0){
                text = Integer.toString(size);

                final org.eclipse.swt.graphics.Point extent = gc.textExtent(text);

                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GREEN));
                gc.fillRoundRectangle(x - extent.x / 2, y - extent.y / 2,
                        extent.x + 2, extent.y + 2, ROUND_RECT_ARC_HEIGHT,
                        ROUND_RECT_ARC_HEIGHT);
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));

                gc.drawText(text, x - extent.x / 2 + 1, y - extent.y / 2 + 1,
                        true);
            }
        }
    }

    public static Builder builder() {
        return new AutoValue_CustomPDPRenderer_Builder();
    }

    // This builder is using Google's AutoValue for creating a value object, see
    // https://github.com/google/auto/tree/master/value for more information on
    // how to make it work in your project. You can also manually implement the
    // equivalent code by making the class concrete and giving it a 'language'
    // field and a constructor parameter to set it. Don't forget to implement
    // equals() and hashCode().
    @AutoValue
    abstract static class Builder extends
            ModelBuilder.AbstractModelBuilder<CustomPDPRenderer, Void> {


        Builder() {
            setDependencies(RoadModel.class, PDPModel.class, DMASModel.class);
        }


        @Override
        public CustomPDPRenderer build(DependencyProvider dependencyProvider) {
            final RoadModel rm = dependencyProvider.get(RoadModel.class);
            final PDPModel pm = dependencyProvider.get(PDPModel.class);
            final DMASModel dm = dependencyProvider.get(DMASModel.class);
            return new CustomPDPRenderer(rm, pm, dm);
        }
    }
}
