package io.klib.tools.ecl2bnd.model.eclipse;

import java.util.Comparator;

public class EclipseFeatureComparator implements Comparator<Feature> {

	@Override
	public int compare(Feature f1, Feature f2) {
		return f1.getId().compareTo(f2.getId());
	}

}
