package com.gmail.molnardad.quester.elements;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.SerializationException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.molnardad.quester.QConfiguration;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.storage.StorageKey;
import com.gmail.molnardad.quester.utils.Util;

public abstract class Objective extends Element {
	
	private String desc = "";
	private boolean hidden = false;
	private final Set<Integer> prerequisites = new HashSet<Integer>();
	
	public Set<Integer> getPrerequisites() {
		return prerequisites;
	}
	
	public void addPrerequisity(final int newPre) {
		prerequisites.add(newPre);
	}
	
	public void removePrerequisity(final int pre) {
		prerequisites.remove(pre);
	}
	
	private String coloredDesc() {
		String des = "";
		if(!prerequisites.isEmpty()) {
			des += " PRE: " + Util.serializePrerequisites(prerequisites, ",");
		}
		if(!desc.isEmpty()) {
			des += "\n  - " + ChatColor.translateAlternateColorCodes('&', desc) + ChatColor.RESET;
		}
		return des;
	}
	
	public void addDescription(final String msg) {
		desc += (" " + msg).trim();
	}
	
	public void removeDescription() {
		desc = "";
	}
	
	public void setHidden(final boolean value) {
		hidden = value;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public final boolean isComplete(final int progress) {
		return progress >= getTargetAmount();
	}
	
	public abstract int getTargetAmount();
	
	protected String parseDescription(final String description) {
		return description;
	}
	
	protected abstract String show(int progress);
	
	protected abstract String info();
	
	public boolean tryToComplete(final Player player) {
		return false;
	}
	
	public String inShow() {
		return inShow(0);
	}
	
	public String inShow(final int progress) {
		if(!desc.isEmpty()) {
			final String partiallyParsed = desc.replaceAll("%r", String.valueOf(getTargetAmount() - progress)).replaceAll("%t", String.valueOf(getTargetAmount())).replaceAll("%a", String.valueOf(progress));
			return ChatColor.translateAlternateColorCodes('&', parseDescription(partiallyParsed));
		}
		return show(progress);
	}
	
	public String inInfo() {
		return getType() + ": " + info() + coloredDesc();
	}
	
	@Override
	public final String toString() {
		return "Objective (type=" + getType() + ")";
	}
	
	protected abstract void save(StorageKey key);
	
	public final void serialize(final StorageKey key) {
		final String type = getType();
		if(type.isEmpty()) {
			throw new SerializationException("Unknown type");
		}
		save(key);
		key.setString("type", type);
		if(!desc.isEmpty()) {
			key.setString("description", desc);
		}
		if(!prerequisites.isEmpty()) {
			key.setString("prerequisites", Util.serializePrerequisites(prerequisites));
		}
		if(hidden) {
			key.setBoolean("hidden", hidden);
		}
	}
	
	public static final Objective deserialize(final StorageKey key) {
		if(!key.hasSubKeys()) {
			Quester.log.severe("Objective deserialization error: no subkeys");
			return null;
		}
		Objective obj = null;
		String type = null, des = null;
		boolean hid = false;
		Set<Integer> prereq = new HashSet<Integer>();
		
		type = key.getString("type");
		if(type == null) {
			Quester.log.severe("Objective type missing.");
			return null;
		}
		des = key.getString("description", null);
		hid = key.getBoolean("hidden", false);
		try {
			prereq = Util.deserializePrerequisites(key.getString("prerequisites"));
		}
		catch (final Exception ignore) {}
		
		final Class<? extends Objective> c = ElementManager.getInstance().getObjectiveClass(type);
		if(c != null) {
			try {
				final Method load = c.getDeclaredMethod("load", StorageKey.class);
				load.setAccessible(true);
				obj = (Objective) load.invoke(null, key);
				if(obj == null) {
					return null;
				}
				if(des != null) {
					obj.addDescription(des);
				}
				if(hid) {
					obj.setHidden(true);
				}
				if(!prereq.isEmpty()) {
					for(final int i : prereq) {
						obj.addPrerequisity(i);
					}
				}
			}
			catch (final Exception e) {
				Quester.log.severe("Error when deserializing " + c.getSimpleName() + ". Method load() missing or invalid. " + e.getClass().getName());
				if(QConfiguration.debug) {
					e.printStackTrace();
				}
				return null;
			}
		}
		else {
			Quester.log.severe("Unknown objective type: '" + type + "'");
		}
		
		return obj;
	}
}
