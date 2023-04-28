package io.klib.tools.ecl2bnd.model.eclipse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Plugin {

	@XmlAttribute
	String id;

	@XmlAttribute(name = "download-size")
	@XmlSchemaType(name = "integer")
	int downloadSize;

	@XmlAttribute(name = "install-size")
	@XmlSchemaType(name = "integer")
	int installSize;

	@XmlAttribute
	String version;

	@XmlAttribute
	@XmlSchemaType(name = "boolean")
	boolean unpack;

	@XmlAttribute
	String os;

	@XmlAttribute
	String ws;

	@XmlAttribute
	String arch;

	@XmlAttribute
	String nl;

	@XmlAttribute
	@XmlSchemaType(name = "boolean")
	boolean fragment;

	public String getId() {
		return id;
	}

	public int getDownloadSize() {
		return downloadSize;
	}

	public int getInstallSize() {
		return installSize;
	}

	public String getVersion() {
		return version;
	}

	public boolean isUnpack() {
		return unpack;
	}

	public String getOs() {
		return os;
	}

	public String getWs() {
		return ws;
	}

	public String getArch() {
		return arch;
	}

	public String getNl() {
		return nl;
	}

	public boolean isFragment() {
		return fragment;
	}

}
