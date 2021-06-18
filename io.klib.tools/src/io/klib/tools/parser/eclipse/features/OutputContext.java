package io.klib.tools.parser.eclipse.features;

import java.nio.file.Path;
import java.util.List;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;

public class OutputContext {

	private OutputStrategy strategy;

	public OutputStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(OutputStrategy strategy) {
		this.strategy = strategy;
	}

	public void execute(List<Feature> features, Path outputPath) {
		strategy.execute(features, outputPath);
	}
}
