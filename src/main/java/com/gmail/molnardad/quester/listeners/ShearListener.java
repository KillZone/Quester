package com.gmail.molnardad.quester.listeners;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.gmail.molnardad.quester.ActionSource;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.elements.Objective;
import com.gmail.molnardad.quester.objectives.ShearObjective;
import com.gmail.molnardad.quester.profiles.PlayerProfile;
import com.gmail.molnardad.quester.profiles.ProfileManager;
import com.gmail.molnardad.quester.quests.Quest;

public class ShearListener implements Listener {
	
	private final ProfileManager profMan;
	
	public ShearListener(final Quester plugin) {
		profMan = plugin.getProfileManager();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onShear(final PlayerShearEntityEvent event) {
		if(event.getEntity().getType() == EntityType.SHEEP) {
			final Player player = event.getPlayer();
			final Sheep sheep = (Sheep) event.getEntity();
			final PlayerProfile prof = profMan.getProfile(player.getName());
			final Quest quest = prof.getQuest();
			if(quest != null) {
				if(!quest.allowedWorld(player.getWorld().getName().toLowerCase())) {
					return;
				}
				final List<Objective> objs = quest.getObjectives();
				for(int i = 0; i < objs.size(); i++) {
					if(objs.get(i).getType().equalsIgnoreCase("SHEAR")) {
						if(!profMan.isObjectiveActive(prof, i)) {
							continue;
						}
						final ShearObjective obj = (ShearObjective) objs.get(i);
						if(obj.check(sheep.getColor())) {
							profMan.incProgress(player, ActionSource.listenerSource(event), i);
							return;
						}
					}
				}
			}
		}
	}
}
