package Test;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.google.auto.value.AutoValue;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import java.util.Map;

public class CustomAGVRenderer extends CanvasRenderer.AbstractCanvasRenderer {

    static final int ROUND_RECT_ARC_HEIGHT = 5;
    static final int X_OFFSET = -5;
    static final int Y_OFFSET = -30;

    public enum Language {
        DUTCH("INSTAPPEN", "UITSTAPPEN"), ENGLISH("EMBARK", "DISEMBARK");

        final String embark;
        final String disembark;

        Language(String s1, String s2) {
            embark = s1;
            disembark = s2;
        }
    }

    final RoadModel roadModel;
    final PDPModel pdpModel;
    final Language lang;

    CustomAGVRenderer(RoadModel r, PDPModel p, Language l) {
        lang = l;
        roadModel = r;
        pdpModel = p;
    }

    @Override
    public void renderStatic(GC gc, ViewPort vp) {}

    enum Pred implements Predicate<Map.Entry<RoadUser, Point>> {
        INSTANCE {

            @Override
            public boolean apply(Map.Entry<RoadUser, Point> input) {
                return input.getKey() instanceof SimpleAgent;
            }

        }
    }

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        final Map<RoadUser, Point> map =
                Maps.filterEntries(roadModel.getObjectsAndPositions(), Pred.INSTANCE);

        for (final Map.Entry<RoadUser, Point> entry : map.entrySet()) {
            final SimpleAgent t = (SimpleAgent) entry.getKey();
            final Point p = entry.getValue();
            final int x = vp.toCoordX(p.x) + X_OFFSET;
            final int y = vp.toCoordY(p.y) + Y_OFFSET;

            final PDPModel.VehicleState vs = pdpModel.getVehicleState(t);

            String text = null;
            final int size = (int) pdpModel.getContentsSize(t);
            if (vs == PDPModel.VehicleState.DELIVERING) {
                text = lang.disembark;
            } else if (vs == PDPModel.VehicleState.PICKING_UP) {
                text = lang.embark;
            } else if (size > 0) {
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
    }

    public static Builder builder(Language l) {
        return new AutoValue_CustomAGVRenderer_Builder(l);
    }

    // This builder is using Google's AutoValue for creating a value object, see
    // https://github.com/google/auto/tree/master/value for more information on
    // how to make it work in your project. You can also manually implement the
    // equivalent code by making the class concrete and giving it a 'language'
    // field and a constructor parameter to set it. Don't forget to implement
    // equals() and hashCode().
    @AutoValue
    abstract static class Builder extends
            ModelBuilder.AbstractModelBuilder<CustomAGVRenderer, Void> {

        private static final long serialVersionUID = -1772420262312399129L;

        Builder() {
            setDependencies(RoadModel.class, PDPModel.class);
        }

        abstract Language language();

        @Override
        public CustomAGVRenderer build(DependencyProvider dependencyProvider) {
            final RoadModel rm = dependencyProvider.get(RoadModel.class);
            final PDPModel pm = dependencyProvider.get(PDPModel.class);
            return new CustomAGVRenderer(rm, pm, language());
        }
    }
}
