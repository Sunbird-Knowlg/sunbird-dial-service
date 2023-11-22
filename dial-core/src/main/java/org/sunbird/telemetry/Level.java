package org.sunbird.telemetry;

public enum Level {
 INFO, WARN, DEBUG, ERROR, TRACE, FATAL;
	
	public static Level getLevel(String level) {
		return Level.valueOf(level);
	}
}

