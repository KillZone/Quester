package com.gmail.molnardad.quester.listeners;

import java.util.ArrayList;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import com.gmail.molnardad.quester.QuestManager;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.objectives.Objective;
import com.gmail.molnardad.quester.objectives.TameObjective;

public class TameListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTame(EntityTameEvent event) {
		if(event.getOwner() instanceof Player) {
		    QuestManager qm = Quester.qMan;
		    Player player = (Player) event.getOwner();
			if(qm.hasQuest(player.getName())) {
		    	ArrayList<Objective> objs = qm.getPlayerQuest(player.getName()).getObjectives();
		    	for(int i = 0; i < objs.size(); i++) {
		    		if(objs.get(i).getType().equalsIgnoreCase("TAME")) {
			    		if(qm.achievedTarget(player, i)){
		    				continue;
		    			}
			    		EntityType ent = event.getEntityType();
		    			TameObjective obj = (TameObjective)objs.get(i);
		    			if(obj.check(ent)) {
		    				qm.incProgress(player, i);
		    				return;
		    			}
		    		}
		    	}
			}
		}
	}
}
