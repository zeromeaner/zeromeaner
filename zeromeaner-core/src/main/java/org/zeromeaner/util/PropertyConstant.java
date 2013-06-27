package org.zeromeaner.util;

import java.util.regex.Pattern;

public final class PropertyConstant {
	public static boolean is(Constant<Boolean> c) {
		return c.value();
	}
	
	public static final ConstantParser<Boolean> BOOLEAN = new AbstractConstantParser<Boolean>("true|false") {
		@Override
		protected Boolean parseNonNull(String stringValue, Boolean defaultValue) {
			return Boolean.parseBoolean(stringValue);
		}

		@Override
		protected String renderNonNull(Boolean value) {
			return "" + value;
		}
	};
	
	public static final ConstantParser<Integer> INTEGER = new AbstractConstantParser<Integer>("\\d+") {
		@Override
		protected Integer parseNonNull(String stringValue, Integer defaultValue) {
			try {
				return Integer.parseInt(stringValue);
			} catch(NumberFormatException nfe) {
				return defaultValue;
			}
		}

		@Override
		protected String renderNonNull(Integer value) {
			return "" + value;
		}
	};
	
	public static final ConstantParser<Double> DOUBLE = new AbstractConstantParser<Double>("\\d+\\.?|\\d*\\.\\d+") {
		@Override
		protected Double parseNonNull(String stringValue, Double defaultValue) {
			try {
				return Double.parseDouble(stringValue);
			} catch(NumberFormatException nfe) {
				return defaultValue;
			}
		}

		@Override
		protected String renderNonNull(Double value) {
			return "" + value;
		}
	};
	
	public static final ConstantParser<String> STRING = new AbstractConstantParser<String>() {
		@Override
		protected String parseNonNull(String stringValue, String defaultValue) {
			return stringValue;
		}

		@Override
		protected String renderNonNull(String value) {
			return value;
		}
	};

	public static interface ConstantParser<T> {
		public T parse(String stringValue, T defaultValue);
		public String render(T value);
	}
	
	public static abstract class AbstractConstantParser<T> implements ConstantParser<T> {
		protected static Pattern EVERYTHING = Pattern.compile(".*");
		
		protected Pattern matches;
		
		protected abstract T parseNonNull(String stringValue, T defaultValue);
		protected abstract String renderNonNull(T value);
		
		public AbstractConstantParser() {
			this(EVERYTHING);
		}
		
		public AbstractConstantParser(String regex) {
			this(Pattern.compile(regex));
		}
		
		public AbstractConstantParser(Pattern matches) {
			this.matches = matches;
		}
		
		@Override
		public T parse(String stringValue, T defaultValue) {
			if(stringValue == null || !matches.matcher(stringValue).matches())
				return defaultValue;
			return parseNonNull(stringValue, defaultValue);
		}

		@Override
		public String render(T value) {
			if(value == null)
				return null;
			return renderNonNull(value);
		}
		
	}
	

	public static class Constant<T> {
		private CustomProperties backing;
		private ConstantParser<T> parser;
		private String key;
		private T defaultValue;
		
		public Constant(ConstantParser<T> parser, String key) {
			this(parser, key, null);
		}
		
		public Constant(ConstantParser<T> parser, String key, T defaultValue) {
			this(null, parser, key, defaultValue);
		}
		
		public Constant(CustomProperties backing, ConstantParser<T> parser, String key, T defaultValue) {
			this.backing = backing;
			this.parser = parser;
			this.key = key;
			this.defaultValue = defaultValue;
		}
		
		public Constant(CustomProperties backing, ConstantParser<T> parser, String key) {
			this(backing, parser, key, null);
		}
		
		public String key() {
			return key;
		}
		
		public T defaultValue() {
			return defaultValue;
		}
		
		public T value() {
			return value(null);
		}
		
		public T value(CustomProperties backing) {
			if(backing == null)
				backing = this.backing;
			return parser.parse(backing.getProperty(key), defaultValue);
		}
		
		public void set(T value) {
			set(null, value);
		}
		
		public void set(CustomProperties backing, T value) {
			if(backing == null)
				backing = this.backing;
			String pval = parser.render(value);
			if(pval != null)
				backing.setProperty(key, pval);
			else
				backing.removeProperty(key);
		}
	}


	private PropertyConstant() {
		
	}
}
