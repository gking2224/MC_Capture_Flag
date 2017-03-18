package me.gking2224.mc.mod.ctf.game;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class GameOptions {

	private static final char SEPARATOR_CHAR = ';';
	private static final GameOptions DEFAULT = new GameOptions();

	private String internalString = null;

	static {
		DEFAULT.setOption("size", 1);
	}

	public static GameOptions getDefault() {
		return DEFAULT;
	}

	private void setOption(String key, Object value) {
		this.options.put(key, value);
	}

	public String toString() {
		if (internalString == null) {
			StringBuffer buf = new StringBuffer();
			this.options.forEach((k,v) -> {
				if (Boolean.class.isAssignableFrom(v.getClass())) {
					String key = k;
					if (!((Boolean)v)) {
						key = "!"+k;
					}
					buf.append(key);
				}
				else {
					buf.append(String.format("%s=%s;", k, v));
				}
			});
			internalString = buf.length() > 0 ? buf.substring(0,  buf.length() - 1) : buf.toString();
		}
		return internalString;
	}
	
	public GameOptions(String optionsStr) {
		this.options = parseOptionsString(optionsStr);
	}

	private Map<String, Object> options = new HashMap<String, Object>();

	private GameOptions() {
	}

	private GameOptions(Map<String, Object> options) {
		this.options = options;
	}

	public GameOptions update(String string) {
		return putOptions(parseOptionsString(string));
	}

	private GameOptions putOptions(Map<String, Object> options) {
		Map<String, Object> mergedOptions = new HashMap<String, Object>(this.options);
		mergedOptions.putAll(options);
		return new GameOptions(mergedOptions);
	}

	private static Map<String, Object> parseOptionsString(String optionsStr) {
		Map<String, Object> rv = new HashMap<String, Object>();
		for (String option : StringUtils.split(optionsStr, SEPARATOR_CHAR)) {
			String op = option.trim();
			if (op.indexOf('=') != -1) {
				String[] kvp = op.split("=");
				rv.put(kvp[0].trim(), kvp[1].trim());
			} else {
				rv.put(op, !op.startsWith("!"));
			}
		}
		return rv;
	}

	public Optional<Boolean> getBoolean(String key) {
		return getTypedOption(key, Boolean.class);
	}

	public Optional<String> getString(String key) {
		return getTypedOption(key, String.class);
	}

	public Optional<Double> getDouble(String key) {
		Optional<String> stringOpt = getTypedOption(key, String.class);
		return (stringOpt.isPresent()) ? Optional.of(Double.parseDouble(stringOpt.get()))
				: Optional.ofNullable((Double) null);
	}

	public Optional<Integer> getInteger(String key) {
		Optional<String> stringOpt = getTypedOption(key, String.class);
		return (stringOpt.isPresent()) ? Optional.of(Integer.parseInt(stringOpt.get()))
				: Optional.ofNullable((Integer) null);
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> getTypedOption(String key, Class<T> clazz) {
		Object value = options.get(key);
		if (value != null && !clazz.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException(
					format("%s value requested for key %s, but it is not a String", clazz, key));
		}
		return Optional.ofNullable((T) value);
	}
}
