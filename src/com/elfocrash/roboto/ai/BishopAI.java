package com.elfocrash.roboto.ai;

import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author elfoc
 *
 */
public class BishopAI extends FakePlayerAI
{
	public BishopAI(FakePlayer character)
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
		return ShotType.BLESSED_SPIRITSHOT;
	}
	
	@Override
	protected List<Pair<Integer, Double>> getOffensiveSpells()
	{		
		return Collections.emptyList();
	}
	
	@Override
	protected List<Pair<Integer, Double>> getHealingSpells()
	{		
		return Collections.emptyList();
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakePlayerManager.INSTANCE.getMageBuffs();
	}	
}
