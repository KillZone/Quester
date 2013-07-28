package com.gmail.molnardad.quester.qevents;

import static com.gmail.molnardad.quester.utils.Util.parseEnchants;
import static com.gmail.molnardad.quester.utils.Util.parseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.commandbase.QCommand;
import com.gmail.molnardad.quester.commandbase.QCommandContext;
import com.gmail.molnardad.quester.commandbase.exceptions.QCommandException;
import com.gmail.molnardad.quester.elements.QElement;
import com.gmail.molnardad.quester.elements.Qevent;
import com.gmail.molnardad.quester.storage.StorageKey;
import com.gmail.molnardad.quester.utils.Util;

@QElement("ITEM")
public final class ItemQevent extends Qevent {
	
	public final Material material;
	public final short data;
	private final int amount;
	private final Map<Integer, Integer> enchants;
	private final boolean inverted;
	private final boolean quest;
	
	public ItemQevent(final Material mat, final int dat, final int amt, final Map<Integer, Integer> enchs, final boolean invert, final boolean quest) {
		inverted = invert;
		material = mat;
		this.quest = quest;
		if(!invert && dat < 0) {
			data = 0;
		}
		else {
			data = (short) dat;
		}
		amount = amt;
		if(enchs != null) {
			enchants = enchs;
		}
		else {
			enchants = new HashMap<Integer, Integer>();
		}
	}
	
	@Override
	public String info() {
		final String itm = material.name() + "[" + material.getId() + ":" + data + "]; AMT: " + amount;
		String enchs = enchants.isEmpty() ? "" : "\n -- ENCH:";
		for(final Integer e : enchants.keySet()) {
			enchs = enchs + " " + Enchantment.getById(e).getName() + ":" + enchants.get(e);
		}
		final StringBuilder flag = new StringBuilder();
		if(inverted || quest) {
			flag.append(" (-");
			if(quest) {
				flag.append('q');
			}
			if(inverted) {
				flag.append('i');
			}
			flag.append(')');
		}
		return itm + flag.toString() + enchs;
	}
	
	@Override
	protected void run(final Player player, final Quester plugin) {
		if(inverted) {
			int toRemove = amount;
			final Inventory inv = player.getInventory();
			final ItemStack[] contents = inv.getContents();
			for(int i = 0; i < contents.length; i++) {
				final ItemStack is = contents[i];
				if(is != null && is.getTypeId() == material.getId() && Util.isQuestItem(is) == quest && (data < 0 || is.getDurability() == data) && checkEnchants(is)) {
					if(is.getAmount() <= toRemove) {
						toRemove -= is.getAmount();
						contents[i] = null;
						if(toRemove == 0) {
							break;
						}
					}
					else {
						is.setAmount(is.getAmount() - toRemove);
						break;
					}
				}
			}
			inv.setContents(contents);
		}
		else {
			final int maxSize = material.getMaxStackSize();
			int toGive = amount;
			int numSpaces = 0;
			int given = 0;
			final ItemStack[] contents = player.getInventory().getContents();
			ItemStack item = new ItemStack(material, 1, data);
			if(quest) {
				final List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.BLUE + "Quest Item");
				final ItemMeta im = item.getItemMeta();
				im.setLore(lore);
				item.setItemMeta(im);
			}
			for(final Integer j : enchants.keySet()) {
				item.addUnsafeEnchantment(Enchantment.getById(j), enchants.get(j));
			}
			for(final ItemStack i : contents) {
				if(i == null) {
					numSpaces += maxSize;
				}
				else if(i.isSimilar(item)) {
					numSpaces += maxSize - i.getAmount();
				}
			}
			given = Math.min(toGive, numSpaces);
			toGive -= given;
			numSpaces = (int) Math.ceil((double) given / (double) maxSize);
			int round;
			final PlayerInventory inv = player.getInventory();
			for(int k = 0; k < numSpaces; k++) {
				round = Math.min(maxSize, given);
				item.setAmount(round);
				inv.addItem(item.clone());
				given -= round;
			}
			
			if(toGive > 0) {
				numSpaces = (int) Math.ceil((double) toGive / (double) maxSize);
				for(int k = 0; k < numSpaces; k++) {
					given = Math.min(toGive, maxSize);
					item = new ItemStack(material, given, data);
					for(final Integer j : enchants.keySet()) {
						item.addUnsafeEnchantment(Enchantment.getById(j), enchants.get(j));
					}
					player.getWorld().dropItem(player.getLocation(), item);
					toGive -= given;
				}
			}
		}
	}
	
	private boolean checkEnchants(final ItemStack is) {
		if(enchants.isEmpty()) {
			return true;
		}
		for(final Entry<Integer, Integer> e : enchants.entrySet()) {
			if(e.getValue() > is.getEnchantmentLevel(Enchantment.getById(e.getKey()))) {
				return false;
			}
		}
		return true;
	}
	
	@QCommand(min = 1, max = 3, usage = "{<item>} [amount] {[enchants]} (-i)")
	public static Qevent fromCommand(final QCommandContext context) throws QCommandException {
		Material mat = null;
		int dat;
		int amt = 1;
		Map<Integer, Integer> enchs = null;
		final int[] itm = parseItem(context.getString(0), context.getSenderLang());
		mat = Material.getMaterial(itm[0]);
		dat = itm[1];
		if(dat < 0) {
			dat = 0;
		}
		if(context.length() > 1) {
			amt = context.getInt(1);
			if(context.length() > 2) {
				enchs = parseEnchants(context.getString(2));
			}
		}
		if(amt < 1 || dat < -1) {
			throw new IllegalArgumentException(context.getSenderLang().ERROR_CMD_ITEM_NUMBERS);
		}
		if(context.length() > 2) {
			enchs = parseEnchants(context.getString(2));
		}
		
		return new ItemQevent(mat, dat, amt, enchs, context.hasFlag('i'), context.hasFlag('q'));
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.setString("item", Util.serializeItem(material, data));
		if(!enchants.isEmpty()) {
			key.setString("enchants", Util.serializeEnchants(enchants));
		}
		if(amount != 1) {
			key.setInt("amount", amount);
		}
		if(inverted) {
			key.setBoolean("inverted", true);
		}
		if(quest) {
			key.setBoolean("quest", true);
		}
	}
	
	protected static Qevent load(final StorageKey key) {
		Material mat = null;
		int dat = 0;
		try {
			final int[] itm = Util.parseItem(key.getString("item", ""));
			mat = Material.getMaterial(itm[0]);
			dat = itm[1];
			if(dat < 0) {
				dat = 0;
			}
		}
		catch (final Exception e) {
			return null;
		}
		
		int amt = key.getInt("amount", 1);
		if(amt < 1) {
			amt = 1;
		}
		
		Map<Integer, Integer> enchs = null;
		try {
			enchs = Util.parseEnchants(key.getString("enchants", ""));
		}
		catch (final IllegalArgumentException ignore) {}
		
		return new ItemQevent(mat, dat, amt, enchs, key.getBoolean("inverted", false), key.getBoolean("quest", false));
	}
}
