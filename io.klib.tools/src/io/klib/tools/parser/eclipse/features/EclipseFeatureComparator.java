package io.klib.tools.parser.eclipse.features;

import java.util.Comparator;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;

public class EclipseFeatureComparator implements Comparator<Feature> {

	@Override
	public int compare(Feature f1, Feature f2) {
		return f1.getId().compareTo(f2.getId());
	}

}
