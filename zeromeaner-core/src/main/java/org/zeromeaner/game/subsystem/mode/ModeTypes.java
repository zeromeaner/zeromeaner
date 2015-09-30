package org.zeromeaner.game.subsystem.mode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModeTypes {
	public ModeType[] value() default {ModeType.SINGLE};
	
	public static enum ModeType {
		HIDDEN,
		SINGLE,
		NET,
		VS,
		RACE,
		;
		
		public static Predicate<Class<? extends GameMode>> require(final ModeType... reqd) {
			return require(reqd, new ModeType[0]);
		}

		public static Predicate<Class<? extends GameMode>> forbid(final ModeType... forb) {
			return require(new ModeType[0], forb);
		}
		
		public static Predicate<Class<? extends GameMode>> require(final ModeType[] reqd, final ModeType[] forb) {
			return (value) -> {
				List<ModeType> found = Collections.emptyList();
				
				if(value.isAnnotationPresent(ModeTypes.class)) {
					found = Arrays.asList(value.getAnnotation(ModeTypes.class).value());
				}
				
				for(ModeType mt : reqd) {
					if(!found.contains(mt))
						return false;
				}
				
				for(ModeType mt : forb) {
					if(found.contains(mt))
						return false;
				}
				
				return true;
			};
		}
		
	}
}
