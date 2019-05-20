package factory;

import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.ImmutableList;

@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_AgvModel_Builder extends AgvModel.Builder {
  private final ImmutableList<ImmutableList<Point>> getPoints;
  private final ImmutableList<Point> getBorder;

  AutoValue_AgvModel_Builder(
      ImmutableList<ImmutableList<Point>> getPoints,
      ImmutableList<Point> getBorder) {
    if (getPoints == null) {
      throw new NullPointerException("Null getPoints");
    }
    this.getPoints = getPoints;
    if (getBorder == null) {
      throw new NullPointerException("Null getBorder");
    }
    this.getBorder = getBorder;
  }

  @Override
  ImmutableList<ImmutableList<Point>> getPoints() {
    return getPoints;
  }

  @Override
  ImmutableList<Point> getBorder() {
    return getBorder;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AgvModel.Builder) {
      AgvModel.Builder that = (AgvModel.Builder) o;
      return (this.getPoints.equals(that.getPoints()))
          && (this.getBorder.equals(that.getBorder()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= getPoints.hashCode();
    h *= 1000003;
    h ^= getBorder.hashCode();
    return h;
  }

  private static final long serialVersionUID = -8527252625057713751L;
}
