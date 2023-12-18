package io.klib.tools.ecl2bnd.model.bnd;

import java.util.Collections;
import java.util.TreeSet;

import io.klib.tools.ecl2bnd.model.eclipse.Feature;
import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class BndFeature {

	private String id;
	private String version;

	private TreeSet<BndFeature> includedFeatures = new TreeSet<>();
	private TreeSet<BndBundle> bundles = new TreeSet<>();

	public BndFeature(Feature feature) {
		this.setId(feature.getId());
		this.setVersion(feature.getVersion());
		for (Plugin plugin : emptyIfNull(feature.getPlugins())) {
			bundles.add(new BndBundle(plugin));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return String.format("<feature id='%s' version='%s'>", id, version);
	}

	public TreeSet<BndFeature> getIncludedFeatures() {
		return includedFeatures;
	}

	public void setIncludedFeatures(TreeSet<BndFeature> includedFeatures) {
		this.includedFeatures = includedFeatures;
	}

	public TreeSet<BndBundle> getPlugins() {
		return bundles;
	}

	public void setPlugins(TreeSet<BndBundle> bundles) {
		this.bundles = bundles;
	}

	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
		return iterable == null ? Collections.<T>emptyList() : iterable;
	}
}
