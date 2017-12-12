package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.ai.addon.IConsumableSpender;
import com.elfocrash.roboto.model.HealingSpell;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SpellUsageCondition;
import com.elfocrash.roboto.model.SupportSpell;

import net.sf.l2j.gameserver.model.ShotType;

public class DuelistAI extends CombatAI implements IConsumableSpender {
	
	public DuelistAI(FakePlayer character) {
		super(character);
	}

	@Override
	public void thinkAndAct() {
		if(_fakePlayer.isDead())
			return;
		
		setBusyThinking(true);
		applyDefaultBuffs();
		handleShots();
		selfSupportBuffs();
		tryTargetRandomCreatureByTypeInRadius(FakePlayerManager.INSTANCE.getTestTargetClass(), FakePlayerManager.INSTANCE.getTestTargetRange());		
		tryAttackingUsingFighterOffensiveSkill();
		setBusyThinking(false);
	}

	@Override
	protected ShotType getShotType() {
		return ShotType.SOULSHOT;
	}	
	
	@Override
	protected double changeOfUsingSkill() {
		return 0.5;
	}

	@Override
	protected List<OffensiveSpell> getOffensiveSpells() {
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(345, 1));
		_offensiveSpells.add(new OffensiveSpell(261, 2));		
		_offensiveSpells.add(new OffensiveSpell(5, 3));		
		_offensiveSpells.add(new OffensiveSpell(6, 4));		
		_offensiveSpells.add(new OffensiveSpell(1, 5));
		return _offensiveSpells;
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells() {
		List<SupportSpell> _selfSupportSpells = new ArrayList<>();
		_selfSupportSpells.add(new SupportSpell(139, 1));
		_selfSupportSpells.add(new SupportSpell(297, 2));
		_selfSupportSpells.add(new SupportSpell(440, SpellUsageCondition.MISSINGCP, 1000, 3));
		return _selfSupportSpells;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getFighterBuffs();
	}
	
	@Override
	protected List<HealingSpell> getHealingSpells()
	{		
		return Collections.emptyList();
	}

}
