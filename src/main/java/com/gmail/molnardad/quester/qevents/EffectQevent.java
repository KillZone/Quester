package com.gmail.molnardad.quester.qevents;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.elements.QElement;
import com.gmail.molnardad.quester.elements.Qevent;
import com.gmail.molnardad.quester.storage.StorageKey;
import com.gmail.molnardad.quester.utils.Ql;
import com.gmail.molnardad.quester.utils.SerUtils;

@QElement("EFFECT")
public class EffectQevent extends Qevent {
	
	private final PotionEffect effect;
	
	public EffectQevent(final PotionEffect eff) {
		effect = eff;
	}
	
	@Override
	public String info() {
		return effect.getType().getName() + "; DUR: " + effect.getDuration() / 20 + "s; AMP: "
				+ effect.getAmplifier();
	}
	
	@Override
	protected void run(final Player player, final Quester plugin) {
		player.addPotionEffect(effect, true);
	}
	
	@QCommand(min = 1, max = 1, usage = "{<potion effect>}")
	public static Qevent fromCommand(final QCommandContext context) {
		final PotionEffect eff = SerUtils.parseEffect(context.getString(0));
		return new EffectQevent(eff);
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.setString("effect", SerUtils.serializeEffect(effect));
	}
	
	protected static Qevent load(final StorageKey key) {
		PotionEffect eff;
		
		if(key.getString("effect") != null) {
			try {
				eff = SerUtils.parseEffect(key.getString("effect", ""));
			}
			catch (final IllegalArgumentException e) {
				Ql.severe("Error deserializing effect event: "
						+ ChatColor.stripColor(e.getMessage()));
				return null;
			}
		}
		else {
			return null;
		}
		return new EffectQevent(eff);
	}
}
