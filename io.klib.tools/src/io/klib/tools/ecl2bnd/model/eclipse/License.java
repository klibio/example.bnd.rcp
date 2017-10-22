package io.klib.tools.ecl2bnd.model.eclipse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
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
