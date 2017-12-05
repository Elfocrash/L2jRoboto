package com.elfocrash.roboto.ai.addon;

import java.util.List;
import java.util.stream.Collectors;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.model.HealingSpell;

import net.sf.l2j.gameserver.model.actor.Creature;

public interface IHealer {
	
	default void tryTargetingLowestHpTargetInRadius(FakePlayer player, Class<? extends Creature> creatureClass, int radius) {
		if(player.getTarget() == null) {
			List<Creature> targets = player.getKnownTypeInRadius(creatureClass, radius).stream()
					.filter(x->!x.isDead())					
					.collect(Collectors.toList());
			
			if(!player.isDead())
				targets.add(player);		
			
			List<Creature> sortedTargets = targets.stream().sorted((x1, x2) -> Double.compare(x1.getCurrentHp(), x2.getCurrentHp())).collect(Collectors.toList());
			
			if(!sortedTargets.isEmpty()) {
				Creature target = sortedTargets.get(0);
				player.setTarget(target);				
			}
		}else {
			if(((Creature)player.getTarget()).isDead())
				player.setTarget(null);
		}	
	}
	
	default void tryHealingTarget(FakePlayer player) {
		
		if(player.getTarget() != null && player.getTarget() instanceof Creature)
		{
			Creature target = (Creature) player.getTarget();
			HealingSpell skill = player.getFakeAi().getRandomAvaiableHealingSpellForTarget();
			if(skill != null) {
				switch(skill.getCondition()){
					case LESSHPPERCENT:
						double currentHpPercentage = Math.round(100.0 / target.getMaxHp() * target.getCurrentHp());
						if(currentHpPercentage <= skill.getConditionValue()) {
							player.getFakeAi().castSpell(player.getSkill(skill.getSkillId()));						
						}						
						break;				
					default:
						break;							
				}
				
			}
				
		}
	}
}
