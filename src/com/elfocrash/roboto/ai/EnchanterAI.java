package com.elfocrash.roboto.ai;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;

public class EnchanterAI extends FakePlayerAI {

	private int _enchantIterations = 0;
	private int _maxEnchant = Config.ENCHANT_MAX_WEAPON;
	private final int iterationsForAction = Rnd.get(3,5);
	
	public EnchanterAI(FakePlayer character) {
		super(character);
	}
	
	@Override
	public void setup() {
		super.setup();
		ItemInstance weapon = _fakePlayer.getActiveWeaponInstance();		
		weapon = checkIfWeaponIsExistsEquipped(weapon);		
		weapon.setEnchantLevel(0);
		_fakePlayer.broadcastCharInfo();
		
	}

	@Override
	public void thinkAndAct() {		
		
		handleDeath();
		setBusyThinking(true);
		if(_enchantIterations % iterationsForAction == 0) {	
			ItemInstance weapon = _fakePlayer.getActiveWeaponInstance();		
			weapon = checkIfWeaponIsExistsEquipped(weapon);			
			double chance = getSuccessChance(weapon);		
						
			int currentEnchantLevel = weapon.getEnchantLevel();
			if(currentEnchantLevel < _maxEnchant || serverHasUnlimitedMax()) {
				if (Rnd.nextDouble() < chance || weapon.getEnchantLevel() < 4) {				
					weapon.setEnchantLevel(currentEnchantLevel + 1);
					_fakePlayer.broadcastCharInfo();
				} else {				
					destroyFailedItem(weapon);
				}							
			}
		}
		_enchantIterations++;	
		setBusyThinking(false);
	}
	
	private void destroyFailedItem(ItemInstance weapon) {
		_fakePlayer.getInventory().destroyItem("Enchant", weapon, _fakePlayer, null);
		_fakePlayer.broadcastUserInfo();
		_fakePlayer.setActiveEnchantItem(null);
	}
	
	private double getSuccessChance(ItemInstance weapon) {
		double chance = 0d;
		if (((Weapon) weapon.getItem()).isMagical())
			chance = (weapon.getEnchantLevel() > 14) ? Config.ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS : Config.ENCHANT_CHANCE_WEAPON_MAGIC;
		else
			chance = (weapon.getEnchantLevel() > 14) ? Config.ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS : Config.ENCHANT_CHANCE_WEAPON_NONMAGIC;
		return chance;
	}
	
	private boolean serverHasUnlimitedMax() {
		return _maxEnchant == 0;
	}
	
	private ItemInstance checkIfWeaponIsExistsEquipped(ItemInstance weapon) {
		if(weapon == null) {
			FakePlayerManager.INSTANCE.giveWeaponsByClass(_fakePlayer, false);
			weapon = _fakePlayer.getActiveWeaponInstance();
		}
		return weapon;
	}

	@Override
	protected int[][] getBuffs() {
		return new int[0][0];
	}
}
