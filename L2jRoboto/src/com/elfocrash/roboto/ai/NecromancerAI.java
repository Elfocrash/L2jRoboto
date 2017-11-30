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
public class NecromancerAI extends FakePlayerAI
{
	private List<Pair<Integer,Double>> _offensiveSpells;
	
	public NecromancerAI(FakePlayer character)
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
		handleBones();
		handleShots();		
		
		tryTargetRandomCreatureByTypeInRadius(FakePlayer.class, 1200);	
		
		tryAttackingUsingMageOffensiveSkill();
	}
	
	private void handleBones() {
		if(_fakePlayer.getInventory().getItemByItemId(2508) != null) {
			if(_fakePlayer.getInventory().getItemByItemId(2508).getCount() <= 20) {
				_fakePlayer.getInventory().addItem("", 2508, 500, _fakePlayer, null);			
			}
		}else {
			_fakePlayer.getInventory().addItem("", 2508, 500, _fakePlayer, null);
		}
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.BLESSED_SPIRITSHOT;
	}
	
	@Override
	protected List<Pair<Integer, Double>> getOffensiveSpells()
	{
		_offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new Pair<>(1234, 33d));
		_offensiveSpells.add(new Pair<>(1148, 33d));
		_offensiveSpells.add(new Pair<>(1343, 33d));
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
}