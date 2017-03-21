package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class GameOptions {

  private static final char SEPARATOR_CHAR = ';';
  private static final GameOptions DEFAULT = new GameOptions();

  public static GameOptions getDefault() {
    return DEFAULT;
  }

  private static Map<String, Object> parseOptionsString(String optionsStr) {
    final Map<String, Object> rv = new HashMap<String, Object>();
    for (final String option : StringUtils.split(optionsStr, SEPARATOR_CHAR)) {
      final String op = option.trim();
      if (op.indexOf('=') != -1) {
        final String[] kvp = op.split("=");
        rv.put(kvp[0].trim(), kvp[1].trim());
      } else {
        final boolean b = !op.startsWith("!");
        rv.put(b ? op : op.substring(1), b);
      }
    }
    return rv;
  }

  private String internalString = null;

  private Map<String, Object> options = new HashMap<String, Object>();

  private GameOptions() {}

  private GameOptions(Map<String, Object> options) {
    this.options = options;
  }

  public GameOptions(String optionsStr) {
    this.options = parseOptionsString(optionsStr);
  }

  public Optional<Boolean> getBoolean(String key) {
    return this.getTypedOption(key, Boolean.class);
  }

  public Optional<Double> getDouble(String key) {
    final Optional<String> stringOpt = this.getTypedOption(key, String.class);
    return (stringOpt.isPresent())
            ? Optional.of(Double.parseDouble(stringOpt.get()))
            : Optional.ofNullable((Double) null);
  }

  public Optional<Integer> getInteger(String key) {
    final Optional<String> stringOpt = this.getTypedOption(key, String.class);
    return (stringOpt.isPresent())
            ? Optional.of(Integer.parseInt(stringOpt.get()))
            : Optional.ofNullable((Integer) null);
  }

  public Optional<String> getString(String key) {
    return this.getTypedOption(key, String.class);
  }

  @SuppressWarnings("unchecked") private <T> Optional<T> getTypedOption(
    String key, Class<T> clazz)
  {
    final Object value = this.options.get(key);
    if (value != null && !clazz.isAssignableFrom(
            value.getClass())) { throw new IllegalArgumentException(
                    format("%s value requested for key %s, but it is not a String",
                            clazz, key)); }
    return Optional.ofNullable((T) value);
  }

  private GameOptions putOptions(Map<String, Object> options) {
    final Map<String, Object> mergedOptions = new HashMap<String, Object>(
            this.options);
    mergedOptions.putAll(options);
    return new GameOptions(mergedOptions);
  }

  @Override public String toString() {
    if (this.internalString == null) {
      final StringBuffer buf = new StringBuffer();
      this.options.forEach((k, v) -> {
        if (Boolean.class.isAssignableFrom(v.getClass())) {
          String key = k;
          if (!((Boolean) v)) {
            key = "!" + k;
          }
          buf.append(key);
        } else {
          buf.append(String.format("%s=%s;", k, v));
        }
      });
      this.internalString = buf.length() > 0
              ? buf.substring(0, buf.length() - 1) : buf.toString();
    }
    return this.internalString;
  }

  public GameOptions update(String string) {
    return this.putOptions(parseOptionsString(string));
  }
}
