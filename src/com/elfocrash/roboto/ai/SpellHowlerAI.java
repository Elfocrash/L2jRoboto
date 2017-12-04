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
public class SpellHowlerAI extends FakePlayerAI
{
	public SpellHowlerAI(FakePlayer character)
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
		tryAttackingUsingMageOffensiveSkill();		
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.BLESSED_SPIRITSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(1341, SpellUsageCondition.NONE, 1));
		_offensiveSpells.add(new OffensiveSpell(1343, SpellUsageCondition.NONE, 2));
		_offensiveSpells.add(new OffensiveSpell(1234, SpellUsageCondition.NONE, 3));
		_offensiveSpells.add(new OffensiveSpell(1239, SpellUsageCondition.NONE, 4));
		return _offensiveSpells; 
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getMageBuffs();
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
