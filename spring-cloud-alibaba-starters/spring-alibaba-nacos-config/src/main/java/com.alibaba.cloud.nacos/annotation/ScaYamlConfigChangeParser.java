/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.annotation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.client.config.impl.YmlChangeParser;
import com.alibaba.nacos.common.utils.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public class ScaYamlConfigChangeParser extends YmlChangeParser {
	private static final String INVALID_CONSTRUCTOR_ERROR_INFO = "could not determine a constructor for the tag";

	public ScaYamlConfigChangeParser() {
		super();
	}

	@Override
	public Map<String, ConfigChangeItem> doParse(String oldContent, String newContent, String type) {
		Map<String, Object> oldMap = Collections.emptyMap();
		Map<String, Object> newMap = Collections.emptyMap();
		try {
			Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
			if (StringUtils.isNotBlank(oldContent)) {
				oldMap = yaml.load(oldContent);
				oldMap = getFlattenedMap(oldMap);
			}
			if (StringUtils.isNotBlank(newContent)) {
				newMap = yaml.load(newContent);
				newMap = getFlattenedMap(newMap);
			}
		}
		catch (MarkedYAMLException e) {
			handleYamlException(e);
		}

		return filterChangeData(oldMap, newMap);
	}

	private void handleYamlException(MarkedYAMLException e) {
		if (e.getMessage().startsWith(INVALID_CONSTRUCTOR_ERROR_INFO) || e instanceof ComposerException) {
			throw new NacosRuntimeException(NacosException.INVALID_PARAM,
					"AbstractConfigChangeListener only support basic java data type for yaml. If you want to listen "
							+ "key changes for custom classes, please use `Listener` to listener whole yaml configuration and parse it by yourself.",
					e);
		}
		throw e;
	}

	private Map<String, Object> getFlattenedMap(Map<String, Object> source) {
		Map<String, Object> result = new LinkedHashMap<>(128);
		buildFlattenedMap(result, source, null);
		return result;
	}

	private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
		for (Iterator<Map.Entry<String, Object>> itr = source.entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry<String, Object> e = itr.next();
			String key = String.valueOf(e.getKey());
			if (StringUtils.isNotBlank(path)) {
				if (key.startsWith("[")) {
					key = path + key;
				}
				else {
					key = path + '.' + key;
				}
			}
			if (e.getValue() instanceof String) {
				result.put(key, e.getValue());
			}
			else if (e.getValue() instanceof Map) {
				@SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) e.getValue();
				buildFlattenedMap(result, map, key);
			}
			else if (e.getValue() instanceof Collection) {
				@SuppressWarnings("unchecked") Collection<Object> collection = (Collection<Object>) e.getValue();
				if (collection.isEmpty()) {
					result.put(key, "");
				}
				else {
					int count = 0;
					for (Object object : collection) {
						buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
					}
				}
			}
			else {
				result.put(key, (e.getValue() != null ? e.getValue() : ""));
			}
		}
	}

}
