package io.klib.tools.ecl2bnd.model.bnd;

import java.util.Hashtable;

import io.klib.tools.ecl2bnd.model.eclipse.Plugin;

public class BndBundle {
	private String id;
	private String version;
	private Hashtable<String, String> req = new Hashtable<>();

	public BndBundle(Plugin plugin) {
		this.setId(plugin.getId());
		this.setVersion(plugin.getVersion());

		if (plugin.getOs() != null) {
			req.put("os", plugin.getOs());
		}
		if (plugin.getWs() != null) {
			req.put("ws", plugin.getWs());
		}
		if (plugin.getOs() != null) {
			req.put("arch", plugin.getArch());
		}
		if (plugin.getNl() != null) {
			req.put("nl", plugin.getNl());
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
		String os = (req.contains("os")) ? "os='" + req.get("os") + "'" : "";
		String ws = (req.contains("ws")) ? "ws='" + req.get("ws") + "'" : "";
		String arch = (req.contains("arch")) ? "arch='" + req.get("arch") + "'" : "";
		String nl = (req.contains("nl")) ? "nl='" + req.get("nl") + "'" : "";
		return String.format("<bundle id='%s' version='%s' %s %s %s %s", id, version, os, ws, arch, nl);
	}
}
