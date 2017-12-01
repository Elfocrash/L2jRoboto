package com.elfocrash.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.FakePlayerManager;
import com.elfocrash.roboto.model.SupportSpell;
import com.elfocrash.roboto.model.SupportSpellUsageCondition;

import javafx.util.Pair;
import net.sf.l2j.gameserver.model.ShotType;

/**
 * @author Elfocrash
 *
 */
public class DestroyerAI extends FakePlayerAI
{
	public DestroyerAI(FakePlayer character)
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
		selfSupportBuffs();
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
		_offensiveSpells.add(new Pair<>(315, 100/3d));
		_offensiveSpells.add(new Pair<>(190, 100/3d));
		_offensiveSpells.add(new Pair<>(362, 100/3d));
		return _offensiveSpells; 
	}
	
	@Override
	public List<SupportSpell> getSelfSupportSpells()
	{
		List<SupportSpell> _selfSupportSpells = new ArrayList<>();
		_selfSupportSpells.add(new SupportSpell(139, SupportSpellUsageCondition.LESSHPPERCENT, 30));
		_selfSupportSpells.add(new SupportSpell(176, SupportSpellUsageCondition.LESSHPPERCENT, 30));
		return _selfSupportSpells;
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