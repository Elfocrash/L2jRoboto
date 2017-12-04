package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.model.OffensiveSpell;
import com.elfocrash.roboto.model.SpellUsageCondition;
import com.elfocrash.roboto.model.SupportSpell;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class TreasureHunterAI extends FakePlayerAI
{
	public TreasureHunterAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{
		if(_fakePlayer.isDead()) {
			return;
		}
		
		applyDefaultBuffs();
		handleShots();			
		tryTargetRandomCreatureByTypeInRadius(FakePlayerManager.INSTANCE.getTestTargetClass(), FakePlayerManager.INSTANCE.getTestTargetRange());		
		tryAttackingUsingFighterOffensiveSkill();
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(263, SpellUsageCondition.NONE, 4));
		_offensiveSpells.add(new OffensiveSpell(12, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(11, SpellUsageCondition.NONE, 2));
		_offensiveSpells.add(new OffensiveSpell(4, SpellUsageCondition.NONE, 3));
		_offensiveSpells.add(new OffensiveSpell(409, SpellUsageCondition.NONE, 5));
		_offensiveSpells.add(new OffensiveSpell(344, SpellUsageCondition.NONE, 6));
		_offensiveSpells.add(new OffensiveSpell(358, SpellUsageCondition.NONE, 7));
		return _offensiveSpells; 
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getFighterBuffs();
	}	
	
	@Override
	protected List<Pair<Integer, Double>> getHealingSpells()
	{		
		return Collections.emptyList();
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells() {
		return Collections.emptyList();
	}
}