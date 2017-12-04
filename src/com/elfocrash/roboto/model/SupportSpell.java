package com.elfocrash.roboto.model;

public class SupportSpell extends BotSkill {
	
	public SupportSpell (int skillId, SpellUsageCondition condition, int conditionValue) {
		super(skillId, condition, conditionValue);		
	}
	
	public SupportSpell (int skillId, SpellUsageCondition condition) {
		super(skillId, condition, 0);
	}
}