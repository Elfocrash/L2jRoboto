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
public class AbyssWalkerAI extends FakePlayerAI
{
	public AbyssWalkerAI(FakePlayer character)
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
	public List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(263, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(122, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(11, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(410, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(12, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(321, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(344, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(358, SpellUsageCondition.NONE, 1));		
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