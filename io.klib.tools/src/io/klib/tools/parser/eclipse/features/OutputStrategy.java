package io.klib.tools.parser.eclipse.features;

import java.nio.file.Path;
import java.util.List;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;

public interface OutputStrategy {

	public static final String FEATURE_PREFIX = "fea_";
	public static final String PLATFORM_PREFIX = "PLATFORM_";

	public void execute(List<Feature> features, Path outputPath);
}
