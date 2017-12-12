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

public class DreadnoughtAI extends CombatAI implements IConsumableSpender {
	
	public DreadnoughtAI(FakePlayer character) {
		super(character);
	}

	@Override
	public void thinkAndAct() {		
		super.thinkAndAct();
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
		return 0.33;
	}

	@Override
	protected List<OffensiveSpell> getOffensiveSpells() {
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(361, 1));
		_offensiveSpells.add(new OffensiveSpell(347, 2));		
		_offensiveSpells.add(new OffensiveSpell(48, 3));
		_offensiveSpells.add(new OffensiveSpell(452, 4));
		_offensiveSpells.add(new OffensiveSpell(36, 5));		
		return _offensiveSpells;
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells() {
		List<SupportSpell> _selfSupportSpells = new ArrayList<>();
		_selfSupportSpells.add(new SupportSpell(440, SpellUsageCondition.MISSINGCP, 1000, 1));
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
