package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
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
		tryTargetRandomCreatureByTypeInRadius(FakePlayer.class, 1200);		
		tryAttackingUsingFighterOffensiveSkill();
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	public List<Pair<Integer, Double>> getOffensiveSpells()
	{
		List<Pair<Integer,Double>> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new Pair<>(263, 100/8d));
		_offensiveSpells.add(new Pair<>(122, 100/8d));
		_offensiveSpells.add(new Pair<>(11, 100/8d));
		_offensiveSpells.add(new Pair<>(410, 100/8d));			
		_offensiveSpells.add(new Pair<>(12, 100/8d));		
		_offensiveSpells.add(new Pair<>(321, 100/8d));
		_offensiveSpells.add(new Pair<>(344, 100/8d));		
		_offensiveSpells.add(new Pair<>(358, 100/8d));		
		
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