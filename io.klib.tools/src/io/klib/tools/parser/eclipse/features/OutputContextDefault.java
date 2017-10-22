package io.klib.tools.parser.eclipse.features;

import java.util.List;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;

public abstract class OutputContextDefault implements OutputStrategy {
	public StringBuffer tocHeader;
	public StringBuffer featureExpression;

	public OutputContextDefault() {
		tocHeader = new StringBuffer();
		featureExpression = new StringBuffer();
	}

	protected StringBuffer parseFeatureIncludeSection(Feature f) {
		StringBuffer featureIncludes = new StringBuffer();
		List<Feature> includes = f.getIncludes();
		if (includes != null) {
			includes.stream().sorted(new EclipseFeatureComparator()).forEach(i -> {
				String id = i.getId();
				String version = i.getVersion();
				featureIncludes.append(String.format("    ${%s%s_%s},\\\n", FEATURE_PREFIX, id, version));
			});
			// remove the trailing ",\\n"
			featureIncludes.replace(featureIncludes.length() - 3, featureIncludes.length(), "");

		}
		return featureIncludes;
	}
}