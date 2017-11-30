package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class HawkeyeAI extends FakePlayerAI
{

	public HawkeyeAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{		
		if(_fakePlayer.isDead()) {
			return;
		}
		
		buffPlayer();
		handleArrows();
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
	protected List<Pair<Integer, Double>> getOffensiveSpells()
	{
		List<Pair<Integer,Double>> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new Pair<>(101, 100/2d));
		_offensiveSpells.add(new Pair<>(343, 100/2d));
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
}