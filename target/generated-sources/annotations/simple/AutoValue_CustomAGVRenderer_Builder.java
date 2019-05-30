package simple;


@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_CustomAGVRenderer_Builder extends CustomAGVRenderer.Builder {
  private final CustomAGVRenderer.Language language;

  AutoValue_CustomAGVRenderer_Builder(
      CustomAGVRenderer.Language language) {
    if (language == null) {
      throw new NullPointerException("Null language");
    }
    this.language = language;
  }

  @Override
  CustomAGVRenderer.Language language() {
    return language;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof CustomAGVRenderer.Builder) {
      CustomAGVRenderer.Builder that = (CustomAGVRenderer.Builder) o;
      return (this.language.equals(that.language()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= language.hashCode();
    return h;
  }

  private static final long serialVersionUID = -1772420262312399129L;
}
