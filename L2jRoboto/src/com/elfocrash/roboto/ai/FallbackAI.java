package com.elfocrash.roboto.ai;

import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class FallbackAI extends FakePlayerAI
{

	public FallbackAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{
		
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	protected List<Pair<Integer, Double>> getOffensiveSpells()
	{
		return Collections.emptyList();
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return new int[0][0];
	}
	
	@Override
	protected List<Pair<Integer, Double>> getHealingSpells()
	{		
		return Collections.emptyList();
	}
}
