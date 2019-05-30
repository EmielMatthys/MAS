package experiment.common;


@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_RoutePanel_Builder extends RoutePanel.Builder {
  private final int preferredPosition;

  AutoValue_RoutePanel_Builder(
      int preferredPosition) {
    this.preferredPosition = preferredPosition;
  }

  @Override
  int preferredPosition() {
    return preferredPosition;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RoutePanel.Builder) {
      RoutePanel.Builder that = (RoutePanel.Builder) o;
      return (this.preferredPosition == that.preferredPosition());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= preferredPosition;
    return h;
  }

  private static final long serialVersionUID = -5277450398730005609L;
}
