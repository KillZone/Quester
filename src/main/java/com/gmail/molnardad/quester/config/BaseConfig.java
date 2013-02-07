package com.gmail.molnardad.quester.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.managers.DataManager;

public class BaseConfig extends CustomConfig {

	public BaseConfig(Quester plugin, String fileName) {
		super(plugin, fileName);
	}
	
	private void wrongConfig(String path) {
		Quester.log.info("Invalid or missing value in config: " + path.replace('.', ':') + ". Setting to default.");
	}
	
	private void checkBoolean(String path) {
		if(this.config.getString(path) != "true" && this.config.getString(path) != "false") {
			this.config.set(path, true);
			wrongConfig(path);
		}
	}
	
	@Override
	public void initialize() {
		
		String path;
		String temp;
		// VERBOSE-LOGGING
		path = "general.verbose-logging";
		checkBoolean(path);
		DataManager.verbose = this.config.getBoolean(path);

		// SAVE INTERVAL
		path = "general.save-interval";
		if(this.config.getInt(path) < 0) {
			this.config.set(path, 15);
			wrongConfig(path);
		}
		DataManager.saveInterval = this.config.getInt(path);
		
		// DEBUG INFO
		path = "general.debug-info";
		checkBoolean(path);
		DataManager.debug = this.config.getBoolean(path);
		
		// SHOW ONLY CURRENT
		path = "objectives.show-only-current";
		checkBoolean(path);
		DataManager.ordOnlyCurrent = this.config.getBoolean(path);
		
		// BREAK NO DROPS
		path = "objectives.break.no-drops";
		checkBoolean(path);
		DataManager.brkNoDrops = this.config.getBoolean(path);
		
		// BREAK SUBTRACT ON PLACE
		path = "objectives.break.subtract-on-place";
		checkBoolean(path);
		DataManager.brkSubOnPlace = this.config.getBoolean(path);
		
		// COLLECT REMOVE ON PICKUP
		path = "objectives.collect.remove-on-pickup";
		checkBoolean(path);
		DataManager.colRemPickup = this.config.getBoolean(path);
				
		// COLLECT SUBTRACT ON DROP
		path = "objectives.collect.subtract-on-drop";
		checkBoolean(path);
		DataManager.colSubOnDrop = this.config.getBoolean(path);
		
		// MAX QUESTS
		path = "quests.max-amount";
		if(this.config.getInt(path) < 1) {
			this.config.set(path, 1);
			wrongConfig(path);
		}
		DataManager.maxQuests = this.config.getInt(path);
		
		// PROGRES MESSAGES
		path = "quests.messages.start-show";
		checkBoolean(path);
		DataManager.progMsgStart = this.config.getBoolean(path);
		
		path = "quests.messages.cancel-show";
		checkBoolean(path);
		DataManager.progMsgCancel = this.config.getBoolean(path);
		
		path = "quests.messages.done-show";
		checkBoolean(path);
		DataManager.progMsgDone = this.config.getBoolean(path);
		
		path = "quests.messages.objective-show";
		checkBoolean(path);
		DataManager.progMsgObj = this.config.getBoolean(path);
	
		// COMMANDS
		
		path = "commands.displayed-cmd";
		temp = this.config.getString(path, "");
		if(!temp.equals("/q") && !temp.equals("/quest") && !temp.equals("/quester")) {
			this.config.set(path, "/q");
			wrongConfig(path);
		}
		DataManager.displayedCmd = this.config.getString(path);
		
		path = "commands.world-label-this";
		temp = this.config.getString(path, "");
		if(temp.isEmpty()) {
			this.config.set(path, "this");
			wrongConfig(path);
		}
		DataManager.worldLabelThis = this.config.getString(path);
		
		path = "commands.loc-label-here";
		temp = this.config.getString(path, "");
		if(temp.isEmpty()) {
			this.config.set(path, "here");
			wrongConfig(path);
		}
		DataManager.locLabelHere = this.config.getString(path);
		
		path = "commands.loc-label-player";
		temp = this.config.getString(path, "");
		if(temp.isEmpty()
				|| temp.equalsIgnoreCase(DataManager.locLabelHere)) {
			this.config.set(path, "player");
			wrongConfig(path);
		}
		DataManager.locLabelPlayer = this.config.getString(path);
		
		path = "commands.loc-label-block";
		temp = this.config.getString(path, "");
		if(temp.isEmpty() 
				|| temp.equalsIgnoreCase(DataManager.locLabelHere)
				|| temp.equalsIgnoreCase(DataManager.locLabelPlayer)) {
			this.config.set(path, "block");
			wrongConfig(path);
		}
		DataManager.locLabelPlayer = this.config.getString(path);
		
		// QUESTER RANKS
		
		Map<Integer, String> rankMap = new HashMap<Integer, String>();
		List<Integer> sortedRanks = new ArrayList<Integer>();
		
		ConfigurationSection ranks = this.config.getConfigurationSection("ranks");
		if(ranks != null) {
			for(String key : ranks.getKeys(false)) {
				rankMap.put(ranks.getInt(key), key.replace('-', ' '));
				sortedRanks.add(ranks.getInt(key));
			}
		}
		if(sortedRanks.size() == 0) {
			wrongConfig("ranks");
			this.config.set("ranks.Quester", 0);
			rankMap.put(0, "Quester");
			sortedRanks.add(0);
			this.config.set("ranks.Apprentice-Quester", 25);
			rankMap.put(25, "Apprentice Quester");
			sortedRanks.add(25);
			this.config.set("ranks.Master-Quester", 50);
			rankMap.put(50, "Master Quester");
			sortedRanks.add(50);
		}
		Collections.sort(sortedRanks);
		//DataManager.ranks = rankMap;
		//DataManager.sortedRanks = sortedRanks;
		
		
		saveConfig();
	}

}
