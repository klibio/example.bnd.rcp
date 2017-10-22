package io.klib.tools.ecl2bnd.model.eclipse;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Feature {

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String label;

	@XmlAttribute
	private String version;

	@XmlElement
	private String description;

	@XmlElement
	private String copyright;

	@XmlElement
	private License license;

	@XmlElement(name = "includes")
	private List<Feature> includes;

	@XmlElement(name = "plugin")
	private List<Plugin> plugins;

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	public String getCopyright() {
		return copyright;
	}

	public License getLicense() {
		return license;
	}

	public List<Feature> getIncludes() {
		return includes;
	}

	public List<Plugin> getPlugins() {
		return plugins;
	}

}
