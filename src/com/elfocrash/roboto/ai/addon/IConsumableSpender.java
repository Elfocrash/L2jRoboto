package com.elfocrash.roboto.ai.addon;

import com.elfocrash.roboto.FakePlayer;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public interface IConsumableSpender {

	default void handleConsumable(FakePlayer fakePlayer, int consumableId) {
		if(fakePlayer.getInventory().getItemByItemId(consumableId) != null) {
			if(fakePlayer.getInventory().getItemByItemId(consumableId).getCount() <= 20) {
				fakePlayer.getInventory().addItem("", consumableId, 500, fakePlayer, null);			
				
			}
		}else {
			fakePlayer.getInventory().addItem("", consumableId, 500, fakePlayer, null);
			ItemInstance consumable = fakePlayer.getInventory().getItemByItemId(consumableId);
			if(consumable.isEquipable())
				fakePlayer.getInventory().equipItem(consumable);
		}
	}
}
