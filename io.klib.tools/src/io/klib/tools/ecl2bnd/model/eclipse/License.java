package io.klib.tools.ecl2bnd.model.eclipse;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType
public class License {
	@XmlAttribute
	private String url;

	@XmlValue
	private String license;

	public String getUrl() {
		return url;
	}

	public String getLicense() {
		return license;
	}

}
