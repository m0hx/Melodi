package com.ga.melodi.mailing;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractEmailContext {

	private String from;
	private String to;
	private String subject;
	private String templateLocation;
	private Map<String, Object> context = new HashMap<>();

	public <T> void init(T ignored) {}

	public Object put(String key, Object value) {
		return key == null ? null : context.put(key, value);
	}
}
