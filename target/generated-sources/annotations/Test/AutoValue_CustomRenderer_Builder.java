package Test;


@javax.annotation.Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_CustomRenderer_Builder extends CustomRenderer.Builder {
  private final CustomRenderer.Language language;

  AutoValue_CustomRenderer_Builder(
      CustomRenderer.Language language) {
    if (language == null) {
      throw new NullPointerException("Null language");
    }
    this.language = language;
  }

  @Override
  CustomRenderer.Language language() {
    return language;
  }


  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof CustomRenderer.Builder) {
      CustomRenderer.Builder that = (CustomRenderer.Builder) o;
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
