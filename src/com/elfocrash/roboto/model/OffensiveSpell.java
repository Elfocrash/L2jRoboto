package com.elfocrash.roboto.model;

public class OffensiveSpell extends BotSkill {	
	
	public OffensiveSpell (int skillId, SpellUsageCondition condition, int conditionValue, int priority) {
		super(skillId, condition, conditionValue, priority);
	}
	
	public OffensiveSpell (int skillId, int priority) {
		super(skillId, SpellUsageCondition.NONE, 0, priority);
	}
	
	public OffensiveSpell (int skillId) {
		super(skillId);
	}		
}