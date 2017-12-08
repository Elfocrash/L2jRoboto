package com.elfocrash.roboto.ai;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.model.BotSkill;
import com.elfocrash.roboto.model.HealingSpell;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SupportSpell;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.ShotType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;

public abstract class CombatAI extends FakePlayerAI {
	
	public CombatAI(FakePlayer character) {
		super(character);
	}
	
	protected void tryAttackingUsingMageOffensiveSkill() {
		if(_fakePlayer.getTarget() != null)
		{
			BotSkill botSkill = getRandomAvaiableMageSpellForTarget();
			if(botSkill == null)
				return;
			
			L2Skill skill = _fakePlayer.getSkill(getRandomAvaiableMageSpellForTarget().getSkillId());
			if(skill != null)
				castSpell(skill);
		}
	}
	
	protected void tryAttackingUsingFighterOffensiveSkill()	{
		if(_fakePlayer.getTarget() != null && _fakePlayer.getTarget() instanceof Creature) {			
			if(getOffensiveSpells() != null && !getOffensiveSpells().isEmpty()) {
				L2Skill skill = getRandomAvaiableFighterSpellForTarget();			
				if(skill != null) {
					castSpell(skill);
				}
			}			
			_fakePlayer.forceAutoAttack((Creature)_fakePlayer.getTarget());
		}
	}
	
	protected int getShotId() {
		int playerLevel = _fakePlayer.getLevel();
		if(playerLevel < 20)
			return getShotType() == ShotType.SOULSHOT ? 1835 : 3947;
		if(playerLevel >= 20 && playerLevel < 40)
			return getShotType() == ShotType.SOULSHOT ? 1463 : 3948;
		if(playerLevel >= 40 && playerLevel < 52)
			return getShotType() == ShotType.SOULSHOT ? 1464 : 3949;
		if(playerLevel >= 52 && playerLevel < 61)
			return getShotType() == ShotType.SOULSHOT ? 1465 : 3950;
		if(playerLevel >= 61 && playerLevel < 76)
			return getShotType() == ShotType.SOULSHOT ? 1466 : 3951;
		if(playerLevel >= 76)
			return getShotType() == ShotType.SOULSHOT ? 1467 : 3952;
		
		return 0;
	}
	
	protected int getArrowId() {
		int playerLevel = _fakePlayer.getLevel();
		if(playerLevel < 20)
			return 17; // wooden arrow
		if(playerLevel >= 20 && playerLevel < 40)
			return 1341; // bone arrow
		if(playerLevel >= 40 && playerLevel < 52)
			return 1342; // steel arrow
		if(playerLevel >= 52 && playerLevel < 61)
			return 1343; // Silver arrow
		if(playerLevel >= 61 && playerLevel < 76)
			return 1344; // Mithril Arrow
		if(playerLevel >= 76)
			return 1345; // shining
		
		return 0;
	}
	
	protected void handleShots() {
		if(_fakePlayer.getInventory().getItemByItemId(getShotId()) != null) {
			if(_fakePlayer.getInventory().getItemByItemId(getShotId()).getCount() <= 20) {
				_fakePlayer.getInventory().addItem("", getShotId(), 500, _fakePlayer, null);			
			}
		}else {
			_fakePlayer.getInventory().addItem("", getShotId(), 500, _fakePlayer, null);
		}
		
		if(_fakePlayer.getAutoSoulShot().isEmpty()) {
			_fakePlayer.addAutoSoulShot(getShotId());
			_fakePlayer.sendPacket(new ExAutoSoulShot(getShotId(), 1));
			
			if (_fakePlayer.getActiveWeaponItem() != _fakePlayer.getFistsWeaponItem() /*&& item.getItem().getCrystalType() == _fakePlayer.getActiveWeaponItem().getCrystalType()*/)
				_fakePlayer.rechargeShots(true, true);
		}	
	}
	
	public HealingSpell getRandomAvaiableHealingSpellForTarget() {

		List<HealingSpell> spellsOrdered = getHealingSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		BotSkill skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);
		return (HealingSpell)skill;
	}	
	
	protected BotSkill getRandomAvaiableMageSpellForTarget() {		
		
		List<OffensiveSpell> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillListSize = spellsOrdered.size();
		
		BotSkill skill = null;
		try {
			skill = waitAndPickAvailablePrioritisedSpell(spellsOrdered, skillListSize);	
		}catch(NullPointerException ex) {
			ex.printStackTrace();
		}
		
		return skill;
	}

	private BotSkill waitAndPickAvailablePrioritisedSpell(List<? extends BotSkill> spellsOrdered, int skillListSize) {
		int skillIndex = 0;		
		BotSkill botSkill = spellsOrdered.get(skillIndex);
		_fakePlayer.getCurrentSkill().setCtrlPressed(!_fakePlayer.getTarget().isInsideZone(ZoneId.PEACE));
		L2Skill skill = _fakePlayer.getSkill(botSkill.getSkillId());
		while(!_fakePlayer.checkUseMagicConditions(skill,true,false)) {
			
			_isBusyThinking = true;
			if(_fakePlayer.isDead() || _fakePlayer.isOutOfControl()) {
				_isBusyThinking = false;
				return null;
			}
			if((skillIndex < 0) || (skillIndex >= skillListSize)) {
				skillIndex = 0;				
			}
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			botSkill = spellsOrdered.get(skillIndex);
			skillIndex++;			
		}
		
		_isBusyThinking = false;
		return botSkill;
	}
	
	protected L2Skill getRandomAvaiableFighterSpellForTarget() {	
		int maxRetries = 10;
		int retries = 0;
		List<OffensiveSpell> spellsOrdered = getOffensiveSpells().stream().sorted((o1, o2)-> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
		int skillIndex = 0;
		int skillListSize = spellsOrdered.size();
		
		L2Skill skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
		
		_fakePlayer.getCurrentSkill().setCtrlPressed(!_fakePlayer.getTarget().isInsideZone(ZoneId.PEACE));		
		while(!_fakePlayer.checkUseMagicConditions(skill,true,false) && retries <= maxRetries) {
			if((skillIndex < 0) || (skillIndex >= skillListSize)) {
				skillIndex = 0;				
			}
			skill = _fakePlayer.getSkill(spellsOrdered.get(skillIndex).getSkillId());
			retries++;
			skillIndex++;
		}

		return skill;
	}
	
	protected void selfSupportBuffs() {
		List<Integer> activeEffects = Arrays.stream(_fakePlayer.getAllEffects())
				.map(x->x.getSkill().getId())
				.collect(Collectors.toList()); 
		
		for(SupportSpell selfBuff : getSelfSupportSpells()) {
			if(activeEffects.contains(selfBuff.getSkillId()))
				continue;
			
			L2Skill skill = SkillTable.getInstance().getInfo(selfBuff.getSkillId(), _fakePlayer.getSkillLevel(selfBuff.getSkillId()));
			
			if(!_fakePlayer.checkUseMagicConditions(skill,true,false))
				continue;
			
			switch(selfBuff.getCondition()) {
				case LESSHPPERCENT:
					if(Math.round(100.0 / _fakePlayer.getMaxHp() * _fakePlayer.getCurrentHp()) <= selfBuff.getConditionValue()) {
						castSelfSpell(skill);						
					}						
					break;
				case NONE:
					castSelfSpell(skill);		
				default:
					break;				
			}
			
		}
	}

	protected abstract ShotType getShotType();
	protected abstract List<OffensiveSpell> getOffensiveSpells();
	protected abstract List<HealingSpell> getHealingSpells();
	protected abstract List<SupportSpell> getSelfSupportSpells();
}
