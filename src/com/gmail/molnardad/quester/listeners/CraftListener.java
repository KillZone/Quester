package com.gmail.molnardad.quester.listeners;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.molnardad.quester.QuestManager;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.objectives.CraftObjective;
import com.gmail.molnardad.quester.objectives.Objective;


public class CraftListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraft(CraftItemEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			QuestManager qm = Quester.qMan;
		    Player player = (Player) event.getWhoClicked();
		    if(qm.hasQuest(player.getName())) {
		    	ArrayList<Objective> objs = qm.getPlayerQuest(player.getName()).getObjectives();
		    	for(int i = 0; i < objs.size(); i++) {
		    		// check if Objective is type CRAFT
		    		if(objs.get(i).getType().equalsIgnoreCase("CRAFT")) {
			    		if(qm.achievedTarget(player, i)){
		    				continue;
		    			}
		    			CraftObjective obj = (CraftObjective)objs.get(i);
		    			ItemStack item = event.getCurrentItem();
		    			ItemStack inHand = event.getCursor();
		    			if(obj.check(item) && obj.checkHand(inHand, item)) {
		    				qm.incProgress(player, i, item.getAmount());
		    				return;
		    			}
		    		}
		    	}
		    	
		    }
		} else {
			return;
		}
	}
	
}
